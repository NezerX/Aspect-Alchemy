package nezerx.aspectalchemy.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.particles.ParticleTypes;

import nezerx.aspectalchemy.AspectAlchemyMod;
import nezerx.aspectalchemy.block.entity.AspectCauldronBlockEntity;
import nezerx.aspectalchemy.data.AspectAlchemyData;
import nezerx.aspectalchemy.init.ModBlockEntities;
import nezerx.aspectalchemy.init.ModBlocks;
import nezerx.aspectalchemy.init.ModItems;
import nezerx.aspectalchemy.item.MultiUsePotionItem;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AspectCauldronBlock extends Block implements EntityBlock {
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 1, 3);
    public static final BooleanProperty BOILING = BooleanProperty.create("boiling");

    private static final Component MSG_NOT_BOILING = Component.translatable("block.aspectalchemy.cauldron.not_boiling");
    private static final Component MSG_EMPTY = Component.translatable("block.aspectalchemy.cauldron.empty");
    private static final Component MSG_FULL = Component.translatable("block.aspectalchemy.cauldron.full");

    private static final VoxelShape OUTLINE_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
    private static final VoxelShape COLLISION_SHAPE;

    static {
        VoxelShape bottom = Block.box(2.0, 0.0, 2.0, 14.0, 3.0, 14.0);
        VoxelShape north = Block.box(2.0, 3.0, 0.0, 14.0, 16.0, 2.0);
        VoxelShape south = Block.box(2.0, 3.0, 14.0, 14.0, 16.0, 16.0);
        VoxelShape west = Block.box(0.0, 3.0, 2.0, 2.0, 16.0, 14.0);
        VoxelShape east = Block.box(14.0, 3.0, 2.0, 16.0, 16.0, 14.0);
        COLLISION_SHAPE = Shapes.or(bottom, north, south, west, east);
    }

    public AspectCauldronBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(LEVEL, 1)
                .setValue(BOILING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL, BOILING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AspectCauldronBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.ASPECT_CAULDRON.get()) return null;
        if (level.isClientSide) return (l, p, s, be) -> tickParticles(l, p, s, (AspectCauldronBlockEntity) be);
        return (l, p, s, be) -> AspectCauldronBlockEntity.tick(l, p, s, (AspectCauldronBlockEntity) be);
    }

    private static void tickParticles(Level level, BlockPos pos, BlockState state, AspectCauldronBlockEntity be) {
        if (!state.getValue(BOILING)) return;
        RandomSource random = level.getRandom();
        if (random.nextInt(3) == 0) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
            double y = pos.getY() + 0.5 + random.nextDouble() * 0.4;
            level.addParticle(ParticleTypes.BUBBLE_POP, x, y, z, 0, 0.04, 0);
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof ItemEntity item) {
            item.setDeltaMovement(item.getDeltaMovement().multiply(0.5, 0.8, 0.5));
            return;
        }
        if (level.isClientSide) return;

        if (level.getBlockEntity(pos) instanceof AspectCauldronBlockEntity cauldron && entity instanceof LivingEntity living) {
            if (living.isOnFire()) living.clearFire();
            if (state.getValue(BOILING) && level.getGameTime() % 20 == 0) {
                entity.hurt(level.damageSources().hotFloor(), 1.0F);
            }
            if (level.getGameTime() % 20 == 0 && cauldron.getIngredientCount() > 0) {
                for (MobEffectInstance effect : cauldron.getActiveEffects()) {
                    living.addEffect(new MobEffectInstance(effect.getEffect(), 60, effect.getAmplifier(), false, false, true));
                }
            }
        }
    }

    public void decreaseCauldronLevel(BlockState state, Level level, BlockPos pos, AspectCauldronBlockEntity cauldron) {
        int currentLevel = state.getValue(LEVEL);
        if (currentLevel > 1) {
            level.setBlock(pos, state.setValue(LEVEL, currentLevel - 1), 3);
        } else {
            cauldron.clearInventory();
            // Исправлено: defaultBlockState() вместо getDefaultState()
            level.setBlock(pos, ModBlocks.ASPECT_EMPTY_CAULDRON.get().defaultBlockState(), 3);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof AspectCauldronBlockEntity cauldron)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        Item item = stack.getItem();

        // 1. Мультибутылки (со сравнением эффектов)
        if (item instanceof MultiUsePotionItem multiItem) {
            if (cauldron.getIngredientCount() == 0) {
                player.displayClientMessage(MSG_EMPTY, true);
                return ItemInteractionResult.SUCCESS;
            }

            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            boolean isEmpty = contents == null || (contents.potion().isEmpty() && contents.customEffects().isEmpty());

            if (isEmpty) {
                if (!level.isClientSide) {
                    ItemStack result = cauldron.createPotionStack(item);
                    stack.consume(1, player);
                    giveItem(player, hand, result);
                    decreaseCauldronLevel(state, level, pos, cauldron);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            } else {
                if (!isPotionMatching(stack, cauldron)) {
                    player.displayClientMessage(Component.translatable("block.aspectalchemy.cauldron.potion_mismatch"), true);
                    return ItemInteractionResult.SUCCESS;
                }
                if (multiItem.getSipsLeft(stack) >= multiItem.getMaxSips()) {
                    player.displayClientMessage(Component.translatable("block.aspectalchemy.cauldron.bottle_full"), true);
                    return ItemInteractionResult.SUCCESS;
                }
                if (!level.isClientSide) {
                    multiItem.setSipsLeft(stack, multiItem.getSipsLeft(stack) + 1);
                    decreaseCauldronLevel(state, level, pos, cauldron);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        // 2. Обычные бутылки
        Item resultItem = getBottleResult(item);
        if (resultItem != null) {
            if (cauldron.getIngredientCount() == 0) {
                player.displayClientMessage(MSG_EMPTY, true);
                return ItemInteractionResult.SUCCESS;
            }
            if (!level.isClientSide) {
                ItemStack potion = cauldron.createPotionStack(resultItem);
                stack.consume(1, player);
                giveItem(player, hand, potion);
                decreaseCauldronLevel(state, level, pos, cauldron);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        // 3. Ингредиенты
        if (AspectAlchemyData.ASPECT_MAP.containsKey(item)) {
            if (!cauldron.isBoiling()) {
                player.displayClientMessage(MSG_NOT_BOILING, true);
                return ItemInteractionResult.SUCCESS;
            }
            if (!cauldron.canAddIngredient()) {
                player.displayClientMessage(MSG_FULL, true);
                return ItemInteractionResult.SUCCESS;
            }
            if (!level.isClientSide && cauldron.addIngredient(stack)) {
                stack.consume(1, player);
                level.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.5f, 1.0f);
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
            return ItemInteractionResult.SUCCESS;
        }

        // 4. Стрелы (Tipped Arrows)
        if (item == Items.ARROW) {
            if (cauldron.getIngredientCount() == 0) {
                player.displayClientMessage(MSG_EMPTY, true);
                return ItemInteractionResult.SUCCESS;
            }
            if (!level.isClientSide) {
                int count = stack.getCount();
                int tippedCount = cauldron.tipArrows(count);
                ItemStack tippedArrows = new ItemStack(Items.TIPPED_ARROW, tippedCount);

                List<MobEffectInstance> effects = cauldron.getActiveEffects();
                tippedArrows.set(DataComponents.POTION_CONTENTS, new PotionContents(java.util.Optional.empty(), java.util.Optional.of(cauldron.getWaterColor()), effects));

                stack.consume(tippedCount, player);
                giveItem(player, hand, tippedArrows);
                if (cauldron.consumePendingLevelDecrease()) {
                    decreaseCauldronLevel(state, level, pos, cauldron);
                }
                level.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.4f, 1.2f);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        // 5. Покраска предметов
        if (item instanceof DyeItem dye) {
            if (cauldron.hasLoadedDye()) {
                player.displayClientMessage(Component.translatable("block.aspectalchemy.cauldron.dye_loaded"), true);
                return ItemInteractionResult.SUCCESS;
            }
            if (!level.isClientSide) {
                cauldron.loadDye(stack);
                stack.consume(1, player);
                level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        if (AspectAlchemyMod.DYEABLE_ITEMS.contains(item) && cauldron.hasLoadedDye()) {
            if (!level.isClientSide) {
                DyeColor color = cauldron.getLoadedDyeColor();
                int toDye = Math.min(stack.getCount(), 16);
                ItemStack dyedStack = new ItemStack(getDyedItem(item, color), toDye);
                stack.consume(toDye, player);
                giveItem(player, hand, dyedStack);
                cauldron.clearLoadedDye();
                decreaseCauldronLevel(state, level, pos, cauldron);
                level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private void giveItem(Player player, InteractionHand hand, ItemStack stack) {
        if (player.getItemInHand(hand).isEmpty()) player.setItemInHand(hand, stack);
        else if (!player.getInventory().add(stack)) player.drop(stack, false);
    }

    private boolean isPotionMatching(ItemStack stack, AspectCauldronBlockEntity cauldron) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) return false;
        List<MobEffectInstance> stackEffects = contents.customEffects();
        List<MobEffectInstance> cauldronEffects = cauldron.getActiveEffects();
        if (stackEffects.size() != cauldronEffects.size()) return false;
        for (MobEffectInstance effect : cauldronEffects) {
            boolean found = false;
            for (MobEffectInstance sEffect : stackEffects) {
                if (sEffect.getEffect() == effect.getEffect() && sEffect.getAmplifier() == effect.getAmplifier()) {
                    found = true; break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    private Item getBottleResult(Item input) {
        if (input == Items.GLASS_BOTTLE) return Items.POTION;
        if (input == ModItems.GLASS_BOTTLE_SMALL.get()) return ModItems.POTION_SMALL.get();
        if (input == ModItems.GLASS_BOTTLE_MEDIUM.get()) return ModItems.POTION_MEDIUM.get();
        if (input == ModItems.GLASS_BOTTLE_LARGE.get()) return ModItems.POTION_LARGE.get();
        return null;
    }

    public Item getDyedItem(Item baseItem, DyeColor color) {
        String id = BuiltInRegistries.ITEM.getKey(baseItem).getPath();
        String prefix = color.getName();
        String targetId;
        if (id.endsWith("_wool")) targetId = prefix + "_wool";
        else if (id.endsWith("_terracotta")) targetId = prefix + "_terracotta";
        else if (id.endsWith("_shulker_box")) targetId = prefix + "_shulker_box";
        else if (id.endsWith("_banner")) targetId = prefix + "_banner";
        else if (id.endsWith("_carpet")) targetId = prefix + "_carpet";
        else if (id.equals("candle") || id.endsWith("_candle")) targetId = prefix + "_candle";
        else return baseItem;
        return BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(targetId));
    }
}