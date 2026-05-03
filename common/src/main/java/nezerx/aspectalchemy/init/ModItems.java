package nezerx.aspectalchemy.init;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import nezerx.aspectalchemy.AspectAlchemyMod;
import nezerx.aspectalchemy.item.AlchemistsGuideItem;
import nezerx.aspectalchemy.item.MultiUsePotionItem;

import java.util.Optional;

public class ModItems {

    public static final Item GLASS_BOTTLE_SMALL  = registerItem("glass_bottle_small",  new Item(new Item.Properties()));
    public static final Item GLASS_BOTTLE_MEDIUM = registerItem("glass_bottle_medium", new Item(new Item.Properties()));
    public static final Item GLASS_BOTTLE_LARGE  = registerItem("glass_bottle_large",  new Item(new Item.Properties()));

    public static final Item POTION_SMALL  = registerItem("potion_small",  new MultiUsePotionItem(new Item.Properties().stacksTo(1), 2, GLASS_BOTTLE_SMALL));
    public static final Item POTION_MEDIUM = registerItem("potion_medium", new MultiUsePotionItem(new Item.Properties().stacksTo(1), 3, GLASS_BOTTLE_MEDIUM));
    public static final Item POTION_LARGE  = registerItem("potion_large",  new MultiUsePotionItem(new Item.Properties().stacksTo(1), 4, GLASS_BOTTLE_LARGE));

    public static final Item ALCHEMISTS_GUIDE = registerItem("alchemists_guide", new AlchemistsGuideItem(new Item.Properties().stacksTo(1)));

    private static Item registerItem(String name, Item item) {
        // В 1.21.1 ResourceLocation создается через .fromNamespaceAndPath
        return Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(AspectAlchemyMod.MOD_ID, name), item);
    }

    // Раскомментировано и исправлено под систему компонентов 1.21.1
    public static ItemStack createPotionStack(Item item, Holder<Potion> potion) {
        ItemStack stack = new ItemStack(item);
        // Записываем зелье в компоненты предмета
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
        return stack;
    }

    public static void register() {
        // Метод вызывается в главном классе мода для инициализации статических полей
    }
}