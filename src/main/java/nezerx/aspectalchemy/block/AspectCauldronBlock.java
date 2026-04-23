package nezerx.aspectalchemy.block;

import java.util.List;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectInstance;
import nezerx.aspectalchemy.block.entity.AspectCauldronBlockEntity;
import nezerx.aspectalchemy.data.AspectAlchemyData;
import nezerx.aspectalchemy.init.ModBlocks;
import nezerx.aspectalchemy.init.ModItems;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.item.Item;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import nezerx.aspectalchemy.init.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public class AspectCauldronBlock extends LeveledCauldronBlock implements BlockEntityProvider {
    public static final IntProperty LEVEL = IntProperty.of("level", 1, 3);
    public static final BooleanProperty BOILING = BooleanProperty.of("boiling");

    private static final VoxelShape OUTLINE_SHAPE =
            Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    private static final VoxelShape COLLISION_SHAPE;
    static {
        VoxelShape bottom = Block.createCuboidShape(2.0,  0.0,  2.0, 14.0,  3.0, 14.0);
        VoxelShape north  = Block.createCuboidShape(2.0,  3.0,  0.0, 14.0, 16.0,  2.0);
        VoxelShape south  = Block.createCuboidShape(2.0,  3.0, 14.0, 14.0, 16.0, 16.0);
        VoxelShape west   = Block.createCuboidShape(0.0,  3.0,  2.0,  2.0, 16.0, 14.0);
        VoxelShape east   = Block.createCuboidShape(14.0, 3.0,  2.0, 16.0, 16.0, 14.0);
        COLLISION_SHAPE = VoxelShapes.union(bottom, north, south, west, east);
    }

    public AspectCauldronBlock(Settings settings) {
        super(settings, p -> false, Collections.emptyMap());
        setDefaultState(this.stateManager.getDefaultState().with(LEVEL, 1).with(BOILING, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LEVEL, BOILING);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return COLLISION_SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AspectCauldronBlockEntity(pos, state);
    }

    // ── Ticker ────────────────────────────────────────────────────────────────

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.ASPECT_CAULDRON) return null;
        if (world.isClient) {
            return (BlockEntityTicker<T>) (BlockEntityTicker<AspectCauldronBlockEntity>)
                    AspectCauldronBlock::tickParticles;
        }
        return (BlockEntityTicker<T>) (BlockEntityTicker<AspectCauldronBlockEntity>)
                AspectCauldronBlock::tickCauldron;
    }

    private static void tickParticles(World world, BlockPos pos, BlockState state, AspectCauldronBlockEntity be) {
        if (!state.get(BOILING)) return;

        net.minecraft.util.math.random.Random random = world.getRandom();

        if (random.nextInt(3) == 0) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
            double y = pos.getY() + 0.5 + random.nextDouble() * 0.4;
            world.addParticle(ParticleTypes.BUBBLE_POP, x, y, z,
                    (random.nextDouble() - 0.5) * 0.01,
                    0.03 + random.nextDouble() * 0.02,
                    (random.nextDouble() - 0.5) * 0.01);
        }
        if (random.nextInt(40) == 0) {
            world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    net.minecraft.sound.SoundEvents.BLOCK_LAVA_POP,
                    net.minecraft.sound.SoundCategory.BLOCKS,
                    0.1f + random.nextFloat() * 0.1f,
                    1.2f + random.nextFloat() * 0.2f,
                    false);
        }
    }

    private static void tickCauldron(World world, BlockPos pos, BlockState state, AspectCauldronBlockEntity be) {
        boolean hasHeat = isHeatSource(world.getBlockState(pos.down()));
        boolean changed = be.tickHeat(hasHeat);

        if (changed) {
            boolean shouldBoil = be.isBoiling();
            if (state.get(AspectCauldronBlock.BOILING) != shouldBoil) {
                world.setBlockState(pos, state.with(AspectCauldronBlock.BOILING, shouldBoil),
                        Block.NOTIFY_LISTENERS);
            }
        }
    }

    private static boolean isHeatSource(BlockState stateBelow) {
        return stateBelow.isOf(Blocks.CAMPFIRE)
                || stateBelow.isOf(Blocks.FIRE)
                || stateBelow.isOf(Blocks.SOUL_FIRE)
                || stateBelow.isOf(Blocks.LAVA)
                || stateBelow.isOf(Blocks.MAGMA_BLOCK);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState,
                                                WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (world.isClient) return;
        if (!(entity instanceof LivingEntity living)) return;

        if (living.isOnFire()) {
            living.extinguish();
        }

        // ── Урон от кипятка ──────────────────────────────────────────────────────────
        if (state.get(BOILING)
                && !living.isFireImmune()
                && !EnchantmentHelper.hasFrostWalker(living)
                && world.getTime() % 20 == 0) {
            entity.damage(world.getDamageSources().hotFloor(), 1.0F);
        }

        // ── Эффекты из ингредиентов ───────────────────────────────────────────────────
        if (world.getTime() % 20 != 0) return;

        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof AspectCauldronBlockEntity cauldron)) return;
        if (cauldron.getIngredientCount() == 0) return;

        for (StatusEffectInstance effect : cauldron.getActiveEffects()) {
            living.addStatusEffect(new StatusEffectInstance(
                    effect.getEffectType(),
                    60,
                    effect.getAmplifier(),
                    false, false, true
            ));
        }
    }


    // ── Взаимодействие ────────────────────────────────────────────────────────

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        BlockEntity be = world.getBlockEntity(pos);

        if (be instanceof AspectCauldronBlockEntity cauldron) {
            Item item = stack.getItem();

            // --- НОВАЯ ЛОГИКА: Долив в существующее зелье ---
            if (item instanceof nezerx.aspectalchemy.item.MultiUsePotionItem multiItem) {
                int currentSips = multiItem.getSipsLeft(stack);
                if (currentSips < multiItem.getMaxSips() && cauldron.isBoiling() && cauldron.getIngredientCount() > 0) {
                    // Проверка на совпадение эффектов (чтобы не смешивать разные зелья)
                    if (isPotionMatching(stack, cauldron)) {
                        if (!world.isClient) {
                            multiItem.setSipsLeft(stack, currentSips + 1);

                            // Тратим воду из котла
                            int level = state.get(LEVEL);
                            if (level > 1) {
                                world.setBlockState(pos, state.with(LEVEL, level - 1));
                            } else {
                                cauldron.clearInventory();
                                world.setBlockState(pos, nezerx.aspectalchemy.init.ModBlocks.ASPECT_EMPTY_CAULDRON.getDefaultState());
                            }
                        }
                        return ActionResult.success(world.isClient);
                    }
                }
            }

            Item resultPotion = null;
            if (item == Items.GLASS_BOTTLE) resultPotion = Items.POTION;
        /*    else if (item == ModItems.GLASS_BOTTLE_SMALL) resultPotion = ModItems.POTION_SMALL;
            else if (item == ModItems.GLASS_BOTTLE_MEDIUM) resultPotion = ModItems.POTION_MEDIUM;
            else if (item == ModItems.GLASS_BOTTLE_LARGE) resultPotion = ModItems.POTION_LARGE; */

            if (resultPotion != null) {
                if (cauldron.isBoiling() && cauldron.getIngredientCount() > 0) {
                    if (!world.isClient) {
                        ItemStack potionStack = cauldron.createPotionStack(resultPotion);
                        if (!player.getAbilities().creativeMode) stack.decrement(1);
                        if (stack.isEmpty()) player.setStackInHand(hand, potionStack);
                        else if (!player.getInventory().insertStack(potionStack)) player.dropItem(potionStack, false);

                        int currentLevel = state.get(LEVEL);
                        if (currentLevel > 1) {
                            world.setBlockState(pos, state.with(LEVEL, currentLevel - 1));
                        } else {
                            cauldron.clearInventory();
                            world.setBlockState(pos, nezerx.aspectalchemy.init.ModBlocks.ASPECT_EMPTY_CAULDRON.getDefaultState());
                        }
                    }
                    return ActionResult.success(world.isClient);
                }
                return ActionResult.CONSUME;
            }

            if (AspectAlchemyData.ASPECT_MAP.containsKey(item)) {
                // Добавлена проверка на кипение для ингредиентов
                if (!cauldron.isBoiling()) {
                    if (!world.isClient) player.sendMessage(Text.translatable("block.aspectalchemy.cauldron.not_boiling"), true);
                    return ActionResult.CONSUME;
                }

                if (cauldron.canAddIngredient()) {
                    if (!world.isClient) {
                        if (cauldron.addIngredient(stack)) {
                            if (!player.getAbilities().creativeMode) stack.decrement(1);
                            world.playSound(null, pos, net.minecraft.sound.SoundEvents.ENTITY_GENERIC_SPLASH, net.minecraft.sound.SoundCategory.BLOCKS, 0.5f, 1.0f);
                        }
                    }
                    return ActionResult.success(world.isClient);
                } else {
                    if (!world.isClient) player.sendMessage(Text.translatable("block.aspectalchemy.cauldron.full"), true);
                    return ActionResult.CONSUME;
                }
            }
        }
        return ActionResult.PASS;
    }

    private boolean isPotionMatching(ItemStack stack, AspectCauldronBlockEntity cauldron) {
        List<net.minecraft.entity.effect.StatusEffectInstance> potionEffects = net.minecraft.potion.PotionUtil.getCustomPotionEffects(stack);
        List<net.minecraft.entity.effect.StatusEffectInstance> cauldronEffects = cauldron.getActiveEffects();

        if (potionEffects.size() != cauldronEffects.size()) return false;

        // Сравниваем типы и усилители
        for (int i = 0; i < potionEffects.size(); i++) {
            var p = potionEffects.get(i);
            var c = cauldronEffects.get(i);
            if (p.getEffectType() != c.getEffectType() || p.getAmplifier() != c.getAmplifier()) {
                return false;
            }
        }
        return true;
    }
}