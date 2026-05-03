package nezerx.aspectalchemy.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

public class MultiUsePotionItem extends PotionItem {
    private final int maxSips;
    private final Item emptyBottle;

    public MultiUsePotionItem(Properties properties, int maxSips, Item emptyBottle) {
        super(properties);
        this.maxSips = maxSips;
        this.emptyBottle = emptyBottle;
    }

    public int getMaxSips() {
        return maxSips;
    }

    public int getSipsLeft(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains("SipsLeft")) return tag.getInt("SipsLeft");
        }
        return maxSips;
    }

    public void setSipsLeft(ItemStack stack, int sips) {
        // Читаем существующий тег или создаём новый
        CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = existing != null ? existing.copyTag() : new CompoundTag();

        tag.putInt("SipsLeft", sips);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        // CustomModelData
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(maxSips - sips));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        Player player = user instanceof Player p ? p : null;

        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
        }

        if (!level.isClientSide) {
            PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            for (MobEffectInstance effect : contents.getAllEffects()) {
                if (effect.getEffect().value().isInstantenous()) {
                    effect.getEffect().value().applyInstantenousEffect(player, player, user, effect.getAmplifier(), 1.0D);
                } else {
                    user.addEffect(new MobEffectInstance(effect));
                }
            }
        }

        if (player != null) {
            player.awardStat(Stats.ITEM_USED.get(this));
            if (!player.getAbilities().instabuild) {
                int sips = getSipsLeft(stack) - 1;
                if (sips <= 0) {
                    stack.shrink(1);
                    if (stack.isEmpty()) {
                        return new ItemStack(emptyBottle);
                    } else {
                        player.getInventory().add(new ItemStack(emptyBottle));
                    }
                } else {
                    setSipsLeft(stack, sips);
                }
            }
        }

        user.gameEvent(GameEvent.DRINK);
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        int sips = getSipsLeft(stack);
        tooltip.add(Component.translatable("item.aspectalchemy.tooltip.sips", sips, maxSips)
                .withStyle(style -> style.withColor(0xAAAAAA)));
    }
}