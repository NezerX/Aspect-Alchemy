package nezerx.aspectalchemy.block;

import net.minecraft.registry.Registries;
import net.minecraft.item.DyeItem;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.entity.ItemEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import java.util.List;
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
import net.minecraft.potion.PotionUtil;
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
import java.util.ArrayList;
import nezerx.aspectalchemy.AspectAlchemyMod;

import java.util.Collections;

public class AspectCauldronBlock extends LeveledCauldronBlock implements BlockEntityProvider {
    public static final IntProperty LEVEL = IntProperty.of("level", 1, 3);
    public static final BooleanProperty BOILING = BooleanProperty.of("boiling");
    private static final Text MSG_EMPTY = Text.translatable("block.aspectalchemy.cauldron.empty");
    private static final Text MSG_FULL = Text.translatable("block.aspectalchemy.cauldron.full");
    private static final Text MSG_NOT_BOILING = Text.translatable("block.aspectalchemy.cauldron.not_boiling");

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

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient && !player.isCreative()) {
            Block.dropStack(world, pos, new ItemStack(ModBlocks.ASPECT_EMPTY_CAULDRON));
        }
        super.onBreak(world, pos, state, player);
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
                (w, p, s, b) -> AspectCauldronBlockEntity.tick(w, p, s, b);
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
        // Тормозим предмет физически — это нормально оставить
        if (entity instanceof ItemEntity item) {
            item.setVelocity(item.getVelocity().multiply(0.5, 0.8, 0.5));
            return; // и сразу выходим, tick() сам разберётся
        }

        if (world.isClient) return;

        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof AspectCauldronBlockEntity cauldron)) return;

        if (!(entity instanceof LivingEntity living)) return;

        if (living.isOnFire()) living.extinguish();

        if (state.get(BOILING) && !living.isFireImmune()
                && !EnchantmentHelper.hasFrostWalker(living)
                && world.getTime() % 20 == 0) {
            entity.damage(world.getDamageSources().hotFloor(), 1.0F);
        }

        if (world.getTime() % 20 != 0) return;
        if (cauldron.getIngredientCount() == 0) return;

        for (StatusEffectInstance effect : cauldron.getActiveEffects()) {
            living.addStatusEffect(new StatusEffectInstance(
                    effect.getEffectType(), 60, effect.getAmplifier(),
                    false, false, true
            ));
        }
    }
    // ── Вспомогательный метод: уменьшить уровень котла ───────────────────────

    public void decreaseCauldronLevel(BlockState state, World world, BlockPos pos, AspectCauldronBlockEntity cauldron) {
        int level = state.get(LEVEL);
        if (level > 1) {
            world.setBlockState(pos, state.with(LEVEL, level - 1));
        } else {
            cauldron.clearInventory();
            world.setBlockState(pos, ModBlocks.ASPECT_EMPTY_CAULDRON.getDefaultState());
        }
    }

    // ── Вспомогательный метод: вывести сообщение игроку (action bar) ─────────
    private void sendMsg(World world, PlayerEntity player, String translationKey) {
        if (!world.isClient) {
            player.sendMessage(Text.translatable(translationKey), true);
        }
    }

    // ── Взаимодействие ────────────────────────────────────────────────────────

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        BlockEntity be = world.getBlockEntity(pos);

        if (!(be instanceof AspectCauldronBlockEntity cauldron)) return ActionResult.PASS;

        Item item = stack.getItem();

        // ── Мультибутылки ─────────────────────────────────────────────────────
        if (item instanceof nezerx.aspectalchemy.item.MultiUsePotionItem multiItem) {

            // Котёл пуст — зачерпнуть нечего
            if (cauldron.getIngredientCount() == 0) {
                sendMsg(world, player, "block.aspectalchemy.cauldron.empty");
                return ActionResult.CONSUME;
            }

            boolean isEmpty = !stack.hasNbt() || !stack.getOrCreateNbt().contains("CustomPotionEffects");

            if (isEmpty) {
                // Бутылка пуста — первое зачерпывание. isBoiling НЕ проверяется.
                // черпать можно и из некипящего котла.
                if (!world.isClient) {
                    ItemStack potionStack = cauldron.createPotionStack(item);
                    if (!player.getAbilities().creativeMode) stack.decrement(1);
                    if (stack.isEmpty()) player.setStackInHand(hand, potionStack);
                    else if (!player.getInventory().insertStack(potionStack)) player.dropItem(potionStack, false);
                    decreaseCauldronLevel(state, world, pos, cauldron);
                }
                return ActionResult.success(world.isClient);
            }

            // Бутылка уже содержит зелье — проверяем совместимость и наполненность
            int currentSips = multiItem.getSipsLeft(stack);

            if (currentSips >= multiItem.getMaxSips()) {
                // бутылка уже полна
                sendMsg(world, player, "block.aspectalchemy.cauldron.bottle_full");
                return ActionResult.CONSUME;
            }

            if (!isPotionMatching(stack, cauldron)) {
                // эффекты не совпадают
                sendMsg(world, player, "block.aspectalchemy.cauldron.potion_mismatch");
                return ActionResult.CONSUME;
            }

            // Все условия соблюдены — долив
            if (!world.isClient) {
                multiItem.setSipsLeft(stack, currentSips + 1);
                decreaseCauldronLevel(state, world, pos, cauldron);
            }
            return ActionResult.success(world.isClient);
        }

        // ── Обычные стеклянные бутылки ────────────────────────────────────────
        Item resultPotion = null;
        if (item == Items.GLASS_BOTTLE)                resultPotion = Items.POTION;
        else if (item == ModItems.GLASS_BOTTLE_SMALL)  resultPotion = ModItems.POTION_SMALL;
        else if (item == ModItems.GLASS_BOTTLE_MEDIUM) resultPotion = ModItems.POTION_MEDIUM;
        else if (item == ModItems.GLASS_BOTTLE_LARGE)  resultPotion = ModItems.POTION_LARGE;

        if (resultPotion != null) {
            //Котёл пуст — нечего черпать
            if (cauldron.getIngredientCount() == 0) {
                sendMsg(world, player, "block.aspectalchemy.cauldron.empty");
                return ActionResult.CONSUME;
            }

            if (!world.isClient) {
                ItemStack potionStack = cauldron.createPotionStack(resultPotion);
                if (!player.getAbilities().creativeMode) stack.decrement(1);
                if (stack.isEmpty()) player.setStackInHand(hand, potionStack);
                else if (!player.getInventory().insertStack(potionStack)) player.dropItem(potionStack, false);
                decreaseCauldronLevel(state, world, pos, cauldron);
            }
            return ActionResult.success(world.isClient);
        }

        // ── Ингредиенты ───────────────────────────────────────────────────────
        if (AspectAlchemyData.ASPECT_MAP.containsKey(item)) {

            // [ИЗМЕНЕНИЕ] Котёл не кипит — ингредиент не добавить
            if (!cauldron.isBoiling()) {
                player.sendMessage(MSG_NOT_BOILING, true); // вместо sendMsg(..., "key")
                return ActionResult.CONSUME;
            }

            if (!cauldron.canAddIngredient()) {
                // [ИЗМЕНЕНИЕ] Котёл переполнен ингредиентами
                sendMsg(world, player, "block.aspectalchemy.cauldron.full");
                return ActionResult.CONSUME;
            }

            // Все условия соблюдены — добавляем ингредиент
            if (!world.isClient) {
                if (cauldron.addIngredient(stack)) {
                    if (!player.getAbilities().creativeMode) stack.decrement(1);
                    world.playSound(null, pos, net.minecraft.sound.SoundEvents.ENTITY_GENERIC_SPLASH,
                            net.minecraft.sound.SoundCategory.BLOCKS, 0.5f, 1.0f);
                }
            }
            return ActionResult.success(world.isClient);
        }
        // ── Стрелы ───────────────────────────────────────────────────────────────
        if (item == Items.ARROW) {
            if (cauldron.getIngredientCount() == 0 || cauldron.getActiveEffects().isEmpty()) {
                sendMsg(world, player, "block.aspectalchemy.cauldron.empty");
                return ActionResult.CONSUME;
            }
            if (!cauldron.canTipArrows()) {
                sendMsg(world, player, "block.aspectalchemy.cauldron.arrows_depleted");
                return ActionResult.CONSUME;
            }

            if (!world.isClient) {
                int inHand = stack.getCount();
                int tipped = cauldron.tipArrows(inHand);

                // Убираем обычные стрелы
                if (!player.getAbilities().creativeMode) stack.decrement(tipped);

                // Выдаём стрелы с эффектом
                ItemStack tippedStack = new ItemStack(Items.TIPPED_ARROW, tipped);
                net.minecraft.potion.PotionUtil.setPotion(tippedStack, net.minecraft.potion.Potions.EMPTY);
                net.minecraft.potion.PotionUtil.setCustomPotionEffects(tippedStack, cauldron.getActiveEffects());
                tippedStack.getOrCreateNbt().putInt("CustomPotionColor", cauldron.getWaterColor());

                if (stack.isEmpty()) player.setStackInHand(hand, tippedStack);
                else if (!player.getInventory().insertStack(tippedStack))
                    player.dropItem(tippedStack, false);

                // Проверяем, нужно ли уменьшить уровень
                if (cauldron.consumePendingLevelDecrease()) {
                    decreaseCauldronLevel(state, world, pos, cauldron);
                }

                world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_SPLASH,
                        SoundCategory.BLOCKS, 0.4f, 1.2f);
            }
            return ActionResult.success(world.isClient);
        }
        // ── Красители: загрузка в котёл ─────────────────────────────────────
        if (item instanceof DyeItem) {
            if (cauldron.hasLoadedDye()) {
                sendMsg(world, player, "block.aspectalchemy.cauldron.dye_loaded");
                return ActionResult.CONSUME;
            }
            if (!world.isClient) {
                cauldron.loadDye(stack);
                if (!player.getAbilities().creativeMode) stack.decrement(1);

                world.playSound(null, pos, SoundEvents.ITEM_DYE_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
            }
            return ActionResult.success(world.isClient);
        }

        // ── Красители: окрашивание предметов ────────────────────────────────
        if (isDyeableItem(item) && cauldron.hasLoadedDye()) {
            if (!world.isClient) {
                DyeColor color = cauldron.getLoadedDyeColor();
                String itemId = Registries.ITEM.getId(item).getPath();
                int maxCount = (itemId.endsWith("_banner") || itemId.endsWith("_carpet")) ? 64 : 16;
                int toDye = Math.min(stack.getCount(), maxCount);

                Item dyedItem = getDyedItem(item, color);
                ItemStack result = new ItemStack(dyedItem, toDye);

                if (!player.getAbilities().creativeMode) stack.decrement(toDye);
                if (stack.isEmpty()) player.setStackInHand(hand, result);
                else if (!player.getInventory().insertStack(result)) player.dropItem(result, false);

                cauldron.clearLoadedDye();
                if (!world.isClient) decreaseCauldronLevel(state, world, pos, cauldron);
                world.playSound(null, pos, SoundEvents.ITEM_DYE_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
            }
            return ActionResult.success(world.isClient);
        }
        return ActionResult.PASS;
    }

    private boolean isPotionMatching(ItemStack stack, AspectCauldronBlockEntity cauldron) {
        List<StatusEffectInstance> potionEffects = PotionUtil.getCustomPotionEffects(stack);
        List<StatusEffectInstance> cauldronEffects = cauldron.getActiveEffects();

        if (potionEffects.size() != cauldronEffects.size()) return false;

        // Создаём копии, чтобы не ломать оригинальные списки
        ArrayList<StatusEffectInstance> list1 = new ArrayList<>(potionEffects);
        ArrayList<StatusEffectInstance> list2 = new ArrayList<>(cauldronEffects);

        // Сортируем по: 1) регистр-айди эффекта (строка), 2) амплифайер
        list1.sort((a, b) -> {
            String idA = Registries.STATUS_EFFECT.getId(a.getEffectType()).toString();
            String idB = Registries.STATUS_EFFECT.getId(b.getEffectType()).toString();
            int cmp = idA.compareTo(idB);
            return cmp != 0 ? cmp : Integer.compare(a.getAmplifier(), b.getAmplifier());
        });

        list2.sort((a, b) -> {
            String idA = Registries.STATUS_EFFECT.getId(a.getEffectType()).toString();
            String idB = Registries.STATUS_EFFECT.getId(b.getEffectType()).toString();
            int cmp = idA.compareTo(idB);
            return cmp != 0 ? cmp : Integer.compare(a.getAmplifier(), b.getAmplifier());
        });

        // Сравниваем поэлементно
        for (int i = 0; i < list1.size(); i++) {
            StatusEffectInstance p = list1.get(i);
            StatusEffectInstance c = list2.get(i);
            if (p.getEffectType() != c.getEffectType() || p.getAmplifier() != c.getAmplifier()) {
                return false;
            }
        }
        return true;
    }

    private boolean isDyeableItem(Item item) {
        return AspectAlchemyMod.DYEABLE_ITEMS.contains(item);
    }

    public Item getDyedItem(Item baseItem, DyeColor color) {
        String id = Registries.ITEM.getId(baseItem).getPath();
        String newId;

        if (id.endsWith("_wool")) newId = color.asString() + "_wool";
        else if (id.endsWith("_terracotta")) newId = color.asString() + "_terracotta";
        else if (id.endsWith("_shulker_box")) newId = color.asString() + "_shulker_box";
        else if (id.endsWith("_banner")) newId = color.asString() + "_banner";
        else if (id.endsWith("_carpet")) newId = color.asString() + "_carpet";
        else if (id.endsWith("_candle") || id.equals("candle")) newId = color.getName() + "_candle";
        //надо пофиксить: св
        else return baseItem;

        return getByName(newId);
    }

    private Item getByName(String name) {
        Identifier id = new Identifier("minecraft", name);
        Item item = Registries.ITEM.get(id);
        return item != null ? item : Items.AIR; // защита от null
    }
}