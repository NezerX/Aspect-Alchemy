package nezerx.aspectalchemy.block.entity;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import nezerx.aspectalchemy.block.AspectCauldronBlock;
import net.minecraft.world.World;
import net.minecraft.entity.ItemEntity;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Box;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import nezerx.aspectalchemy.AspectAlchemyMod;
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

    private Box itemCollectionBox;

    private List<StatusEffectInstance> cachedEffects = null;
    private Integer cachedWaterColor = null;

    private int tippedArrowsUsed = 0;
    private static final int ARROWS_PER_LEVEL = 32;

    private int heatTicks = 0;
    private static final int BOIL_TICKS = 200; // 10 секунд

    private static final int VANILLA_WATER_COLOR = 0x3F76E4;

    public static void tick(World world, BlockPos pos, BlockState state, AspectCauldronBlockEntity be) {
        if (world.isClient) return;

        // Нагрев
        BlockState below = world.getBlockState(pos.down());
        boolean hasHeat = below.isOf(Blocks.CAMPFIRE) || below.isOf(Blocks.FIRE)
                || below.isOf(Blocks.SOUL_FIRE) || below.isOf(Blocks.LAVA)
                || below.isOf(Blocks.MAGMA_BLOCK);

        boolean changed = be.tickHeat(hasHeat);
        if (changed) {
            boolean shouldBoil = be.isBoiling();
            if (state.get(AspectCauldronBlock.BOILING) != shouldBoil) {
                world.setBlockState(pos, state.with(AspectCauldronBlock.BOILING, shouldBoil), Block.NOTIFY_LISTENERS);
            }
        }

        // Предметы — каждые 10 тиков
        if (world.getTime() % 10 != 0) return;

        // Большая Box — вся внутренность котла
        Box box = be.itemCollectionBox;
        List<ItemEntity> items = world.getEntitiesByClass(ItemEntity.class, box, e -> !e.getStack().isEmpty());

        for (ItemEntity itemEntity : items) {
            ItemStack stack = itemEntity.getStack();

            // Ингредиенты
            if (AspectAlchemyData.ASPECT_MAP.containsKey(stack.getItem())) {
                if (!be.isBoiling() || !be.canAddIngredient()) continue;
                if (be.addIngredient(stack)) {
                    stack.decrement(1);
                    if (stack.isEmpty()) itemEntity.discard();
                    else itemEntity.setStack(stack);
                    world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 0.5f, 1.0f);
                }
                continue;
            }

            // Краситель
            if (stack.getItem() instanceof DyeItem && !be.hasLoadedDye()) {
                be.loadDye(stack);
                stack.decrement(1);
                if (stack.isEmpty()) itemEntity.discard();
                else itemEntity.setStack(stack);
                world.playSound(null, pos, SoundEvents.ITEM_DYE_USE, SoundCategory.BLOCKS, 0.8f, 1.0f);
                continue;
            }

            // Окрашивание
            if (be.hasLoadedDye() && AspectAlchemyMod.DYEABLE_ITEMS.contains(stack.getItem())) {
                DyeColor color = be.getLoadedDyeColor();
                String itemId = Registries.ITEM.getId(stack.getItem()).getPath();
                int maxCount = (itemId.endsWith("_banner") || itemId.endsWith("_carpet")) ? 64 : 16;
                int toDye = Math.min(stack.getCount(), maxCount);

                // getDyedItem живёт в AspectCauldronBlock — нужен доступ
                if (world.getBlockState(pos).getBlock() instanceof AspectCauldronBlock block) {
                    Item dyedItem = block.getDyedItem(stack.getItem(), color);
                    if (dyedItem != Items.AIR) {
                        ItemStack result = new ItemStack(dyedItem, toDye);
                        stack.decrement(toDye);
                        if (stack.isEmpty()) itemEntity.discard();
                        else itemEntity.setStack(stack);
                        ItemEntity output = new ItemEntity(world, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), result);
                        output.setVelocity(0, 0.1, 0);
                        world.spawnEntity(output);
                        be.clearLoadedDye();
                        block.decreaseCauldronLevel(state, world, pos, be);
                        world.playSound(null, pos, SoundEvents.ITEM_DYE_USE, SoundCategory.BLOCKS, 0.8f, 1.0f);
                    }
                }
            }
            // Стрелы
            if (stack.getItem() == Items.ARROW && be.canTipArrows()) {
                int tipped = be.tipArrows(stack.getCount());
                if (tipped > 0) {
                    ItemStack tippedStack = be.createTippedArrowStack(tipped);
                    stack.decrement(tipped);
                    if (stack.isEmpty()) itemEntity.discard();
                    else itemEntity.setStack(stack);
                    ItemEntity output = new ItemEntity(world, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), tippedStack);
                    output.setVelocity(0, 0.1, 0);
                    world.spawnEntity(output);
                    if (be.consumePendingLevelDecrease()) {
                        if (world.getBlockState(pos).getBlock() instanceof AspectCauldronBlock block) {
                            block.decreaseCauldronLevel(state, world, pos, be);
                        }
                    }
                    world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 0.5f, 1.2f);
                    continue;
                }
            }
        }
    }

    public AspectCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ASPECT_CAULDRON, pos, state);
        // Реальные внутренние размеры котла — чуть уже, чем раньше
        this.itemCollectionBox = new Box(
                pos.getX() + 0.2, pos.getY() + 0.3, pos.getZ() + 0.2,
                pos.getX() + 0.8, pos.getY() + 0.9, pos.getZ() + 0.8
        );
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
        pendingLevelDecrease = nbt.getBoolean("PendingLevelDecrease");
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
                // markDirty() убран — не сохраняем каждый тик
                if (heatTicks == BOIL_TICKS) {
                    markDirty(); // сохраняем только в момент закипания
                    return true;
                }
            }
        } else {
            if (heatTicks > 0) {
                heatTicks = 0;
                markDirty(); // сохраняем только при остановке
                return true;
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
        super.markDirty(); // только запись на диск
    }

    // ── Эффекты ───────────────────────────────────────────────────────────────

    private void ensureEffectsCached() {
        if (cachedEffects != null) return;

        // Суммируем силу каждого эффекта по всем ингредиентам
        Map<StatusEffect, Integer> powerSums = new LinkedHashMap<>();
        for (ItemStack stack : inventory) {
            if (stack.isEmpty()) continue;
            List<AspectAlchemyData.AspectEntry> entries = AspectAlchemyData.ASPECT_MAP.get(stack.getItem());
            if (entries == null) continue;
            for (AspectAlchemyData.AspectEntry entry : entries) {
                powerSums.merge(entry.effect(), entry.power(), Integer::sum);
            }
        }

        // Порог появления эффекта — суммарная сила >= 2
        List<StatusEffectInstance> result = new ArrayList<>();
        for (Map.Entry<StatusEffect, Integer> entry : powerSums.entrySet()) {
            int totalPower = entry.getValue();
            if (totalPower >= 2) {
                int amplifier = Math.min(totalPower - 2, 4);
                int duration = entry.getKey().isInstant() ? 1 : 3600;
                result.add(new StatusEffectInstance(
                        entry.getKey(), duration, amplifier,
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

        if (targetPotionItem instanceof nezerx.aspectalchemy.item.MultiUsePotionItem multiPotion) {
            multiPotion.setSipsLeft(bottle, 1); // теперь CustomModelData тоже выставляется
        }

        List<StatusEffectInstance> sorted = new ArrayList<>(cachedEffects);
        sorted.sort(
                Comparator.comparingInt((StatusEffectInstance e) -> e.getAmplifier()).reversed()
                        .thenComparingInt(e -> -AspectAlchemyData.NAMING_WEIGHT.getOrDefault(e.getEffectType(), 0))
        );

        List<StatusEffectInstance> primary = new ArrayList<>();
        int maxAmplifier = sorted.get(0).getAmplifier();
        for (StatusEffectInstance eff : sorted) {
            if (eff.getAmplifier() == maxAmplifier) primary.add(eff);
            else break;
        }

        boolean healingDominant = primary.stream().anyMatch(e ->
                e.getEffectType() == StatusEffects.INSTANT_HEALTH ||
                        e.getEffectType() == StatusEffects.REGENERATION
        );

        if (healingDominant) {
            List<StatusEffectInstance> adjusted = new ArrayList<>();
            for (StatusEffectInstance eff : cachedEffects) {
                StatusEffect type = eff.getEffectType();
                boolean isSideEffect =
                        type == StatusEffects.SLOWNESS ||
                                type == StatusEffects.WEAKNESS;

                if (isSideEffect) {
                    // 30 секунд вместо стандартных 3 минут
                    adjusted.add(new StatusEffectInstance(
                            type, 600, eff.getAmplifier(),
                            false, false, true
                    ));
                } else {
                    adjusted.add(eff);
                }
            }
            cachedEffects = adjusted; // [ИЗМЕНЕНИЕ] обновляем кэш перед записью в бутылку
            PotionUtil.setCustomPotionEffects(bottle, cachedEffects); // [ИЗМЕНЕНИЕ] перезаписываем NBT
        }
        bottle.setCustomName(buildPotionName(primary));

        if (world != null && !world.isClient) {
            ServerPlayerEntity player = (ServerPlayerEntity) world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 3.0, false);
            if (player != null) {
                checkAndUnlockAspects(player, cachedEffects);
            }
        }

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
    public static String toRoman(int n) {
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
        nbt.putBoolean("PendingLevelDecrease", pendingLevelDecrease);
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
        sorted.sort(
                Comparator.comparingInt((StatusEffectInstance e) -> e.getAmplifier()).reversed()
                        .thenComparingInt(e -> -AspectAlchemyData.NAMING_WEIGHT.getOrDefault(e.getEffectType(), 0))
        );

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

    private void checkAndUnlockAspects(ServerPlayerEntity player, List<StatusEffectInstance> resultEffects) {
        for (ItemStack ingredient : inventory) {
            if (ingredient.isEmpty()) continue;
            Item item = ingredient.getItem();
            List<AspectAlchemyData.AspectEntry> entries = AspectAlchemyData.ASPECT_MAP.get(item);
            if (entries == null) continue;

            String itemId = Registries.ITEM.getId(item).getPath();

            for (int i = 0; i < entries.size(); i++) {
                StatusEffect aspect = entries.get(i).effect();
                Identifier advId = new Identifier("aspectalchemy", "discovery/" + itemId + "_" + i);

                boolean effectPresent = resultEffects.stream().anyMatch(e -> e.getEffectType() == aspect);

                if (effectPresent) {
                    grantAdvancement(player, advId);
                    if (i + 1 < entries.size()) {
                        grantAdvancement(player, new Identifier("aspectalchemy", "discovery/" + itemId + "_" + (i + 1)));
                    }
                }
            }
        }
    }

    private void grantAdvancement(ServerPlayerEntity player, Identifier id) {
        Advancement advancement = player.getServer().getAdvancementLoader().get(id);
        if (advancement != null) {
            AdvancementProgress progress = player.getAdvancementTracker().getProgress(advancement);
            if (!progress.isDone()) {
                for (String criterion : progress.getUnobtainedCriteria()) {
                    player.getAdvancementTracker().grantCriterion(advancement, criterion);
                }
            }
        }
    }
}