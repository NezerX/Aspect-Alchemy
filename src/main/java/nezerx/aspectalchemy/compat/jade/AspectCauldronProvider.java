package nezerx.aspectalchemy.compat.jade;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;
import nezerx.aspectalchemy.block.entity.AspectCauldronBlockEntity;

public enum AspectCauldronProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        NbtCompound data = accessor.getServerData();

        // Ингредиенты
        if (data.contains("CauldronContents")) {
            NbtList items = data.getList("CauldronContents", 10);
            IElementHelper helper = tooltip.getElementHelper();
            for (int i = 0; i < items.size(); i++) {
                ItemStack stack = ItemStack.fromNbt(items.getCompound(i));
                if (!stack.isEmpty()) {
                    tooltip.add(helper.item(stack));
                    tooltip.append(stack.getName());
                }
            }
        }

        // Краситель
        if (data.contains("JadeDye")) {
            ItemStack dye = ItemStack.fromNbt(data.getCompound("JadeDye"));
            tooltip.add(Text.literal("Краситель: ").append(dye.getName()));
        }

        // Эффекты
        if (data.contains("CauldronEffects")) {
            NbtList effects = data.getList("CauldronEffects", 10);
            if (!effects.isEmpty()) {
                tooltip.add(Text.literal("§6Эффекты:"));
                for (int i = 0; i < effects.size(); i++) {
                    NbtCompound eff = effects.getCompound(i);
                    String name = eff.getString("Name");
                    int amplifier = eff.getInt("Amplifier");

                    String roman = switch (amplifier) {
                        case 1 -> " II"; case 2 -> " III";
                        case 3 -> " IV"; case 4 -> " V";
                        default -> "";
                    };

                    tooltip.add(Text.literal("§7• §r")
                            .append(Text.translatable(name))
                            .append(Text.literal(roman)));
                }
            }
        }
    }

    @Override
    public void appendServerData(NbtCompound data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof AspectCauldronBlockEntity be)) return;

        // Ингредиенты (как было)
        NbtList list = new NbtList();
        for (ItemStack stack : be.getInventory()) {
            if (!stack.isEmpty()) {
                NbtCompound tag = new NbtCompound();
                stack.writeNbt(tag);
                list.add(tag);
            }
        }
        data.put("CauldronContents", list);

        // Краситель (как было)
        if (be.hasLoadedDye()) {
            NbtCompound nbt = be.createNbt();
            if (nbt.contains("LoadedDye")) {
                data.put("JadeDye", nbt.getCompound("LoadedDye"));
            }
        }

        // Эффекты — новое
        NbtList effectList = new NbtList();
        for (var effect : be.getActiveEffects()) {
            NbtCompound eff = new NbtCompound();
            eff.putString("Name", effect.getEffectType().getTranslationKey());
            eff.putInt("Amplifier", effect.getAmplifier());
            effectList.add(eff);
        }
        data.put("CauldronEffects", effectList);
    }

    @Override
    public Identifier getUid() {
        return Identifier.of("aspectalchemy", "cauldron");
    }


}