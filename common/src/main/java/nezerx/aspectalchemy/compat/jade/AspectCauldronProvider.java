package nezerx.aspectalchemy.compat.jade;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import nezerx.aspectalchemy.block.entity.AspectCauldronBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

public enum AspectCauldronProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        IElementHelper helper = IElementHelper.get();

        // Ингредиенты
        if (data.contains("CauldronContents", Tag.TAG_LIST)) {
            ListTag items = data.getList("CauldronContents", Tag.TAG_COMPOUND);
            for (int i = 0; i < items.size(); i++) {
                CompoundTag itemTag = items.getCompound(i);
                String itemId = itemTag.getString("id");
                int count = itemTag.getInt("count");

                ResourceLocation id = ResourceLocation.tryParse(itemId);
                if (id != null) {
                    Item item = BuiltInRegistries.ITEM.get(id);
                    if (item != Items.AIR) {
                        ItemStack stack = new ItemStack(item, count);
                        tooltip.add(helper.item(stack));
                        tooltip.add(stack.getHoverName());
                    }
                }
            }
        }

        // Краситель
        if (data.contains("JadeDye", Tag.TAG_COMPOUND)) {
            CompoundTag dyeTag = data.getCompound("JadeDye");
            String dyeId = dyeTag.getString("id");
            int count = dyeTag.getInt("count");

            ResourceLocation id = ResourceLocation.tryParse(dyeId);
            if (id != null) {
                Item dye = BuiltInRegistries.ITEM.get(id);
                if (dye != Items.AIR) {
                    ItemStack stack = new ItemStack(dye, count);
                    tooltip.add(Component.literal("Краситель: ").append(stack.getHoverName()));
                }
            }
        }

        // Эффекты
        if (data.contains("CauldronEffects", Tag.TAG_LIST)) {
            ListTag effects = data.getList("CauldronEffects", Tag.TAG_COMPOUND);
            if (!effects.isEmpty()) {
                tooltip.add(Component.literal("§6Эффекты:"));
                for (int i = 0; i < effects.size(); i++) {
                    CompoundTag eff = effects.getCompound(i);
                    String effectId = eff.getString("id");
                    int amplifier = eff.getInt("amplifier");

                    String roman = switch (amplifier) {
                        case 1 -> " II";
                        case 2 -> " III";
                        case 3 -> " IV";
                        case 4 -> " V";
                        default -> "";
                    };

                    tooltip.add(Component.literal("§7• §r")
                            .append(Component.translatable("effect." + effectId))
                            .append(Component.literal(roman)));
                }
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof AspectCauldronBlockEntity be)) {
            return;
        }

        // 🔹 Ингредиенты - сохраняем только ID и количество
        ListTag list = new ListTag();
        for (ItemStack stack : be.getInventory()) {
            if (!stack.isEmpty()) {
                CompoundTag tag = new CompoundTag();
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
                if (id != null) {
                    tag.putString("id", id.toString());
                    tag.putInt("count", stack.getCount());
                    list.add(tag);
                }
            }
        }
        data.put("CauldronContents", list);

        // 🔹 Краситель
        if (be.hasLoadedDye()) {
            ItemStack dye = be.getLoadedDye();
            if (!dye.isEmpty()) {
                CompoundTag tag = new CompoundTag();
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(dye.getItem());
                if (id != null) {
                    tag.putString("id", id.toString());
                    tag.putInt("count", dye.getCount());
                    data.put("JadeDye", tag);
                }
            }
        }

        // 🔹 Эффекты
        ListTag effectList = new ListTag();
        for (var effect : be.getActiveEffects()) {
            CompoundTag eff = new CompoundTag();

            // 🔹 Фикс: getEffect() возвращает Holder, достаем значение через .value()
            ResourceLocation effectId = BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value());
            if (effectId != null) {
                eff.putString("id", effectId.toString());
                eff.putInt("amplifier", effect.getAmplifier());
                eff.putInt("duration", effect.getDuration());
                effectList.add(eff);
            }
        }
        data.put("CauldronEffects", effectList);
    }
    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath("aspectalchemy", "cauldron");
    }
}