package nezerx.aspectalchemy.block.entity;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.ChatFormatting;
import nezerx.aspectalchemy.AspectAlchemyMod;
import nezerx.aspectalchemy.block.AspectCauldronBlock;
import nezerx.aspectalchemy.data.AspectAlchemyData;
import nezerx.aspectalchemy.init.ModBlockEntities;
import nezerx.aspectalchemy.item.MultiUsePotionItem;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.component.DataComponents;

import java.util.*;

public class AspectCauldronBlockEntity extends BlockEntity {

    private final List<ItemStack> inventory = new ArrayList<>(Arrays.asList(
            ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
    ));

    private ItemStack loadedDye = ItemStack.EMPTY;
    private AABB itemCollectionBox;
    private List<MobEffectInstance> cachedEffects = null;
    private Integer cachedWaterColor = null;
    private int tippedArrowsUsed = 0;
    private static final int ARROWS_PER_LEVEL = 32;
    private int heatTicks = 0;
    private static final int BOIL_TICKS = 200;
    private static final int VANILLA_WATER_COLOR = 0x3F76E4;
    private boolean pendingLevelDecrease = false;

    public AspectCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ASPECT_CAULDRON.get(), pos, state);
        this.itemCollectionBox = new AABB(
                pos.getX() + 0.2, pos.getY() + 0.3, pos.getZ() + 0.2,
                pos.getX() + 0.8, pos.getY() + 0.9, pos.getZ() + 0.8
        );
    }

    // ── Тик ──────────────────────────────────────────────────────────────────

    public static void tick(Level level, BlockPos pos, BlockState state, AspectCauldronBlockEntity be) {
        if (level.isClientSide) return;

        BlockState below = level.getBlockState(pos.below());
        boolean hasHeat = below.is(Blocks.CAMPFIRE) || below.is(Blocks.FIRE)
                || below.is(Blocks.SOUL_FIRE) || below.is(Blocks.LAVA)
                || below.is(Blocks.MAGMA_BLOCK);

        boolean changed = be.tickHeat(hasHeat);
        if (changed) {
            boolean shouldBoil = be.isBoiling();
            if (!state.getValue(AspectCauldronBlock.BOILING).equals(shouldBoil)) {
                level.setBlock(pos, state.setValue(AspectCauldronBlock.BOILING, shouldBoil), Block.UPDATE_CLIENTS);
            }
        }

        if (level.getGameTime() % 10 != 0) return;

        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, be.itemCollectionBox,
                e -> !e.getItem().isEmpty());

        for (ItemEntity itemEntity : items) {
            ItemStack stack = itemEntity.getItem();

            // Ингредиенты
            if (AspectAlchemyData.ASPECT_MAP.containsKey(stack.getItem())) {
                if (!be.isBoiling() || !be.canAddIngredient()) continue;
                if (be.addIngredient(stack)) {
                    stack.shrink(1);
                    if (stack.isEmpty()) itemEntity.discard();
                    else itemEntity.setItem(stack);
                    level.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.5f, 1.0f);
                }
                continue;
            }

            // Краситель
            if (stack.getItem() instanceof DyeItem && !be.hasLoadedDye()) {
                be.loadDye(stack);
                stack.shrink(1);
                if (stack.isEmpty()) itemEntity.discard();
                else itemEntity.setItem(stack);
                level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 0.8f, 1.0f);
                continue;
            }

            // Окрашивание
            if (be.hasLoadedDye() && AspectAlchemyMod.DYEABLE_ITEMS.contains(stack.getItem())) {
                DyeColor color = be.getLoadedDyeColor();
                String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
                int maxCount = (itemId.endsWith("_banner") || itemId.endsWith("_carpet")) ? 64 : 16;
                int toDye = Math.min(stack.getCount(), maxCount);

                if (level.getBlockState(pos).getBlock() instanceof AspectCauldronBlock block) {
                    Item dyedItem = block.getDyedItem(stack.getItem(), color);
                    if (dyedItem != Items.AIR) {
                        ItemStack result = new ItemStack(dyedItem, toDye);
                        stack.shrink(toDye);
                        if (stack.isEmpty()) itemEntity.discard();
                        else itemEntity.setItem(stack);
                        ItemEntity output = new ItemEntity(level,
                                itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), result);
                        output.setDeltaMovement(0, 0.1, 0);
                        level.addFreshEntity(output);
                        be.clearLoadedDye();
                        block.decreaseCauldronLevel(state, level, pos, be);
                        level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 0.8f, 1.0f);
                    }
                }
            }

            // Стрелы
            if (stack.is(Items.ARROW) && be.canTipArrows()) {
                int tipped = be.tipArrows(stack.getCount());
                if (tipped > 0) {
                    ItemStack tippedStack = be.createTippedArrowStack(tipped);
                    stack.shrink(tipped);
                    if (stack.isEmpty()) itemEntity.discard();
                    else itemEntity.setItem(stack);
                    ItemEntity output = new ItemEntity(level,
                            itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), tippedStack);
                    output.setDeltaMovement(0, 0.1, 0);
                    level.addFreshEntity(output);
                    if (be.consumePendingLevelDecrease()) {
                        if (level.getBlockState(pos).getBlock() instanceof AspectCauldronBlock block) {
                            block.decreaseCauldronLevel(state, level, pos, be);
                        }
                    }
                    level.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.5f, 1.2f);
                    continue;
                }
            }
        }
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        tippedArrowsUsed = tag.getInt("TippedArrowsUsed");
        for (int i = 0; i < inventory.size(); i++) inventory.set(i, ItemStack.EMPTY);
        ListTag list = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag slot = list.getCompound(i);
            int slotIdx = slot.getByte("Slot");
            if (slotIdx >= 0 && slotIdx < inventory.size()) {
                inventory.set(slotIdx, ItemStack.parseOptional(registries, slot));
            }
        }
        if (tag.contains("LoadedDye", Tag.TAG_COMPOUND)) {
            loadedDye = ItemStack.parseOptional(registries, tag.getCompound("LoadedDye"));
        } else {
            loadedDye = ItemStack.EMPTY;
        }
        heatTicks = tag.getInt("HeatTicks");
        pendingLevelDecrease = tag.getBoolean("PendingLevelDecrease");
        cachedEffects = null;
        cachedWaterColor = null;
        if (getLevel() != null) {
            getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("TippedArrowsUsed", tippedArrowsUsed);
        tag.putInt("HeatTicks", heatTicks);
        tag.putBoolean("PendingLevelDecrease", pendingLevelDecrease);
        ListTag list = new ListTag();
        for (int i = 0; i < inventory.size(); i++) {
            if (!inventory.get(i).isEmpty()) {
                Tag itemTag = inventory.get(i).saveOptional(registries);
                if (itemTag instanceof CompoundTag slot) {
                    slot.putByte("Slot", (byte) i);
                    list.add(slot);
                }
            }
        }
        tag.put("Items", list);
        if (!loadedDye.isEmpty()) {
            Tag dyeTag = loadedDye.saveOptional(registries);
            if (dyeTag != null) {
                tag.put("LoadedDye", dyeTag);
            }
        }
    }

    // ── Тик нагрева ──────────────────────────────────────────────────────────

    public boolean tickHeat(boolean hasHeatBelow) {
        if (hasHeatBelow) {
            if (heatTicks < BOIL_TICKS) {
                heatTicks++;
                if (heatTicks == BOIL_TICKS) {
                    setChanged();
                    return true;
                }
            }
        } else {
            if (heatTicks > 0) {
                heatTicks = 0;
                setChanged();
                return true;
            }
        }
        return false;
    }

    public boolean isBoiling() {
        return heatTicks >= BOIL_TICKS;
    }

    // ── Инвентарь ─────────────────────────────────────────────────────────────

    public boolean canAddIngredient() { return getIngredientCount() < 3; }

    public int getIngredientCount() {
        int count = 0;
        for (ItemStack stack : inventory) if (!stack.isEmpty()) count++;
        return count;
    }

    public boolean addIngredient(ItemStack stack) {
        if (stack.isEmpty() || !canAddIngredient()) return false;
        for (ItemStack existing : inventory) {
            if (!existing.isEmpty() && existing.is(stack.getItem())) return false;
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
        if (!loadedDye.isEmpty() || !(stack.getItem() instanceof DyeItem)) return false;
        loadedDye = stack.copyWithCount(1);
        invalidateAndSync();
        return true;
    }

    public boolean hasLoadedDye() { return !loadedDye.isEmpty(); }

    public DyeColor getLoadedDyeColor() {
        return loadedDye.isEmpty() ? null : ((DyeItem) loadedDye.getItem()).getDyeColor();
    }

    public void clearLoadedDye() { loadedDye = ItemStack.EMPTY; invalidateAndSync(); }

    public List<ItemStack> getInventory() { return inventory; }

    // ── Кэш и синхронизация ──────────────────────────────────────────────────

    private void invalidateAndSync() {
        cachedEffects = null;
        cachedWaterColor = null;
        super.setChanged();
        if (getLevel() != null && !getLevel().isClientSide) {
            getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
    }

    // ── Эффекты ───────────────────────────────────────────────────────────────

    private void ensureEffectsCached() {
        if (cachedEffects != null) return;

        Map<MobEffect, Integer> powerSums = new LinkedHashMap<>();
        for (ItemStack stack : inventory) {
            if (stack.isEmpty()) continue;
            List<AspectAlchemyData.AspectEntry> entries = AspectAlchemyData.ASPECT_MAP.get(stack.getItem());
            if (entries == null) continue;
            for (AspectAlchemyData.AspectEntry entry : entries) {
                powerSums.merge(entry.effect(), entry.power(), Integer::sum);
            }
        }

        List<MobEffectInstance> result = new ArrayList<>();
        for (Map.Entry<MobEffect, Integer> entry : powerSums.entrySet()) {
            int totalPower = entry.getValue();
            if (totalPower >= 2) {
                int amplifier = Math.min(totalPower - 2, 4);
                int duration = entry.getKey().isInstantenous() ? 1 : 3600;
                result.add(new MobEffectInstance(
                        BuiltInRegistries.MOB_EFFECT.wrapAsHolder(entry.getKey()),
                        duration, amplifier, false, true, true
                ));
            }
        }
        cachedEffects = result;
    }

    // ── Цвет воды ─────────────────────────────────────────────────────────────

    public int getWaterColor() {
        if (cachedWaterColor != null) return cachedWaterColor;

        if (!loadedDye.isEmpty() && loadedDye.getItem() instanceof DyeItem dyeItem) {
            cachedWaterColor = dyeItem.getDyeColor().getFireworkColor();
            return cachedWaterColor;
        }

        ensureEffectsCached();

        if (cachedEffects == null || cachedEffects.isEmpty()) {
            cachedWaterColor = VANILLA_WATER_COLOR;
            return cachedWaterColor;
        }

        long totalR = 0, totalG = 0, totalB = 0;
        int count = 0;
        for (MobEffectInstance instance : cachedEffects) {
            int color = instance.getEffect().value().getColor();
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

        if (cachedEffects == null || cachedEffects.isEmpty()) return bottle;

        PotionContents contents = new PotionContents(
                Optional.empty(),
                Optional.of(getWaterColor()),
                cachedEffects
        );
        bottle.set(DataComponents.POTION_CONTENTS, contents);

        if (targetPotionItem instanceof MultiUsePotionItem multiPotion) {
            multiPotion.setSipsLeft(bottle, 1);
        }

        List<MobEffectInstance> sorted = new ArrayList<>(cachedEffects);
        sorted.sort(
                Comparator.comparingInt(MobEffectInstance::getAmplifier).reversed()
                        .thenComparingInt(e -> -AspectAlchemyData.NAMING_WEIGHT.getOrDefault(e.getEffect().value(), 0))
        );

        List<MobEffectInstance> primary = new ArrayList<>();
        int maxAmplifier = sorted.get(0).getAmplifier();
        for (MobEffectInstance eff : sorted) {
            if (eff.getAmplifier() == maxAmplifier) primary.add(eff);
            else break;
        }

        boolean healingDominant = primary.stream().anyMatch(e ->
                e.getEffect().value() == MobEffects.HEAL ||
                        e.getEffect().value() == MobEffects.REGENERATION
        );

        if (healingDominant) {
            List<MobEffectInstance> adjusted = new ArrayList<>();
            for (MobEffectInstance eff : cachedEffects) {
                MobEffect type = eff.getEffect().value();
                boolean isSideEffect = type == MobEffects.MOVEMENT_SLOWDOWN || type == MobEffects.WEAKNESS;
                if (isSideEffect) {
                    adjusted.add(new MobEffectInstance(
                            eff.getEffect(), 600, eff.getAmplifier(), false, false, true
                    ));
                } else {
                    adjusted.add(eff);
                }
            }
            cachedEffects = adjusted;
            PotionContents adjusted_contents = new PotionContents(
                    Optional.empty(), Optional.of(getWaterColor()), cachedEffects
            );
            bottle.set(DataComponents.POTION_CONTENTS, adjusted_contents);
        }

        bottle.set(DataComponents.CUSTOM_NAME, buildPotionName(primary));

        if (getLevel() != null && !getLevel().isClientSide) {
            ServerPlayer player = (ServerPlayer) getLevel().getNearestPlayer(
                    getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), 3.0, false);
            if (player != null) {
                checkAndUnlockAspects(player, cachedEffects);
            }
        }

        return bottle;
    }

    // ── Имя зелья ─────────────────────────────────────────────────────────────

    private MutableComponent buildPotionName(List<MobEffectInstance> primary) {
        boolean hasBeneficial = primary.stream().anyMatch(e -> e.getEffect().value().isBeneficial());
        String prefixKey = hasBeneficial
                ? "item.aspectalchemy.potion.prefix.beneficial"
                : "item.aspectalchemy.potion.prefix.harmful";

        ChatFormatting vanillaColor = ChatFormatting.BLUE;

        MutableComponent name = Component.translatable(prefixKey)
                .append(Component.literal(" "))
                .withStyle(vanillaColor);

        for (int i = 0; i < primary.size(); i++) {
            MobEffectInstance eff = primary.get(i);
            String originalKey = eff.getEffect().value().getDescriptionId();
            String declinedKey = originalKey.contains("minecraft")
                    ? originalKey.replace("effect.minecraft.", "effect.aspectalchemy.declined.")
                    : "effect.aspectalchemy.declined." + originalKey.substring(originalKey.lastIndexOf('.') + 1);

            name.append(Component.translatable(declinedKey));
            if (eff.getAmplifier() > 0) {
                name.append(Component.literal(" " + toRoman(eff.getAmplifier() + 1)));
            }
            if (i < primary.size() - 1) {
                name.append(Component.literal(" "))
                        .append(Component.translatable("item.aspectalchemy.potion.conjunction"))
                        .append(Component.literal(" "));
            }
        }
        return name.withStyle(style -> style.withItalic(false).withColor(vanillaColor));
    }

    // ── Имя стрелы ────────────────────────────────────────────────────────────

    private MutableComponent buildArrowName() {
        ensureEffectsCached();
        List<MobEffectInstance> sorted = new ArrayList<>(cachedEffects);
        sorted.sort(
                Comparator.comparingInt(MobEffectInstance::getAmplifier).reversed()
                        .thenComparingInt(e -> -AspectAlchemyData.NAMING_WEIGHT.getOrDefault(e.getEffect().value(), 0))
        );

        List<MobEffectInstance> primary = new ArrayList<>();
        int maxAmplifier = sorted.get(0).getAmplifier();
        for (MobEffectInstance eff : sorted) {
            if (eff.getAmplifier() == maxAmplifier) primary.add(eff);
            else break;
        }

        boolean hasBeneficial = primary.stream().anyMatch(e -> e.getEffect().value().isBeneficial());
        String prefixKey = hasBeneficial
                ? "item.aspectalchemy.arrow.prefix.beneficial"
                : "item.aspectalchemy.arrow.prefix.harmful";

        ChatFormatting color = ChatFormatting.RED;

        MutableComponent name = Component.translatable(prefixKey)
                .append(Component.literal(" "))
                .withStyle(color);

        for (int i = 0; i < primary.size(); i++) {
            MobEffectInstance eff = primary.get(i);
            String originalKey = eff.getEffect().value().getDescriptionId();
            String declinedKey = originalKey.contains("minecraft")
                    ? originalKey.replace("effect.minecraft.", "effect.aspectalchemy.declined.")
                    : "effect.aspectalchemy.declined." + originalKey.substring(originalKey.lastIndexOf('.') + 1);

            name.append(Component.translatable(declinedKey));
            if (eff.getAmplifier() > 0) {
                name.append(Component.literal(" " + toRoman(eff.getAmplifier() + 1)));
            }
            if (i < primary.size() - 1) {
                name.append(Component.literal(" "))
                        .append(Component.translatable("item.aspectalchemy.potion.conjunction"))
                        .append(Component.literal(" "));
            }
        }
        return name.withStyle(style -> style.withItalic(false).withColor(color));
    }

    public static String toRoman(int n) {
        return switch (n) {
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(n);
        };
    }

    // ── Стрелы ────────────────────────────────────────────────────────────────

    public boolean canTipArrows() {
        return !getActiveEffects().isEmpty() && tippedArrowsUsed < ARROWS_PER_LEVEL;
    }

    public int getRemainingArrows() { return ARROWS_PER_LEVEL - tippedArrowsUsed; }

    public boolean consumePendingLevelDecrease() {
        if (pendingLevelDecrease) { pendingLevelDecrease = false; return true; }
        return false;
    }

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
        PotionContents contents = new PotionContents(
                Optional.empty(), Optional.of(getWaterColor()), cachedEffects
        );
        arrow.set(DataComponents.POTION_CONTENTS, contents);
        arrow.set(DataComponents.CUSTOM_NAME, buildArrowName());
        return arrow;
    }

    public List<MobEffectInstance> getActiveEffects() {
        ensureEffectsCached();
        return cachedEffects != null ? cachedEffects : Collections.emptyList();
    }

    // ── Достижения ────────────────────────────────────────────────────────────

    private void checkAndUnlockAspects(ServerPlayer player, List<MobEffectInstance> resultEffects) {
        for (ItemStack ingredient : inventory) {
            if (ingredient.isEmpty()) continue;
            Item item = ingredient.getItem();
            List<AspectAlchemyData.AspectEntry> entries = AspectAlchemyData.ASPECT_MAP.get(item);
            if (entries == null) continue;

            String itemId = BuiltInRegistries.ITEM.getKey(item).getPath();

            for (int i = 0; i < entries.size(); i++) {
                MobEffect aspect = entries.get(i).effect();
                ResourceLocation advId = ResourceLocation.fromNamespaceAndPath(
                        "aspectalchemy", "discovery/" + itemId + "_" + i);

                boolean effectPresent = resultEffects.stream()
                        .anyMatch(e -> e.getEffect().value() == aspect);

                if (effectPresent) {
                    grantAdvancement(player, advId);
                    if (i + 1 < entries.size()) {
                        grantAdvancement(player, ResourceLocation.fromNamespaceAndPath(
                                "aspectalchemy", "discovery/" + itemId + "_" + (i + 1)));
                    }
                }
            }
        }
    }

    private void grantAdvancement(ServerPlayer player, ResourceLocation id) {
        AdvancementHolder advancement = player.getServer().getAdvancements().get(id);
        if (advancement != null) {
            AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
            if (!progress.isDone()) {
                for (String criterion : progress.getRemainingCriteria()) {
                    player.getAdvancements().award(advancement, criterion);
                }
            }
        }
    }
    public ItemStack getLoadedDye() {
        // Верни ItemStack с красителем, который загружен в котёл
        // Например, из поля loadedDye
        return this.loadedDye != null ? this.loadedDye : ItemStack.EMPTY;
    }
}