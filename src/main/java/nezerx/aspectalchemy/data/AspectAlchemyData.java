package nezerx.aspectalchemy.data;

import net.minecraft.item.Items;
import net.minecraft.item.Item;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import java.util.*;

public class AspectAlchemyData {

    public record AspectEntry(StatusEffect effect, int power) {}

    public static final Map<Item, List<AspectEntry>> ASPECT_MAP = new HashMap<>();

    // Веса для выбора названия (при равном amplifier побеждает больший вес)
    public static final Map<StatusEffect, Integer> NAMING_WEIGHT = new HashMap<>();

    static {
        // ── Ингредиенты ───────────────────────────────────────────────────────
        put(Items.RABBIT_FOOT,          e(StatusEffects.SPEED, 1),          e(StatusEffects.JUMP_BOOST, 1),      e(StatusEffects.MINING_FATIGUE, 1), e(StatusEffects.LUCK, 1));
        put(Items.PHANTOM_MEMBRANE,     e(StatusEffects.BLINDNESS, 1),          e(StatusEffects.SLOW_FALLING, 2),    e(StatusEffects.LEVITATION, 1));
        put(Items.SUGAR,                e(StatusEffects.SPEED, 1),          e(StatusEffects.ABSORPTION, 1),      e(StatusEffects.SATURATION, 1));
        put(Items.BLAZE_POWDER,         e(StatusEffects.SPEED, 1),          e(StatusEffects.STRENGTH, 2),        e(StatusEffects.FIRE_RESISTANCE, 1), e(StatusEffects.GLOWING, 1));
        put(Items.REDSTONE,             e(StatusEffects.HASTE, 1),          e(StatusEffects.STRENGTH, 1),        e(StatusEffects.NIGHT_VISION, 1),    e(StatusEffects.INSTANT_DAMAGE, 1));
        put(Items.GUNPOWDER,            e(StatusEffects.HASTE, 1),          e(StatusEffects.STRENGTH, 1),        e(StatusEffects.HUNGER, 1),          e(StatusEffects.GLOWING, 1),         e(StatusEffects.INSTANT_DAMAGE, 1));
        put(Items.LAPIS_LAZULI,         e(StatusEffects.HASTE, 1),          e(StatusEffects.WATER_BREATHING, 1), e(StatusEffects.HUNGER, 1),          e(StatusEffects.LUCK, 1));
        put(Items.FERMENTED_SPIDER_EYE, e(StatusEffects.BLINDNESS, 1),   e(StatusEffects.SPEED, 1),    e(StatusEffects.JUMP_BOOST, 1));
        put(Items.GHAST_TEAR,           e(StatusEffects.REGENERATION, 2),   e(StatusEffects.FIRE_RESISTANCE, 1), e(StatusEffects.HEALTH_BOOST, 1),    e(StatusEffects.LEVITATION, 1),      e(StatusEffects.BAD_OMEN, 1));
        put(Items.GOLDEN_APPLE,         e(StatusEffects.REGENERATION, 1),   e(StatusEffects.ABSORPTION, 2),      e(StatusEffects.LUCK, 1));
        put(Items.ENCHANTED_GOLDEN_APPLE, e(StatusEffects.REGENERATION, 2),   e(StatusEffects.ABSORPTION, 2),      e(StatusEffects.LUCK, 2), e(StatusEffects.FIRE_RESISTANCE, 2), e(StatusEffects.STRENGTH, 2));
        put(Items.TURTLE_HELMET,        e(StatusEffects.RESISTANCE, 2),     e(StatusEffects.WATER_BREATHING, 1), e(StatusEffects.SLOWNESS, 1),        e(StatusEffects.MINING_FATIGUE, 1));
        put(Items.GLISTERING_MELON_SLICE, e(StatusEffects.HEALTH_BOOST, 1), e(StatusEffects.SATURATION, 1),      e(StatusEffects.SLOWNESS, 1));
        put(Items.GOLDEN_CARROT,        e(StatusEffects.NIGHT_VISION, 1),   e(StatusEffects.SATURATION, 1),      e(StatusEffects.SLOWNESS, 1));
        put(Items.MAGMA_CREAM,          e(StatusEffects.FIRE_RESISTANCE, 2),e(StatusEffects.HUNGER, 1),          e(StatusEffects.INSTANT_DAMAGE, 1),  e(StatusEffects.UNLUCK, 1),          e(StatusEffects.SLOW_FALLING, 1));
        put(Items.SPIDER_EYE,           e(StatusEffects.BLINDNESS, 1),      e(StatusEffects.POISON, 1),          e(StatusEffects.WEAKNESS, 1));
        put(Items.PUFFERFISH,           e(StatusEffects.WATER_BREATHING, 1),e(StatusEffects.NAUSEA, 1),          e(StatusEffects.POISON, 1),          e(StatusEffects.INSTANT_DAMAGE, 2));
        put(Items.GLOWSTONE_DUST,       e(StatusEffects.NIGHT_VISION, 1),   e(StatusEffects.NAUSEA, 1),          e(StatusEffects.GLOWING, 1),         e(StatusEffects.WEAKNESS, 1));
        put(Items.DRAGON_BREATH,        e(StatusEffects.ABSORPTION, 1),     e(StatusEffects.INSTANT_DAMAGE, 2),  e(StatusEffects.LEVITATION, 1),      e(StatusEffects.BAD_OMEN, 2));
        put(Items.NETHER_WART,          e(StatusEffects.RESISTANCE, 1),     e(StatusEffects.FIRE_RESISTANCE, 1), e(StatusEffects.POISON, 1),          e(StatusEffects.UNLUCK, 1),          e(StatusEffects.SLOWNESS, 1));
        put(Items.RED_MUSHROOM,         e(StatusEffects.POISON, 1),         e(StatusEffects.NAUSEA, 1),          e(StatusEffects.STRENGTH, 1));
        put(Items.BROWN_MUSHROOM,       e(StatusEffects.INSTANT_HEALTH, 1), e(StatusEffects.HUNGER, 1),          e(StatusEffects.WEAKNESS, 1));
        put(Items.SWEET_BERRIES,        e(StatusEffects.SPEED, 1),          e(StatusEffects.HUNGER, 2),          e(StatusEffects.INSTANT_DAMAGE, 1));
        put(Items.WITHER_ROSE,          e(StatusEffects.WITHER, 2),         e(StatusEffects.INSTANT_DAMAGE, 1),  e(StatusEffects.STRENGTH, 1),        e(StatusEffects.BAD_OMEN, 1));
        put(Items.AMETHYST_SHARD,       e(StatusEffects.RESISTANCE, 1),     e(StatusEffects.NIGHT_VISION, 1),    e(StatusEffects.LUCK, 2),            e(StatusEffects.SLOWNESS, 1));
        put(Items.QUARTZ,               e(StatusEffects.HASTE, 1),          e(StatusEffects.GLOWING, 1),         e(StatusEffects.WEAKNESS, 1));
        put(Items.COAL,                 e(StatusEffects.STRENGTH, 1),       e(StatusEffects.HUNGER, 1),          e(StatusEffects.INSTANT_DAMAGE, 1));
        put(Items.TORCHFLOWER,          e(StatusEffects.REGENERATION, 2),   e(StatusEffects.NIGHT_VISION, 1),    e(StatusEffects.LUCK, 1));
        put(Items.EGG,                  e(StatusEffects.LEVITATION, 1),     e(StatusEffects.WEAKNESS, 1),        e(StatusEffects.INSTANT_HEALTH, 1));
        put(Items.SOUL_SAND,            e(StatusEffects.SLOWNESS, 1),       e(StatusEffects.WITHER, 1),          e(StatusEffects.UNLUCK, 1),          e(StatusEffects.INVISIBILITY, 1),    e(StatusEffects.BAD_OMEN, 1));
        put(Items.CLAY_BALL,            e(StatusEffects.SLOWNESS, 1),       e(StatusEffects.RESISTANCE, 1),      e(StatusEffects.INSTANT_HEALTH, 1));
        put(Items.PITCHER_PLANT,        e(StatusEffects.ABSORPTION, 2),     e(StatusEffects.SLOWNESS, 1),        e(StatusEffects.POISON, 1),          e(StatusEffects.WATER_BREATHING, 1));
        put(Items.OBSIDIAN,             e(StatusEffects.RESISTANCE, 1),     e(StatusEffects.SLOWNESS, 1),        e(StatusEffects.FIRE_RESISTANCE, 1));
        put(Items.DIAMOND,              e(StatusEffects.RESISTANCE, 2),     e(StatusEffects.HEALTH_BOOST, 1),    e(StatusEffects.LUCK, 1));
        put(Items.ECHO_SHARD,           e(StatusEffects.STRENGTH, 2),       e(StatusEffects.SPEED, 1),           e(StatusEffects.WITHER, 1));
        put(Items.NETHER_BRICK,         e(StatusEffects.FIRE_RESISTANCE, 1),e(StatusEffects.STRENGTH, 1),        e(StatusEffects.WITHER, 1));
        put(Items.MILK_BUCKET,          e(StatusEffects.SATURATION, 1),     e(StatusEffects.REGENERATION, 1),    e(StatusEffects.WEAKNESS, 1));
        put(Items.HONEY_BOTTLE,         e(StatusEffects.INSTANT_HEALTH, 1), e(StatusEffects.REGENERATION, 1),    e(StatusEffects.SLOWNESS, 1));
        put(Items.CHORUS_FRUIT,         e(StatusEffects.LEVITATION, 2),     e(StatusEffects.NAUSEA, 1),          e(StatusEffects.INVISIBILITY, 1));
        put(Items.ENDER_PEARL,          e(StatusEffects.SPEED, 1),          e(StatusEffects.LEVITATION, 1),      e(StatusEffects.WEAKNESS, 1));
        put(Items.ROTTEN_FLESH,         e(StatusEffects.HUNGER, 1),         e(StatusEffects.STRENGTH, 1),        e(StatusEffects.POISON, 1));
        put(Items.BONE,                 e(StatusEffects.WEAKNESS, 1),       e(StatusEffects.RESISTANCE, 1));
        put(Items.STRING,               e(StatusEffects.SLOWNESS, 1),       e(StatusEffects.WEAKNESS, 1),        e(StatusEffects.INVISIBILITY, 1));
        put(Items.FEATHER,              e(StatusEffects.SLOW_FALLING, 1),   e(StatusEffects.SPEED, 1),           e(StatusEffects.WEAKNESS, 1));
        put(Items.HONEYCOMB,            e(StatusEffects.INSTANT_HEALTH, 1), e(StatusEffects.SLOWNESS, 1),        e(StatusEffects.ABSORPTION, 2));
        put(Items.EXPERIENCE_BOTTLE,    e(StatusEffects.SPEED, 2),          e(StatusEffects.LUCK, 2),            e(StatusEffects.HERO_OF_THE_VILLAGE, 2));
        put(Items.EMERALD_BLOCK,        e(StatusEffects.LUCK, 1),           e(StatusEffects.HERO_OF_THE_VILLAGE, 1),       e(StatusEffects.HUNGER, 2));
        put(Items.PRISMARINE_CRYSTALS,  e(StatusEffects.DOLPHINS_GRACE, 1), e(StatusEffects.WATER_BREATHING, 1));
        put(Items.TROPICAL_FISH,        e(StatusEffects.DOLPHINS_GRACE, 1), e(StatusEffects.INSTANT_HEALTH, 1));
        // ── Веса для именования ───────────────────────────────────────────────
        // Harmful
        NAMING_WEIGHT.put(StatusEffects.WITHER,          100);
        NAMING_WEIGHT.put(StatusEffects.INSTANT_DAMAGE,   90);
        NAMING_WEIGHT.put(StatusEffects.POISON,           80);
        NAMING_WEIGHT.put(StatusEffects.BAD_OMEN,         70);
        NAMING_WEIGHT.put(StatusEffects.BLINDNESS,        60);
        NAMING_WEIGHT.put(StatusEffects.WEAKNESS,         50);
        NAMING_WEIGHT.put(StatusEffects.HUNGER,           40);
        NAMING_WEIGHT.put(StatusEffects.NAUSEA,           30);
        NAMING_WEIGHT.put(StatusEffects.SLOWNESS,         20);
        NAMING_WEIGHT.put(StatusEffects.LEVITATION,       15);
        NAMING_WEIGHT.put(StatusEffects.MINING_FATIGUE,   10);
        NAMING_WEIGHT.put(StatusEffects.UNLUCK,            5);
        NAMING_WEIGHT.put(StatusEffects.GLOWING,           1);
        // Beneficial
        NAMING_WEIGHT.put(StatusEffects.REGENERATION,     95);
        NAMING_WEIGHT.put(StatusEffects.INSTANT_HEALTH,   85);
        NAMING_WEIGHT.put(StatusEffects.HEALTH_BOOST,     75);
        NAMING_WEIGHT.put(StatusEffects.STRENGTH,         70);
        NAMING_WEIGHT.put(StatusEffects.RESISTANCE,       65);
        NAMING_WEIGHT.put(StatusEffects.ABSORPTION,       55);
        NAMING_WEIGHT.put(StatusEffects.SPEED,            45);
        NAMING_WEIGHT.put(StatusEffects.HASTE,            40);
        NAMING_WEIGHT.put(StatusEffects.FIRE_RESISTANCE,  35);
        NAMING_WEIGHT.put(StatusEffects.WATER_BREATHING,  30);
        NAMING_WEIGHT.put(StatusEffects.INVISIBILITY,     25);
        NAMING_WEIGHT.put(StatusEffects.SLOW_FALLING,     20);
        NAMING_WEIGHT.put(StatusEffects.JUMP_BOOST,       15);
        NAMING_WEIGHT.put(StatusEffects.NIGHT_VISION,     10);
        NAMING_WEIGHT.put(StatusEffects.SATURATION,        5);
        NAMING_WEIGHT.put(StatusEffects.LUCK,              1);
    }

    private static AspectEntry e(StatusEffect effect, int power) {
        return new AspectEntry(effect, power);
    }

    private static void put(Item item, AspectEntry... entries) {
        ASPECT_MAP.put(item, Arrays.asList(entries));
    }

    public static List<StatusEffect> getEffectsForItems(List<net.minecraft.item.ItemStack> stacks) {
        List<StatusEffect> allEffects = new ArrayList<>();
        for (var stack : stacks) {
            if (!stack.isEmpty() && ASPECT_MAP.containsKey(stack.getItem())) {
                for (AspectEntry entry : ASPECT_MAP.get(stack.getItem())) {
                    allEffects.add(entry.effect());
                }
            }
        }
        return allEffects;
    }
}