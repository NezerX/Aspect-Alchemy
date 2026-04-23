package nezerx.aspectalchemy.item;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.PotionUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MultiUsePotionItem extends PotionItem {
    private final int maxSips;
    private final Item emptyBottle;

    public MultiUsePotionItem(Settings settings, int maxSips, Item emptyBottle) {
        super(settings);
        this.maxSips = maxSips;
        this.emptyBottle = emptyBottle;
    }

    public int getMaxSips() {
        return maxSips;
    }

    public int getSipsLeft(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains("SipsLeft")) {
            return nbt.getInt("SipsLeft");
        }
        return maxSips; // По умолчанию полное
    }

    public void setSipsLeft(ItemStack stack, int sips) {
        stack.getOrCreateNbt().putInt("SipsLeft", sips);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        PlayerEntity player = user instanceof PlayerEntity ? (PlayerEntity) user : null;

        if (player instanceof ServerPlayerEntity serverPlayer) {
            Criteria.CONSUME_ITEM.trigger(serverPlayer, stack);
        }

        // Применяем эффекты
        if (!world.isClient) {
            List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(stack);
            for (StatusEffectInstance effect : effects) {
                if (effect.getEffectType().isInstant()) {
                    effect.getEffectType().applyInstantEffect(player, player, user, effect.getAmplifier(), 1.0D);
                } else {
                    user.addStatusEffect(new StatusEffectInstance(effect));
                }
            }
        }

        if (player != null) {
            player.incrementStat(Stats.USED.getOrCreateStat(this));
            if (!player.getAbilities().creativeMode) {
                int sips = getSipsLeft(stack) - 1;
                if (sips <= 0) {
                    // Глотки кончились, возвращаем пустую бутылку
                    stack.decrement(1);
                    if (stack.isEmpty()) {
                        return new ItemStack(emptyBottle);
                    } else {
                        player.getInventory().insertStack(new ItemStack(emptyBottle));
                    }
                } else {
                    // Сохраняем оставшиеся глотки
                    setSipsLeft(stack, sips);
                }
            }
        }

        user.emitGameEvent(GameEvent.DRINK);
        return stack;
    }

    // --- Отрисовка полоски ---

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return getSipsLeft(stack) < maxSips;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.round(13.0F * getSipsLeft(stack) / maxSips); // 13 - максимальная длина UI-полоски
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return 0x00FF00; // Ярко-зеленый цвет полоски
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        int sips = getSipsLeft(stack);
        tooltip.add(Text.translatable("item.aspectalchemy.tooltip.sips", sips, maxSips).formatted(Formatting.GRAY));
    }
}