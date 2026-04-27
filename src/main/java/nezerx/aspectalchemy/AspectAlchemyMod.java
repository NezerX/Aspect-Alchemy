package nezerx.aspectalchemy;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import nezerx.aspectalchemy.init.ModBlocks;
import nezerx.aspectalchemy.init.ModBlockEntities;
import nezerx.aspectalchemy.init.ModItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AspectAlchemyMod implements ModInitializer {
    public static final String MOD_ID = "aspectalchemy";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Set<Item> DYEABLE_ITEMS = new HashSet<>();
    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Aspect Alchemy!");

        // 1. Сначала регистрируем ВСЕ предметы/блоки
        ModBlocks.register();
        ModBlockEntities.register();
        ModItems.register();

        // 2. Только потом инициализируем вспомогательные структуры
        initDyeableItems(); // ← Теперь безопасно
    }
    // Безопасная инициализация через цикл — коротко и надёжно
    public static void initDyeableItems() {
        for (DyeColor color : DyeColor.values()) {
            String colorName = color.getName();

            // Шерсть
            safeAdd("minecraft", colorName + "_wool");
            // Терракота
            safeAdd("minecraft", colorName + "_terracotta");
            // Ковры
            safeAdd("minecraft", colorName + "_carpet");
            // Баннеры
            safeAdd("minecraft", colorName + "_banner");
            // Свечи
            safeAdd("minecraft", colorName + "_candle");
            // Шалкеровые коробки
            safeAdd("minecraft", colorName + "_shulker_box");
            safeAdd("minecraft", "candle");
        }

        // Если у тебя есть свои красящиеся предметы — добавь их тут:
        // DYEABLE_ITEMS.add(ModItems.YOUR_DYEABLE_ITEM);
    }

    // Вспомогательный метод: добавляет предмет, если он существует (защита от null)
    private static void safeAdd(String namespace, String path) {
        Item item = Registries.ITEM.get(new Identifier(namespace, path));
        if (item != null && item != Items.AIR) {
            DYEABLE_ITEMS.add(item);
        }
    }

}