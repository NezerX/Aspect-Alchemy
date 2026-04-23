package nezerx.aspectalchemy.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nezerx.aspectalchemy.init.ModBlocks;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class AspectEmptyCauldronBlock extends Block {

    private static final VoxelShape OUTLINE_SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    public AspectEmptyCauldronBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {

        if (!(context instanceof EntityShapeContext)) {
            return VoxelShapes.fullCube();
        }

        VoxelShape bottom = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 3.0, 14.0);

        VoxelShape north = Block.createCuboidShape(2.0, 3.0, 0.0, 14.0, 16.0, 2.0);   // z: 0->2
        VoxelShape south = Block.createCuboidShape(2.0, 3.0, 14.0, 14.0, 16.0, 16.0); // z: 14->16
        VoxelShape west  = Block.createCuboidShape(0.0, 3.0, 2.0, 2.0, 16.0, 14.0);   // x: 0->2
        VoxelShape east  = Block.createCuboidShape(14.0, 3.0, 2.0, 16.0, 16.0, 14.0); // x: 14->16

        return VoxelShapes.union(bottom, north, south, west, east);
    }


    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack handStack = player.getStackInHand(hand);
        if (world.isClient) return ActionResult.SUCCESS;

        if (handStack.isOf(Items.WATER_BUCKET)) {
            world.setBlockState(pos, ModBlocks.ASPECT_CAULDRON.getDefaultState().with(AspectCauldronBlock.LEVEL, 3));
            world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
            if (!player.getAbilities().creativeMode) player.setStackInHand(hand, new ItemStack(Items.BUCKET));
            return ActionResult.CONSUME;
        }

        if (handStack.isOf(Items.POTION) && PotionUtil.getPotion(handStack) == Potions.WATER) {
            world.setBlockState(pos, ModBlocks.ASPECT_CAULDRON.getDefaultState().with(AspectCauldronBlock.LEVEL, 1));
            world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
            if (!player.getAbilities().creativeMode) {
                handStack.decrement(1);
                player.giveItemStack(new ItemStack(Items.GLASS_BOTTLE));
            }
            return ActionResult.CONSUME;
        }

        return ActionResult.PASS;
    }
}