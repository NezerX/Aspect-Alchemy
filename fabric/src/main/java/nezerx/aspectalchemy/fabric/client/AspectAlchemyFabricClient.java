package nezerx.aspectalchemy.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import nezerx.aspectalchemy.block.entity.AspectCauldronBlockEntity;
import nezerx.aspectalchemy.data.AspectAlchemyData;
import nezerx.aspectalchemy.init.ModBlocks;
import nezerx.aspectalchemy.init.ModItems;

import java.util.List;

import static nezerx.aspectalchemy.block.entity.AspectCauldronBlockEntity.toRoman;

public final class AspectAlchemyFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.ASPECT_CAULDRON.get(), RenderType.translucent());

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (world != null && pos != null) {
                var be = world.getBlockEntity(pos);
                if (be instanceof AspectCauldronBlockEntity cauldron) {
                    return cauldron.getWaterColor();
                }
            }
            return 0x3F76E4;
        }, ModBlocks.ASPECT_CAULDRON.get());

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
                    if (tintIndex == 0) {
                        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                        int color = contents.getColor();
                        return color | 0xDD000000;
                    }
                    return -1;
                },
                ModItems.POTION_LARGE.get(),
                ModItems.POTION_MEDIUM.get(),
                ModItems.POTION_SMALL.get()
        );

        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipFlag, lines) -> {
            var player = Minecraft.getInstance().player;
            if (player == null || !player.isCreative()) return;

            List<AspectAlchemyData.AspectEntry> entries = AspectAlchemyData.ASPECT_MAP.get(stack.getItem());
            if (entries == null || entries.isEmpty()) return;

            lines.add(Component.empty());
            lines.add(Component.translatable("item.aspectalchemy.tooltip.aspects")
                    .withStyle(ChatFormatting.DARK_AQUA));

            for (AspectAlchemyData.AspectEntry entry : entries) {
                ChatFormatting color = entry.effect().isBeneficial() ? ChatFormatting.AQUA : ChatFormatting.RED;
                String power = entry.power() > 1 ? " (" + toRoman(entry.power()) + ")" : "";
                lines.add(Component.literal("  • ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.translatable(entry.effect().getDescriptionId()).withStyle(color))
                        .append(Component.literal(power).withStyle(ChatFormatting.GRAY))
                );
            }
        });


    }

    private static int getPotionColor(ItemStack stack) {
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        return contents.getColor();
    }
}