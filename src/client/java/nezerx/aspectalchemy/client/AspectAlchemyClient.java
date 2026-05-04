package nezerx.aspectalchemy.client;

import net.minecraft.client.MinecraftClient; // Добавь этот импорт
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

import static nezerx.aspectalchemy.block.entity.AspectCauldronBlockEntity.toRoman;

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

        // Регистрация тултипа с проверкой на креатив
        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            var player = MinecraftClient.getInstance().player;
            if (player == null || !player.isCreative()) return;

            List<AspectAlchemyData.AspectEntry> entries = AspectAlchemyData.ASPECT_MAP.get(stack.getItem());
            if (entries == null || entries.isEmpty()) return;

            lines.add(Text.empty());
            lines.add(Text.translatable("item.aspectalchemy.tooltip.aspects")
                    .formatted(Formatting.DARK_AQUA));

            for (AspectAlchemyData.AspectEntry entry : entries) {
                Formatting color = entry.effect().isBeneficial() ? Formatting.AQUA : Formatting.RED;
                String power = entry.power() > 1 ? " (" + toRoman(entry.power()) + ")" : "";
                lines.add(Text.literal("  • ")
                        .formatted(Formatting.GRAY)
                        .append(Text.translatable(entry.effect().getTranslationKey()).formatted(color))
                        .append(Text.literal(power).formatted(Formatting.GRAY))
                );
            }
        });
    }
}