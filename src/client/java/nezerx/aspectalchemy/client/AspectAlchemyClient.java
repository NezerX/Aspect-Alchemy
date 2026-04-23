package nezerx.aspectalchemy.client;

import net.minecraft.client.color.item.ItemColors;
import net.minecraft.potion.PotionUtil;
import nezerx.aspectalchemy.init.ModItems;
import nezerx.aspectalchemy.data.AspectAlchemyData;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.List;
import nezerx.aspectalchemy.block.entity.AspectCauldronBlockEntity;
import nezerx.aspectalchemy.init.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.render.RenderLayer;

public class AspectAlchemyClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (world != null && pos != null) {
                var be = world.getBlockEntity(pos);
                if (be instanceof AspectCauldronBlockEntity cauldron) {
                    return cauldron.getWaterColor();
                }
            }
            return 0x3F76E4;
        }, ModBlocks.ASPECT_CAULDRON);

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.ASPECT_CAULDRON, RenderLayer.getTranslucent());

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            return tintIndex > 0 ? -1 : PotionUtil.getColor(stack);
        }, ModItems.POTION_SMALL, ModItems.POTION_MEDIUM, ModItems.POTION_LARGE);

        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            List<StatusEffect> effects = AspectAlchemyData.ASPECT_MAP.get(stack.getItem());
            if (effects == null || effects.isEmpty()) return;

            lines.add(Text.empty());

            lines.add(Text.translatable("item.aspectalchemy.tooltip.aspects")
                    .formatted(Formatting.DARK_AQUA));

            for (StatusEffect effect : effects) {
                Formatting color = effect.isBeneficial() ? Formatting.AQUA : Formatting.RED;
                lines.add(Text.literal("  • ")
                        .formatted(Formatting.GRAY)
                        .append(Text.translatable(effect.getTranslationKey()).formatted(color))
                );
            }
        });
    }


}