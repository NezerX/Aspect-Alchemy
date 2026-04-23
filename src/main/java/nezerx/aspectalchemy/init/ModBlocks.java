package nezerx.aspectalchemy.init;

import nezerx.aspectalchemy.AspectAlchemyMod;
import nezerx.aspectalchemy.block.AspectCauldronBlock;
import nezerx.aspectalchemy.block.AspectEmptyCauldronBlock;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static final Block ASPECT_EMPTY_CAULDRON = new AspectEmptyCauldronBlock(
            FabricBlockSettings.copyOf(Blocks.CAULDRON).nonOpaque()
    );
    public static final Block ASPECT_CAULDRON = new AspectCauldronBlock(
            FabricBlockSettings.copyOf(Blocks.CAULDRON).nonOpaque()
    );

    private static Block registerBlock(String name, Block block) {
        Identifier id = new Identifier(AspectAlchemyMod.MOD_ID, name);

        Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));

        return Registry.register(Registries.BLOCK, id, block);
    }

    public static void register() {
        registerBlock("aspect_empty_cauldron", ASPECT_EMPTY_CAULDRON);
        registerBlock("aspect_cauldron", ASPECT_CAULDRON);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(ASPECT_EMPTY_CAULDRON);
        });
    }
}