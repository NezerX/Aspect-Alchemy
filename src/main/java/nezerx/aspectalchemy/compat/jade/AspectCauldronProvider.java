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
        if (accessor.getServerData().contains("CauldronContents")) {
            NbtList items = accessor.getServerData().getList("CauldronContents", 10);
            IElementHelper helper = tooltip.getElementHelper();

            for (int i = 0; i < items.size(); i++) {
                ItemStack stack = ItemStack.fromNbt(items.getCompound(i));
                if (!stack.isEmpty()) {
                    tooltip.add(helper.item(stack));
                    tooltip.append(stack.getName());
                }
            }
        }

        if (accessor.getServerData().contains("JadeDye")) {
            ItemStack dye = ItemStack.fromNbt(accessor.getServerData().getCompound("JadeDye"));
            tooltip.add(Text.literal("Краситель: ").append(dye.getName()));
        }
    }

    @Override
    public void appendServerData(NbtCompound data, BlockAccessor accessor) {
        // 🔥 Важный каст!
        if (!(accessor.getBlockEntity() instanceof AspectCauldronBlockEntity be)) {
            return;
        }

        NbtList list = new NbtList();
        for (ItemStack stack : be.getInventory()) {
            if (!stack.isEmpty()) {
                NbtCompound tag = new NbtCompound();
                stack.writeNbt(tag);
                list.add(tag);
            }
        }
        data.put("CauldronContents", list);

        if (be.hasLoadedDye()) {
            NbtCompound nbt = be.createNbt();
            if (nbt.contains("LoadedDye")) {
                data.put("JadeDye", nbt.getCompound("LoadedDye"));
            }
        }
    }

    @Override
    public Identifier getUid() {
        return Identifier.of("aspectalchemy", "cauldron");
    }
}