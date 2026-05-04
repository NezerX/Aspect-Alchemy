package nezerx.aspectalchemy.neoforge.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import nezerx.aspectalchemy.AspectAlchemyMod;
import nezerx.aspectalchemy.data.AspectAlchemyData;

import java.util.List;

import static nezerx.aspectalchemy.block.entity.AspectCauldronBlockEntity.toRoman;

@EventBusSubscriber(modid = AspectAlchemyMod.MOD_ID, value = Dist.CLIENT)
public final class AspectAlchemyNeoForgeTooltip {

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        var player = Minecraft.getInstance().player;
        if (player == null || !player.isCreative()) return;

        List<AspectAlchemyData.AspectEntry> entries = AspectAlchemyData.ASPECT_MAP.get(event.getItemStack().getItem());
        if (entries == null || entries.isEmpty()) return;

        event.getToolTip().add(Component.empty());
        event.getToolTip().add(Component.translatable("item.aspectalchemy.tooltip.aspects")
                .withStyle(ChatFormatting.DARK_AQUA));

        for (AspectAlchemyData.AspectEntry entry : entries) {
            ChatFormatting color = entry.effect().isBeneficial() ? ChatFormatting.AQUA : ChatFormatting.RED;
            String power = entry.power() > 1 ? " (" + toRoman(entry.power()) + ")" : "";
            event.getToolTip().add(Component.literal("  • ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.translatable(entry.effect().getDescriptionId()).withStyle(color))
                    .append(Component.literal(power).withStyle(ChatFormatting.GRAY))
            );
        }
    }
}