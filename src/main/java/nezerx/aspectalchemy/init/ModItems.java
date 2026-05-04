package nezerx.aspectalchemy.init;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import nezerx.aspectalchemy.AspectAlchemyMod;
import nezerx.aspectalchemy.item.AlchemistsGuideItem;
import nezerx.aspectalchemy.item.MultiUsePotionItem;

public class ModItems {

    // Пустые колбы
    public static final Item GLASS_BOTTLE_SMALL = registerItem("glass_bottle_small", new Item(new FabricItemSettings()));
    public static final Item GLASS_BOTTLE_MEDIUM = registerItem("glass_bottle_medium", new Item(new FabricItemSettings()));
    public static final Item GLASS_BOTTLE_LARGE = registerItem("glass_bottle_large", new Item(new FabricItemSettings()));

    // Зелья (используем MultiUsePotionItem)
    public static final Item POTION_SMALL = registerItem("potion_small", new MultiUsePotionItem(new FabricItemSettings().maxCount(1), 2, GLASS_BOTTLE_SMALL));
    public static final Item POTION_MEDIUM = registerItem("potion_medium", new MultiUsePotionItem(new FabricItemSettings().maxCount(1), 3, GLASS_BOTTLE_MEDIUM));
    public static final Item POTION_LARGE = registerItem("potion_large", new MultiUsePotionItem(new FabricItemSettings().maxCount(1), 4, GLASS_BOTTLE_LARGE));

    public static final Item ALCHEMISTS_GUIDE = registerItem("alchemists_guide", new AlchemistsGuideItem(new FabricItemSettings().maxCount(1)));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(AspectAlchemyMod.MOD_ID, name), item);
    }

    public static void register() {
        // Добавляем ингредиенты и книгу в соответствующую вкладку
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(GLASS_BOTTLE_SMALL);
            entries.add(GLASS_BOTTLE_MEDIUM);
            entries.add(GLASS_BOTTLE_LARGE);
            entries.add(ALCHEMISTS_GUIDE);
        });

        // Добавляем все варианты зелий во вкладку Еда и Напитки (там обычно лежат зелья)
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> {
            for (Potion potion : Registries.POTION) {
                // Мы не добавляем зелья без эффектов (Empty), если не нужно
                if (potion == net.minecraft.potion.Potions.EMPTY) continue;

                // Для каждого эффекта создаем три стака (Small, Medium, Large)
                entries.add(createPotionStack(POTION_SMALL, potion));
                entries.add(createPotionStack(POTION_MEDIUM, potion));
                entries.add(createPotionStack(POTION_LARGE, potion));
            }
        });
    }

    // Вспомогательный метод для создания стака зелья с эффектом
    private static ItemStack createPotionStack(Item item, Potion potion) {
        ItemStack stack = new ItemStack(item);
        PotionUtil.setPotion(stack, potion);
        return stack;
    }
}