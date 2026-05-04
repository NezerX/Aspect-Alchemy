package nezerx.aspectalchemy.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import nezerx.aspectalchemy.AspectAlchemyMod;
import nezerx.aspectalchemy.item.AlchemistsGuideItem;
import nezerx.aspectalchemy.item.MultiUsePotionItem;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(AspectAlchemyMod.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<Item> GLASS_BOTTLE_SMALL = ITEMS.register("glass_bottle_small", () -> new Item(new Item.Properties().arch$tab(CreativeModeTabs.INGREDIENTS)));
    public static final RegistrySupplier<Item> GLASS_BOTTLE_MEDIUM = ITEMS.register("glass_bottle_medium", () -> new Item(new Item.Properties().arch$tab(CreativeModeTabs.INGREDIENTS)));
    public static final RegistrySupplier<Item> GLASS_BOTTLE_LARGE = ITEMS.register("glass_bottle_large", () -> new Item(new Item.Properties().arch$tab(CreativeModeTabs.INGREDIENTS)));

    public static final RegistrySupplier<Item> POTION_SMALL = ITEMS.register("potion_small", () -> new MultiUsePotionItem(new Item.Properties().stacksTo(1).arch$tab(CreativeModeTabs.FOOD_AND_DRINKS), 2, GLASS_BOTTLE_SMALL.get()));
    public static final RegistrySupplier<Item> POTION_MEDIUM = ITEMS.register("potion_medium", () -> new MultiUsePotionItem(new Item.Properties().stacksTo(1).arch$tab(CreativeModeTabs.FOOD_AND_DRINKS), 3, GLASS_BOTTLE_MEDIUM.get()));
    public static final RegistrySupplier<Item> POTION_LARGE = ITEMS.register("potion_large", () -> new MultiUsePotionItem(new Item.Properties().stacksTo(1).arch$tab(CreativeModeTabs.FOOD_AND_DRINKS), 4, GLASS_BOTTLE_LARGE.get()));

    public static final RegistrySupplier<Item> ALCHEMISTS_GUIDE = ITEMS.register("alchemists_guide",
            () -> new AlchemistsGuideItem(new Item.Properties()
                    .stacksTo(1)
                    .arch$tab(CreativeModeTabs.TOOLS_AND_UTILITIES)
            )
    );

    public static ItemStack createPotionStack(Item item, Holder<Potion> potion) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
        return stack;
    }

    public static void register() {
        ITEMS.register();
    }
}