package nezerx.aspectalchemy.block.entity;

import net.minecraft.item.DyeItem;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import nezerx.aspectalchemy.data.AspectAlchemyData;
import nezerx.aspectalchemy.init.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import java.util.*;

public class AspectCauldronBlockEntity extends BlockEntity {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);

    private ItemStack loadedDye = ItemStack.EMPTY;

    private List<StatusEffectInstance> cachedEffects = null;
    private Integer cachedWaterColor = null;

    private int tippedArrowsUsed = 0;
    private static final int ARROWS_PER_LEVEL = 32;

    private int heatTicks = 0;
    private static final int BOIL_TICKS = 200; // 10 секунд

    private static final int VANILLA_WATER_COLOR = 0x3F76E4;

    public AspectCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ASPECT_CAULDRON, pos, state);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    @Nullable
    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        tippedArrowsUsed = nbt.getInt("TippedArrowsUsed");
        inventory.clear();
        NbtList list = nbt.getList("Items", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound slot = list.getCompound(i);
            int slotIdx = slot.getByte("Slot");
            if (slotIdx >= 0 && slotIdx < inventory.size()) {
                inventory.set(slotIdx, ItemStack.fromNbt(slot));
            }
        }

        if (nbt.contains("LoadedDye", NbtElement.COMPOUND_TYPE)) {
            loadedDye = ItemStack.fromNbt(nbt.getCompound("LoadedDye"));
        } else {
            loadedDye = ItemStack.EMPTY;
        }

        heatTicks = nbt.getInt("HeatTicks");
        cachedEffects = null;
        cachedWaterColor = null;

        if (world != null) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
        }
    }

    // ── Тик нагрева ───────────────────────────────────────────────────────────

    public boolean tickHeat(boolean hasHeatBelow) {
        if (hasHeatBelow) {
            if (heatTicks < BOIL_TICKS) {
                heatTicks++;
                markDirty();
                return heatTicks == BOIL_TICKS; // стал кипеть
            }
        } else {
            if (heatTicks > 0) {
                heatTicks = 0;
                markDirty();
                return true; // перестал кипеть
            }
        }
        return false;
    }

    public boolean isBoiling() {
        return heatTicks >= BOIL_TICKS;
    }

    // ── Инвентарь ─────────────────────────────────────────────────────────────

    public boolean canAddIngredient() {
        return getIngredientCount() < 3;
    }

    public int getIngredientCount() {
        int count = 0;
        for (ItemStack stack : inventory) if (!stack.isEmpty()) count++;
        return count;
    }

    public boolean addIngredient(ItemStack stack) {
        if (stack.isEmpty() || !canAddIngredient()) return false;

        for (ItemStack existing : inventory) {
            if (!existing.isEmpty() && existing.isOf(stack.getItem())) return false;
        }

        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).isEmpty()) {
                inventory.set(i, stack.copyWithCount(1));
                invalidateAndSync();
                return true;
            }
        }
        return false;
    }

    public void clearInventory() {
        for (int i = 0; i < inventory.size(); i++) inventory.set(i, ItemStack.EMPTY);
        invalidateAndSync();
    }

    public boolean loadDye(ItemStack stack) {
        if (!loadedDye.isEmpty() || !(stack.getItem() instanceof net.minecraft.item.DyeItem)) return false;
        loadedDye = stack.copyWithCount(1);
        invalidateAndSync();
        return true;
    }
    public boolean hasLoadedDye() { return !loadedDye.isEmpty(); }
    public net.minecraft.util.DyeColor getLoadedDyeColor() {
        return loadedDye.isEmpty() ? null : ((net.minecraft.item.DyeItem) loadedDye.getItem()).getColor();
    }
    public void clearLoadedDye() { loadedDye = ItemStack.EMPTY; invalidateAndSync(); }

    public DefaultedList<ItemStack> getInventory() {
        return inventory;
    }

    // ── Кэш и синхронизация ───────────────────────────────────────────────────

    private void invalidateAndSync() {
        cachedEffects = null;
        cachedWaterColor = null;
        super.markDirty();
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
        }
    }

    @Override
    public void markDirty() {
        cachedEffects = null;
        cachedWaterColor = null;
        super.markDirty();
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
        }
    }

    // ── Эффекты ───────────────────────────────────────────────────────────────

    private void ensureEffectsCached() {
        if (cachedEffects != null) return;

        List<StatusEffect> allEffects = new ArrayList<>();
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty() && AspectAlchemyData.ASPECT_MAP.containsKey(stack.getItem())) {
                allEffects.addAll(AspectAlchemyData.ASPECT_MAP.get(stack.getItem()));
            }
        }

        Map<StatusEffect, Integer> counts = new LinkedHashMap<>();
        for (StatusEffect effect : allEffects) {
            counts.merge(effect, 1, Integer::sum);
        }

        List<StatusEffectInstance> result = new ArrayList<>();
        for (Map.Entry<StatusEffect, Integer> entry : counts.entrySet()) {
            int count = entry.getValue();
            if (count >= 2) {
                int amplifier = Math.min(count - 2, 4); // I=1, II=2, III=3... кап V
                result.add(new StatusEffectInstance(
                        entry.getKey(), 3600, amplifier,
                        false, false, true
                ));
            }
        }
        cachedEffects = result;
    }

    // ── Цвет воды ─────────────────────────────────────────────────────────────

    public int getWaterColor() {
        if (cachedWaterColor != null) return cachedWaterColor;

        // Если загружен краситель — показываем его цвет
        if (!loadedDye.isEmpty() && loadedDye.getItem() instanceof DyeItem dyeItem) {
            cachedWaterColor = dyeItem.getColor().getFireworkColor();
            return cachedWaterColor;
        }

        ensureEffectsCached();

        if (cachedEffects == null || cachedEffects.isEmpty()) {
            cachedWaterColor = VANILLA_WATER_COLOR;
            return cachedWaterColor;
        }

        long totalR = 0, totalG = 0, totalB = 0;
        int count = 0;
        for (StatusEffectInstance instance : cachedEffects) {
            int color = instance.getEffectType().getColor();
            totalR += (color >> 16) & 0xFF;
            totalG += (color >> 8) & 0xFF;
            totalB += color & 0xFF;
            count++;
        }

        cachedWaterColor = (int) ((totalR / count) << 16 | (totalG / count) << 8 | (totalB / count));
        return cachedWaterColor;
    }

    // ── Создание зелья ────────────────────────────────────────────────────────

    public ItemStack createPotionStack(Item targetPotionItem) {
        ensureEffectsCached();
        ItemStack bottle = new ItemStack(targetPotionItem);

        if (cachedEffects == null || cachedEffects.isEmpty()) {
            return bottle;
        }

        PotionUtil.setPotion(bottle, Potions.EMPTY);
        PotionUtil.setCustomPotionEffects(bottle, cachedEffects);
        bottle.getOrCreateNbt().putInt("CustomPotionColor", getWaterColor());

        // ↓ БЫЛО: bottle.getOrCreateNbt().putInt("SipsLeft", 1);
        if (targetPotionItem instanceof nezerx.aspectalchemy.item.MultiUsePotionItem multiPotion) {
            multiPotion.setSipsLeft(bottle, 1); // теперь CustomModelData тоже выставляется
        }

        List<StatusEffectInstance> sorted = new ArrayList<>(cachedEffects);
        sorted.sort(Comparator
                .comparingInt(StatusEffectInstance::getAmplifier).reversed()
                .thenComparing(eff -> eff.getEffectType().isBeneficial(), Comparator.reverseOrder())
                .thenComparingInt(StatusEffectInstance::getDuration).reversed());

        List<StatusEffectInstance> primary = new ArrayList<>();
        int maxAmplifier = sorted.get(0).getAmplifier();
        for (StatusEffectInstance eff : sorted) {
            if (eff.getAmplifier() == maxAmplifier) primary.add(eff);
            else break;
        }

        bottle.setCustomName(buildPotionName(primary));
        return bottle;
    }


    private MutableText buildPotionName(List<StatusEffectInstance> primary) {
        boolean hasBeneficial = primary.stream().anyMatch(eff -> eff.getEffectType().isBeneficial());

        String prefixKey = hasBeneficial
                ? "item.aspectalchemy.potion.prefix.beneficial"
                : "item.aspectalchemy.potion.prefix.harmful";

        Formatting vanillaColor = Formatting.BLUE;

        MutableText name = Text.translatable(prefixKey)
                .append(Text.literal(" "))
                .formatted(vanillaColor);

        for (int i = 0; i < primary.size(); i++) {
            StatusEffectInstance eff = primary.get(i);

            String originalKey = eff.getEffectType().getTranslationKey();
            String declinedKey = originalKey.contains("minecraft")
                    ? originalKey.replace("effect.minecraft.", "effect.aspectalchemy.declined.")
                    : "effect.aspectalchemy.declined." + originalKey.substring(originalKey.lastIndexOf('.') + 1);

            name.append(Text.translatable(declinedKey));

            if (eff.getAmplifier() > 0) {
                name.append(Text.literal(" " + toRoman(eff.getAmplifier() + 1)));
            }

            if (i < primary.size() - 1) {
                name.append(Text.literal(" "))
                        .append(Text.translatable("item.aspectalchemy.potion.conjunction"))
                        .append(Text.literal(" "));
            }
        }

        return name.styled(style -> style.withItalic(false).withColor(vanillaColor));
    }


    /** Конвертирует число в римскую цифру (достаточно до V для зелий) */
    private static String toRoman(int n) {
        return switch (n) {
            case 2  -> "II";
            case 3  -> "III";
            case 4  -> "IV";
            case 5  -> "V";
            default -> String.valueOf(n);
        };
    }


    public List<StatusEffectInstance> getActiveEffects() {
        ensureEffectsCached();
        return cachedEffects != null ? cachedEffects : Collections.emptyList();
    }


    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("TippedArrowsUsed", tippedArrowsUsed);
        nbt.putInt("HeatTicks", heatTicks);
        NbtList list = new NbtList();
        for (int i = 0; i < inventory.size(); i++) {
            if (!inventory.get(i).isEmpty()) {
                NbtCompound slot = new NbtCompound();
                slot.putByte("Slot", (byte) i);
                inventory.get(i).writeNbt(slot);
                list.add(slot);
            }
        }
        nbt.put("Items", list);



        if (!loadedDye.isEmpty()) {
            nbt.put("LoadedDye", loadedDye.writeNbt(new NbtCompound()));
        }
    }

    public boolean canTipArrows() {
        return !getActiveEffects().isEmpty() && tippedArrowsUsed < ARROWS_PER_LEVEL;
    }

    public int getRemainingArrows() {
        return ARROWS_PER_LEVEL - tippedArrowsUsed;
    }

    private boolean pendingLevelDecrease = false;

    public boolean consumePendingLevelDecrease() {
        if (pendingLevelDecrease) {
            pendingLevelDecrease = false;
            return true;
        }
        return false;
    }
    /** Возвращает сколько стрел реально пропитали (может быть меньше запрошенного) */
    public int tipArrows(int count) {
        int canTip = Math.min(count, getRemainingArrows());
        if (canTip <= 0) return 0;
        tippedArrowsUsed += canTip;
        if (tippedArrowsUsed >= ARROWS_PER_LEVEL) {
            tippedArrowsUsed = 0;
            pendingLevelDecrease = true;
        }
        invalidateAndSync();
        return canTip;
    }

    public ItemStack createTippedArrowStack(int count) {
        ensureEffectsCached();
        ItemStack arrow = new ItemStack(Items.TIPPED_ARROW, count);

        if (cachedEffects == null || cachedEffects.isEmpty()) return arrow;

        PotionUtil.setPotion(arrow, Potions.EMPTY);
        PotionUtil.setCustomPotionEffects(arrow, cachedEffects);
        arrow.getOrCreateNbt().putInt("CustomPotionColor", getWaterColor());
        arrow.setCustomName(buildArrowName());
        return arrow;
    }

    private MutableText buildArrowName() {
        ensureEffectsCached();

        List<StatusEffectInstance> sorted = new ArrayList<>(cachedEffects);
        sorted.sort(Comparator
                .comparingInt(StatusEffectInstance::getAmplifier).reversed()
                .thenComparing(eff -> eff.getEffectType().isBeneficial(), Comparator.reverseOrder())
                .thenComparingInt(StatusEffectInstance::getDuration).reversed());

        List<StatusEffectInstance> primary = new ArrayList<>();
        int maxAmplifier = sorted.get(0).getAmplifier();
        for (StatusEffectInstance eff : sorted) {
            if (eff.getAmplifier() == maxAmplifier) primary.add(eff);
            else break;
        }

        boolean hasBeneficial = primary.stream().anyMatch(e -> e.getEffectType().isBeneficial());
        String prefixKey = hasBeneficial
                ? "item.aspectalchemy.arrow.prefix.beneficial"
                : "item.aspectalchemy.arrow.prefix.harmful";

        Formatting color = Formatting.RED; // стрелы — красный, чтобы отличались от зелий

        MutableText name = Text.translatable(prefixKey)
                .append(Text.literal(" "))
                .formatted(color);

        for (int i = 0; i < primary.size(); i++) {
            StatusEffectInstance eff = primary.get(i);

            String originalKey = eff.getEffectType().getTranslationKey();
            String declinedKey = originalKey.contains("minecraft")
                    ? originalKey.replace("effect.minecraft.", "effect.aspectalchemy.declined.")
                    : "effect.aspectalchemy.declined." + originalKey.substring(originalKey.lastIndexOf('.') + 1);

            name.append(Text.translatable(declinedKey));

            if (eff.getAmplifier() > 0) {
                name.append(Text.literal(" " + toRoman(eff.getAmplifier() + 1)));
            }
            if (i < primary.size() - 1) {
                name.append(Text.literal(" "))
                        .append(Text.translatable("item.aspectalchemy.potion.conjunction"))
                        .append(Text.literal(" "));
            }
        }

        return name.styled(style -> style.withItalic(false).withColor(color));
    }
}