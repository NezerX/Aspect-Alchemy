package nezerx.aspectalchemy.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.resources.ResourceLocation;
import nezerx.aspectalchemy.AspectAlchemyMod;
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
            try {
                var api = PatchouliAPI.get();
                AspectAlchemyMod.LOGGER.info("[AA] API stub? {}", api.isStub());
                try {
                    var subtitle = api.getSubtitle(bookId);
                    AspectAlchemyMod.LOGGER.info("[AA] Book found! Subtitle: {}", subtitle);
                } catch (IllegalArgumentException e) {
                    AspectAlchemyMod.LOGGER.error("[AA] Book NOT found: {}", e.getMessage());
                }
                api.openBookGUI(bookId);
            } catch (Exception e) {
                AspectAlchemyMod.LOGGER.error("[AA] Failed!", e);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}