package nezerx.aspectalchemy.data;

import net.minecraft.item.Items;
import net.minecraft.item.Item;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import java.util.*;

public class AspectAlchemyData {
    public static final Map<Item, List<StatusEffect>> ASPECT_MAP = new HashMap<>();

    static {
        put(Items.RABBIT_FOOT, StatusEffects.SPEED, StatusEffects.JUMP_BOOST, StatusEffects.MINING_FATIGUE, StatusEffects.LUCK);
        put(Items.PHANTOM_MEMBRANE, StatusEffects.SPEED, StatusEffects.SLOW_FALLING, StatusEffects.LEVITATION);
        put(Items.SUGAR, StatusEffects.SPEED, StatusEffects.ABSORPTION, StatusEffects.SATURATION);
        put(Items.BLAZE_POWDER, StatusEffects.SPEED, StatusEffects.STRENGTH, StatusEffects.FIRE_RESISTANCE, StatusEffects.GLOWING);
        put(Items.REDSTONE, StatusEffects.HASTE, StatusEffects.STRENGTH, StatusEffects.NIGHT_VISION, StatusEffects.INSTANT_DAMAGE);
        put(Items.GUNPOWDER, StatusEffects.HASTE, StatusEffects.STRENGTH, StatusEffects.HUNGER, StatusEffects.GLOWING, StatusEffects.INSTANT_DAMAGE);
        put(Items.LAPIS_LAZULI, StatusEffects.HASTE, StatusEffects.WATER_BREATHING, StatusEffects.HUNGER, StatusEffects.LUCK);
        put(Items.FERMENTED_SPIDER_EYE, StatusEffects.JUMP_BOOST, StatusEffects.BLINDNESS, StatusEffects.NAUSEA, StatusEffects.POISON, StatusEffects.WEAKNESS);
        put(Items.GHAST_TEAR, StatusEffects.REGENERATION, StatusEffects.FIRE_RESISTANCE, StatusEffects.HEALTH_BOOST, StatusEffects.LEVITATION, StatusEffects.BAD_OMEN);
        put(Items.GOLDEN_APPLE, StatusEffects.REGENERATION, StatusEffects.ABSORPTION, StatusEffects.LUCK);
        put(Items.TURTLE_HELMET, StatusEffects.RESISTANCE, StatusEffects.WATER_BREATHING, StatusEffects.SLOWNESS, StatusEffects.MINING_FATIGUE);
        put(Items.GLISTERING_MELON_SLICE, StatusEffects.HEALTH_BOOST, StatusEffects.SATURATION, StatusEffects.SLOWNESS);
        put(Items.GOLDEN_CARROT, StatusEffects.NIGHT_VISION, StatusEffects.SATURATION, StatusEffects.SLOWNESS);
        put(Items.MAGMA_CREAM, StatusEffects.FIRE_RESISTANCE, StatusEffects.HUNGER, StatusEffects.INSTANT_DAMAGE, StatusEffects.UNLUCK, StatusEffects.SLOW_FALLING);
        put(Items.SPIDER_EYE, StatusEffects.BLINDNESS, StatusEffects.POISON);
        put(Items.PUFFERFISH, StatusEffects.WATER_BREATHING, StatusEffects.NAUSEA, StatusEffects.POISON, StatusEffects.INSTANT_DAMAGE);
        put(Items.GLOWSTONE_DUST, StatusEffects.NIGHT_VISION, StatusEffects.NAUSEA, StatusEffects.GLOWING, StatusEffects.WEAKNESS);
        put(Items.DRAGON_BREATH, StatusEffects.ABSORPTION, StatusEffects.INSTANT_DAMAGE, StatusEffects.LEVITATION, StatusEffects.BAD_OMEN);
        put(Items.NETHER_WART, StatusEffects.RESISTANCE, StatusEffects.FIRE_RESISTANCE, StatusEffects.POISON, StatusEffects.UNLUCK);
    }

    private static void put(Item item, StatusEffect... effects) {
        ASPECT_MAP.put(item, Arrays.asList(effects));
    }

    public static List<StatusEffect> getEffectsForItems(List<net.minecraft.item.ItemStack> stacks) {
        List<StatusEffect> allEffects = new ArrayList<>();
        for (var stack : stacks) {
            if (!stack.isEmpty() && ASPECT_MAP.containsKey(stack.getItem())) {
                allEffects.addAll(ASPECT_MAP.get(stack.getItem()));
            }
        }
        return allEffects;
    }
}