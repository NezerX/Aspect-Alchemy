package nezerx.aspectalchemy.neoforge.client;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import nezerx.aspectalchemy.AspectAlchemyMod;
import nezerx.aspectalchemy.block.entity.AspectCauldronBlockEntity;
import nezerx.aspectalchemy.init.ModBlocks;
import nezerx.aspectalchemy.init.ModItems;

@EventBusSubscriber(modid = AspectAlchemyMod.MOD_ID, value = Dist.CLIENT)
public final class AspectAlchemyNeoForgeClient {

    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, world, pos, tintIndex) -> {
            if (world != null && pos != null) {
                var be = world.getBlockEntity(pos);
                if (be instanceof AspectCauldronBlockEntity cauldron) {
                    return cauldron.getWaterColor();
                }
            }
            return 0x3F76E4;
        }, ModBlocks.ASPECT_CAULDRON.get());
    }

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) ->
                        tintIndex > 0 ? -1 : getPotionColor(stack),
                ModItems.POTION_SMALL.get(),
                ModItems.POTION_MEDIUM.get(),
                ModItems.POTION_LARGE.get()
        );
    }

    private static int getPotionColor(ItemStack stack) {
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        return contents.getColor() | 0xDD000000;
    }
}