package nezerx.aspectalchemy.block;

import nezerx.aspectalchemy.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AspectEmptyCauldronBlock extends Block {

    private static final VoxelShape OUTLINE_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    public AspectEmptyCauldronBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (!(context instanceof EntityCollisionContext)) {
            return Shapes.block();
        }
        VoxelShape bottom = Block.box(2.0,  0.0,  2.0, 14.0,  3.0, 14.0);
        VoxelShape north  = Block.box(2.0,  3.0,  0.0, 14.0, 16.0,  2.0);
        VoxelShape south  = Block.box(2.0,  3.0, 14.0, 14.0, 16.0, 16.0);
        VoxelShape west   = Block.box(0.0,  3.0,  2.0,  2.0, 16.0, 14.0);
        VoxelShape east   = Block.box(14.0, 3.0,  2.0, 16.0, 16.0, 14.0);
        return Shapes.or(bottom, north, south, west, east);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && !player.getAbilities().instabuild) {
            Block.popResource(level, pos, new ItemStack(ModBlocks.ASPECT_EMPTY_CAULDRON.asItem()));
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        ItemStack handStack = player.getMainHandItem();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        if (handStack.is(Items.WATER_BUCKET)) {
            level.setBlock(pos, ModBlocks.ASPECT_CAULDRON.defaultBlockState()
                    .setValue(AspectCauldronBlock.LEVEL, 3), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
            if (!player.getAbilities().instabuild) {
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
            }
        }

        // Проверяем водяное зелье через DataComponents
        if (handStack.is(Items.POTION)) {
            PotionContents contents = handStack.get(DataComponents.POTION_CONTENTS);
            if (contents != null && contents.is(Potions.WATER)) {
                level.setBlock(pos, ModBlocks.ASPECT_CAULDRON.defaultBlockState()
                        .setValue(AspectCauldronBlock.LEVEL, 1), Block.UPDATE_ALL);
                level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
                if (!player.getAbilities().instabuild) {
                    handStack.shrink(1);
                    player.addItem(new ItemStack(Items.GLASS_BOTTLE));
                }
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }
}