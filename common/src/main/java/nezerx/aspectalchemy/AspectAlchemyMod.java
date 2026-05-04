package nezerx.aspectalchemy;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import nezerx.aspectalchemy.init.ModBlockEntities;
import nezerx.aspectalchemy.init.ModBlocks;
import nezerx.aspectalchemy.init.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

// УБРАЛИ implements ModInitializer
public class AspectAlchemyMod {
    public static final String MOD_ID = "aspectalchemy";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Set<Item> DYEABLE_ITEMS = new HashSet<>();

    // Переименовали в init и сделали static
    public static void init() {
        LOGGER.info("Initializing Aspect Alchemy!");
        ModBlocks.register();
        ModBlockEntities.register();
        ModItems.register();
        initDyeableItems();
    }

    public static void initDyeableItems() {
        for (DyeColor color : DyeColor.values()) {
            String colorName = color.getName();
            safeAdd("minecraft", colorName + "_wool");
            safeAdd("minecraft", colorName + "_terracotta");
            safeAdd("minecraft", colorName + "_carpet");
            safeAdd("minecraft", colorName + "_banner");
            safeAdd("minecraft", colorName + "_candle");
            safeAdd("minecraft", colorName + "_shulker_box");
        }
        safeAdd("minecraft", "candle");
    }

    private static void safeAdd(String namespace, String path) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path);
        Item item = BuiltInRegistries.ITEM.get(id); // В 1.21.1 BuiltInRegistries.ITEM.get возвращает Item напрямую
        if (item != Items.AIR) {
            DYEABLE_ITEMS.add(item);
        }
    }
}