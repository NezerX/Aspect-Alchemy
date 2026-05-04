package nezerx.aspectalchemy.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import nezerx.aspectalchemy.AspectAlchemyMod;
import nezerx.aspectalchemy.block.AspectCauldronBlock;
import nezerx.aspectalchemy.block.AspectEmptyCauldronBlock;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(AspectAlchemyMod.MOD_ID, Registries.BLOCK);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(AspectAlchemyMod.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<Block> ASPECT_EMPTY_CAULDRON = registerBlock("aspect_empty_cauldron",
            () -> new AspectEmptyCauldronBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON)
                    .noOcclusion()
                    .strength(2.0f, 3.0f)
                    .requiresCorrectToolForDrops())
    );

    public static final RegistrySupplier<Block> ASPECT_CAULDRON = registerBlock("aspect_cauldron",
            () -> new AspectCauldronBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON)
                    .noOcclusion()
                    .strength(2.0f, 3.0f)
                    .requiresCorrectToolForDrops())
    );

    private static RegistrySupplier<Block> registerBlock(String name, Supplier<Block> blockSupplier) {
        RegistrySupplier<Block> block = BLOCKS.register(name, blockSupplier);
        Item.Properties props = name.equals("aspect_empty_cauldron")
                ? new Item.Properties().arch$tab(net.minecraft.world.item.CreativeModeTabs.FUNCTIONAL_BLOCKS)
                : new Item.Properties();
        ITEMS.register(name, () -> new BlockItem(block.get(), props));
        return block;
    }

    public static void register() {
        BLOCKS.register();
        ITEMS.register();
    }
}