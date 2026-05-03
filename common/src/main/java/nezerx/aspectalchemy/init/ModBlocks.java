package nezerx.aspectalchemy.init;

import nezerx.aspectalchemy.AspectAlchemyMod;
import nezerx.aspectalchemy.block.AspectCauldronBlock;
import nezerx.aspectalchemy.block.AspectEmptyCauldronBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {

    public static final Block ASPECT_EMPTY_CAULDRON = new AspectEmptyCauldronBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON)
                    .noOcclusion()
                    .strength(2.0f, 3.0f)
                    .requiresCorrectToolForDrops()
    );

    public static final Block ASPECT_CAULDRON = new AspectCauldronBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON)
                    .noOcclusion()
                    .strength(2.0f, 3.0f)
                    .requiresCorrectToolForDrops()
    );

    private static Block registerBlock(String name, Block block) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(AspectAlchemyMod.MOD_ID, name);
        Registry.register(BuiltInRegistries.ITEM, id, new BlockItem(block, new Item.Properties()));
        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    public static void register() {
        registerBlock("aspect_empty_cauldron", ASPECT_EMPTY_CAULDRON);
        registerBlock("aspect_cauldron", ASPECT_CAULDRON);
    }
}