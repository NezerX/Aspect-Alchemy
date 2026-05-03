package nezerx.aspectalchemy.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.resources.ResourceLocation;
import vazkii.patchouli.api.PatchouliAPI;

public class AlchemistsGuideItem extends Item {

    public AlchemistsGuideItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ResourceLocation bookId = ResourceLocation.fromNamespaceAndPath("aspectalchemy", "alchemists_guide");

        if (level.isClientSide) {
            PatchouliAPI.get().openBookGUI(bookId);
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.consume(stack);
    }
}