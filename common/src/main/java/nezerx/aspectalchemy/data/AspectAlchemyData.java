package nezerx.aspectalchemy.data;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.Holder;
import java.util.*;

public class AspectAlchemyData {

    public record AspectEntry(MobEffect effect, int power) {}

    public static final Map<Item, List<AspectEntry>> ASPECT_MAP = new HashMap<>();
    public static final Map<MobEffect, Integer> NAMING_WEIGHT = new HashMap<>();

    static {
        // Ингредиенты
        put(Items.RABBIT_FOOT,          e(MobEffects.MOVEMENT_SPEED, 1),    e(MobEffects.JUMP, 1),               e(MobEffects.DIG_SLOWDOWN, 1), e(MobEffects.LUCK, 1));
        put(Items.PHANTOM_MEMBRANE,     e(MobEffects.BLINDNESS, 1),          e(MobEffects.SLOW_FALLING, 2),       e(MobEffects.LEVITATION, 1));
        put(Items.SUGAR,                e(MobEffects.MOVEMENT_SPEED, 1),    e(MobEffects.ABSORPTION, 1),         e(MobEffects.SATURATION, 1));
        put(Items.BLAZE_POWDER,         e(MobEffects.MOVEMENT_SPEED, 1),    e(MobEffects.DAMAGE_BOOST, 2),       e(MobEffects.FIRE_RESISTANCE, 1), e(MobEffects.GLOWING, 1));
        put(Items.REDSTONE,             e(MobEffects.DIG_SPEED, 1),         e(MobEffects.DAMAGE_BOOST, 1),       e(MobEffects.NIGHT_VISION, 1),    e(MobEffects.HARM, 1));
        put(Items.GUNPOWDER,            e(MobEffects.DIG_SPEED, 1),         e(MobEffects.DAMAGE_BOOST, 1),       e(MobEffects.HUNGER, 1),          e(MobEffects.GLOWING, 1),         e(MobEffects.HARM, 1));
        put(Items.LAPIS_LAZULI,         e(MobEffects.DIG_SPEED, 1),         e(MobEffects.WATER_BREATHING, 1),    e(MobEffects.HUNGER, 1),          e(MobEffects.LUCK, 1));
        put(Items.FERMENTED_SPIDER_EYE, e(MobEffects.BLINDNESS, 1),          e(MobEffects.MOVEMENT_SPEED, 1),     e(MobEffects.JUMP, 1));
        put(Items.GHAST_TEAR,           e(MobEffects.REGENERATION, 2),      e(MobEffects.FIRE_RESISTANCE, 1),    e(MobEffects.HEALTH_BOOST, 1),    e(MobEffects.LEVITATION, 1),      e(MobEffects.BAD_OMEN, 1));
        put(Items.GOLDEN_APPLE,         e(MobEffects.REGENERATION, 1),      e(MobEffects.ABSORPTION, 2),         e(MobEffects.LUCK, 1));
        put(Items.ENCHANTED_GOLDEN_APPLE, e(MobEffects.REGENERATION, 2),    e(MobEffects.ABSORPTION, 2),         e(MobEffects.LUCK, 2), e(MobEffects.FIRE_RESISTANCE, 2), e(MobEffects.DAMAGE_BOOST, 2));
        put(Items.TURTLE_HELMET,        e(MobEffects.DAMAGE_RESISTANCE, 2), e(MobEffects.WATER_BREATHING, 1),    e(MobEffects.MOVEMENT_SLOWDOWN, 1), e(MobEffects.DIG_SLOWDOWN, 1));
        put(Items.GLISTERING_MELON_SLICE, e(MobEffects.HEALTH_BOOST, 1),     e(MobEffects.SATURATION, 1),         e(MobEffects.MOVEMENT_SLOWDOWN, 1));
        put(Items.GOLDEN_CARROT,        e(MobEffects.NIGHT_VISION, 1),      e(MobEffects.SATURATION, 1),         e(MobEffects.MOVEMENT_SLOWDOWN, 1));
        put(Items.MAGMA_CREAM,          e(MobEffects.FIRE_RESISTANCE, 2),   e(MobEffects.HUNGER, 1),             e(MobEffects.HARM, 1),             e(MobEffects.UNLUCK, 1),          e(MobEffects.SLOW_FALLING, 1));
        put(Items.SPIDER_EYE,           e(MobEffects.BLINDNESS, 1),          e(MobEffects.POISON, 1),             e(MobEffects.WEAKNESS, 1));
        put(Items.PUFFERFISH,           e(MobEffects.WATER_BREATHING, 1),   e(MobEffects.CONFUSION, 1),          e(MobEffects.POISON, 1),             e(MobEffects.HARM, 2));
        put(Items.GLOWSTONE_DUST,       e(MobEffects.NIGHT_VISION, 1),      e(MobEffects.CONFUSION, 1),          e(MobEffects.GLOWING, 1),            e(MobEffects.WEAKNESS, 1));
        put(Items.DRAGON_BREATH,        e(MobEffects.ABSORPTION, 1),        e(MobEffects.HARM, 2),               e(MobEffects.LEVITATION, 1),         e(MobEffects.BAD_OMEN, 2));
        put(Items.NETHER_WART,          e(MobEffects.DAMAGE_RESISTANCE, 1), e(MobEffects.FIRE_RESISTANCE, 1),    e(MobEffects.POISON, 1),             e(MobEffects.UNLUCK, 1),          e(MobEffects.MOVEMENT_SLOWDOWN, 1));
        put(Items.RED_MUSHROOM,         e(MobEffects.POISON, 1),            e(MobEffects.CONFUSION, 1),          e(MobEffects.DAMAGE_BOOST, 1));
        put(Items.BROWN_MUSHROOM,       e(MobEffects.HEAL, 1),              e(MobEffects.HUNGER, 1),             e(MobEffects.WEAKNESS, 1));
        put(Items.SWEET_BERRIES,        e(MobEffects.MOVEMENT_SPEED, 1),    e(MobEffects.HUNGER, 2),             e(MobEffects.HARM, 1));
        put(Items.WITHER_ROSE,          e(MobEffects.WITHER, 2),            e(MobEffects.HARM, 1),               e(MobEffects.DAMAGE_BOOST, 1),       e(MobEffects.BAD_OMEN, 1));
        put(Items.AMETHYST_SHARD,       e(MobEffects.DAMAGE_RESISTANCE, 1), e(MobEffects.NIGHT_VISION, 1),       e(MobEffects.LUCK, 2),               e(MobEffects.MOVEMENT_SLOWDOWN, 1));
        put(Items.QUARTZ,               e(MobEffects.DIG_SPEED, 1),         e(MobEffects.GLOWING, 1),            e(MobEffects.WEAKNESS, 1));
        put(Items.COAL,                 e(MobEffects.DAMAGE_BOOST, 1),      e(MobEffects.HUNGER, 1),             e(MobEffects.HARM, 1));
        put(Items.TORCHFLOWER,          e(MobEffects.REGENERATION, 2),      e(MobEffects.NIGHT_VISION, 1),       e(MobEffects.LUCK, 1));
        put(Items.EGG,                  e(MobEffects.LEVITATION, 1),        e(MobEffects.WEAKNESS, 1),           e(MobEffects.HEAL, 1));
        put(Items.SOUL_SAND,            e(MobEffects.MOVEMENT_SLOWDOWN, 1), e(MobEffects.WITHER, 1),             e(MobEffects.UNLUCK, 1),             e(MobEffects.INVISIBILITY, 1),    e(MobEffects.BAD_OMEN, 1));
        put(Items.CLAY_BALL,            e(MobEffects.MOVEMENT_SLOWDOWN, 1), e(MobEffects.DAMAGE_RESISTANCE, 1),  e(MobEffects.HEAL, 1));
        put(Items.PITCHER_PLANT,        e(MobEffects.ABSORPTION, 2),        e(MobEffects.MOVEMENT_SLOWDOWN, 1),  e(MobEffects.POISON, 1),             e(MobEffects.WATER_BREATHING, 1));
        put(Items.OBSIDIAN,             e(MobEffects.DAMAGE_RESISTANCE, 1), e(MobEffects.MOVEMENT_SLOWDOWN, 1),  e(MobEffects.FIRE_RESISTANCE, 1));
        put(Items.DIAMOND,              e(MobEffects.DAMAGE_RESISTANCE, 2), e(MobEffects.HEALTH_BOOST, 1),       e(MobEffects.LUCK, 1));
        put(Items.ECHO_SHARD,           e(MobEffects.DAMAGE_BOOST, 2),      e(MobEffects.MOVEMENT_SPEED, 1),     e(MobEffects.WITHER, 1));
        put(Items.NETHER_BRICK,         e(MobEffects.FIRE_RESISTANCE, 1),   e(MobEffects.DAMAGE_BOOST, 1),       e(MobEffects.WITHER, 1));
        put(Items.MILK_BUCKET,          e(MobEffects.SATURATION, 1),        e(MobEffects.REGENERATION, 1),       e(MobEffects.WEAKNESS, 1));
        put(Items.HONEY_BOTTLE,         e(MobEffects.HEAL, 1),              e(MobEffects.REGENERATION, 1),       e(MobEffects.MOVEMENT_SLOWDOWN, 1));
        put(Items.CHORUS_FRUIT,         e(MobEffects.LEVITATION, 2),        e(MobEffects.CONFUSION, 1),          e(MobEffects.INVISIBILITY, 1));
        put(Items.ENDER_PEARL,          e(MobEffects.MOVEMENT_SPEED, 1),    e(MobEffects.LEVITATION, 1),         e(MobEffects.WEAKNESS, 1));
        put(Items.ROTTEN_FLESH,         e(MobEffects.HUNGER, 1),            e(MobEffects.DAMAGE_BOOST, 1),       e(MobEffects.POISON, 1));
        put(Items.BONE,                 e(MobEffects.WEAKNESS, 1),          e(MobEffects.DAMAGE_RESISTANCE, 1));
        put(Items.STRING,               e(MobEffects.MOVEMENT_SLOWDOWN, 1), e(MobEffects.WEAKNESS, 1),           e(MobEffects.INVISIBILITY, 1));
        put(Items.FEATHER,              e(MobEffects.SLOW_FALLING, 1),      e(MobEffects.MOVEMENT_SPEED, 1),     e(MobEffects.WEAKNESS, 1));
        put(Items.HONEYCOMB,            e(MobEffects.HEAL, 1),              e(MobEffects.MOVEMENT_SLOWDOWN, 1),  e(MobEffects.ABSORPTION, 2));
        put(Items.EXPERIENCE_BOTTLE,    e(MobEffects.MOVEMENT_SPEED, 2),    e(MobEffects.LUCK, 2),               e(MobEffects.HERO_OF_THE_VILLAGE, 2));
        put(Items.EMERALD_BLOCK,        e(MobEffects.LUCK, 1),              e(MobEffects.HERO_OF_THE_VILLAGE, 1),e(MobEffects.HUNGER, 2));
        put(Items.PRISMARINE_CRYSTALS,  e(MobEffects.DOLPHINS_GRACE, 1),    e(MobEffects.WATER_BREATHING, 1));
        put(Items.TROPICAL_FISH,        e(MobEffects.DOLPHINS_GRACE, 1),    e(MobEffects.HEAL, 1));

        // Веса (здесь тоже используем .value(), чтобы ключом был MobEffect)
        addWeight(MobEffects.WITHER, 100); addWeight(MobEffects.HARM, 90);
        addWeight(MobEffects.POISON, 80); addWeight(MobEffects.BAD_OMEN, 70);
        addWeight(MobEffects.BLINDNESS, 60); addWeight(MobEffects.WEAKNESS, 50);
        addWeight(MobEffects.HUNGER, 40); addWeight(MobEffects.CONFUSION, 30);
        addWeight(MobEffects.MOVEMENT_SLOWDOWN, 20); addWeight(MobEffects.LEVITATION, 15);
        addWeight(MobEffects.DIG_SLOWDOWN, 10); addWeight(MobEffects.UNLUCK, 5);
        addWeight(MobEffects.GLOWING, 1); addWeight(MobEffects.REGENERATION, 95);
        addWeight(MobEffects.HEAL, 85); addWeight(MobEffects.HEALTH_BOOST, 75);
        addWeight(MobEffects.DAMAGE_BOOST, 70); addWeight(MobEffects.DAMAGE_RESISTANCE, 65);
        addWeight(MobEffects.ABSORPTION, 55); addWeight(MobEffects.MOVEMENT_SPEED, 45);
        addWeight(MobEffects.DIG_SPEED, 40); addWeight(MobEffects.FIRE_RESISTANCE, 35);
        addWeight(MobEffects.WATER_BREATHING, 30); addWeight(MobEffects.INVISIBILITY, 25);
        addWeight(MobEffects.SLOW_FALLING, 20); addWeight(MobEffects.JUMP, 15);
        addWeight(MobEffects.NIGHT_VISION, 10); addWeight(MobEffects.SATURATION, 5);
        addWeight(MobEffects.LUCK, 1);
    }

    private static AspectEntry e(Holder<MobEffect> effect, int power) {
        return new AspectEntry(effect.value(), power);
    }

    private static void addWeight(Holder<MobEffect> effect, int weight) {
        NAMING_WEIGHT.put(effect.value(), weight);
    }

    private static void put(Item item, AspectEntry... entries) {
        ASPECT_MAP.put(item, Arrays.asList(entries));
    }

    public static List<MobEffect> getEffectsForItems(List<ItemStack> stacks) {
        List<MobEffect> allEffects = new ArrayList<>();
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