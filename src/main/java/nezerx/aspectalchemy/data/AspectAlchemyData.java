package nezerx.aspectalchemy.data;

import net.minecraft.item.Items;
import net.minecraft.item.Item;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;

import java.io.ObjectInputFilter;
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
        put(Items.SPIDER_EYE, StatusEffects.BLINDNESS, StatusEffects.POISON, StatusEffects.WEAKNESS);
        put(Items.PUFFERFISH, StatusEffects.WATER_BREATHING, StatusEffects.NAUSEA, StatusEffects.POISON, StatusEffects.INSTANT_DAMAGE);
        put(Items.GLOWSTONE_DUST, StatusEffects.NIGHT_VISION, StatusEffects.NAUSEA, StatusEffects.GLOWING, StatusEffects.WEAKNESS);
        put(Items.DRAGON_BREATH, StatusEffects.ABSORPTION, StatusEffects.INSTANT_DAMAGE, StatusEffects.LEVITATION, StatusEffects.BAD_OMEN);
        put(Items.NETHER_WART, StatusEffects.RESISTANCE, StatusEffects.FIRE_RESISTANCE, StatusEffects.POISON, StatusEffects.UNLUCK, StatusEffects.SLOWNESS);
        put(Items.RED_MUSHROOM, StatusEffects.POISON, StatusEffects.NAUSEA, StatusEffects.STRENGTH);
        put(Items.BROWN_MUSHROOM, StatusEffects.INSTANT_HEALTH, StatusEffects.HUNGER, StatusEffects.WEAKNESS);
        put(Items.SWEET_BERRIES, StatusEffects.SPEED, StatusEffects.HUNGER, StatusEffects.INSTANT_DAMAGE);
        put(Items.WITHER_ROSE, StatusEffects.WITHER, StatusEffects.WITHER, StatusEffects.INSTANT_DAMAGE, StatusEffects.STRENGTH);
        put(Items.AMETHYST_SHARD, StatusEffects.RESISTANCE, StatusEffects.NIGHT_VISION, StatusEffects.LUCK, StatusEffects.SLOWNESS);
        put(Items.QUARTZ, StatusEffects.HASTE, StatusEffects.GLOWING, StatusEffects.WEAKNESS);
        put(Items.COAL, StatusEffects.STRENGTH, StatusEffects.HUNGER, StatusEffects.INSTANT_DAMAGE);
        put(Items.TORCHFLOWER, StatusEffects.NIGHT_VISION, StatusEffects.NIGHT_VISION, StatusEffects.REGENERATION, StatusEffects.LUCK);
        put(Items.EGG, StatusEffects.LEVITATION, StatusEffects.WEAKNESS, StatusEffects.INSTANT_HEALTH);
        put(Items.SOUL_SAND, StatusEffects.SLOWNESS, StatusEffects.WITHER, StatusEffects.UNLUCK, StatusEffects.INVISIBILITY);
        put(Items.CLAY_BALL, StatusEffects.SLOWNESS, StatusEffects.RESISTANCE, StatusEffects.INSTANT_HEALTH);
        put(Items.PITCHER_PLANT, StatusEffects.ABSORPTION, StatusEffects.ABSORPTION, StatusEffects.SLOWNESS,StatusEffects.POISON,StatusEffects.WATER_BREATHING);
        put(Items.OBSIDIAN, StatusEffects.RESISTANCE, StatusEffects.SLOWNESS, StatusEffects.FIRE_RESISTANCE);
        put(Items.DIAMOND, StatusEffects.RESISTANCE, StatusEffects.HEALTH_BOOST, StatusEffects.LUCK);
        put(Items.ECHO_SHARD, StatusEffects.STRENGTH, StatusEffects.STRENGTH, StatusEffects.SPEED, StatusEffects.WITHER);
        put(Items.NETHER_BRICK, StatusEffects.FIRE_RESISTANCE, StatusEffects.STRENGTH, StatusEffects.WITHER);
        put(Items.MILK_BUCKET, StatusEffects.SATURATION, StatusEffects.REGENERATION, StatusEffects.WEAKNESS);
        put(Items.HONEY_BOTTLE, StatusEffects.INSTANT_HEALTH, StatusEffects.REGENERATION, StatusEffects.SLOWNESS);
        put(Items.CHORUS_FRUIT, StatusEffects.LEVITATION, StatusEffects.NAUSEA, StatusEffects.INVISIBILITY);
        put(Items.ENDER_PEARL, StatusEffects.SPEED, StatusEffects.LEVITATION, StatusEffects.WEAKNESS);
        put(Items.ROTTEN_FLESH, StatusEffects.HUNGER, StatusEffects.STRENGTH, StatusEffects.POISON);
        put(Items.BONE, StatusEffects.WEAKNESS, StatusEffects.RESISTANCE);
        put(Items.STRING, StatusEffects.SLOWNESS, StatusEffects.WEAKNESS, StatusEffects.INVISIBILITY);
        put(Items.FEATHER, StatusEffects.SLOW_FALLING, StatusEffects.SPEED, StatusEffects.WEAKNESS);
        put(Items.HONEYCOMB, StatusEffects.INSTANT_HEALTH, StatusEffects.SLOWNESS, StatusEffects.ABSORPTION);
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