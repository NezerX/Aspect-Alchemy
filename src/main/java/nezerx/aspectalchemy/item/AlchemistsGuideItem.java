package nezerx.aspectalchemy.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import vazkii.patchouli.api.PatchouliAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlchemistsGuideItem extends Item {
    private static final Logger LOGGER = LoggerFactory.getLogger("aspectalchemy");

    public AlchemistsGuideItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        Identifier bookId = new Identifier("aspectalchemy", "alchemists_guide");

        if (world.isClient) {
            // Просто вызываем открытие. Проверки в логах мы уже прошли, Patchouli книгу видит.
            PatchouliAPI.get().openBookGUI(bookId);
            return TypedActionResult.success(stack);
        }

        // На сервере возвращаем CONSUME, чтобы остановить дальнейшую обработку клика
        return TypedActionResult.consume(stack);
    }
}