package nezerx.aspectalchemy.client.patchouli;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import nezerx.aspectalchemy.data.AspectAlchemyData;
import vazkii.patchouli.api.IComponentRenderContext;
import vazkii.patchouli.api.ICustomComponent;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.client.base.ClientAdvancements;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class AspectTooltipComponent implements ICustomComponent {

    public String item_id;
    public int width = 18;
    public int height = 18;

    private transient int compX, compY;
    private transient ItemStack stack = ItemStack.EMPTY;

    @Override
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup, HolderLookup.Provider registries) {
        if (item_id != null && !item_id.isEmpty()) {
            // Используем новый метод wrap с передачей registries, как в твоем IVariable.java
            IVariable wrappedId = IVariable.wrap(item_id, registries);
            String resolvedId = lookup.apply(wrappedId).asString();

            if (resolvedId != null && !resolvedId.isEmpty()) {
                ResourceLocation id = ResourceLocation.tryParse(resolvedId);
                if (id != null) {
                    var item = BuiltInRegistries.ITEM.get(id);
                    stack = new ItemStack(item);
                }
            }
        }
    }

    @Override
    public void build(int componentX, int componentY, int pageNum) {
        this.compX = componentX;
        this.compY = componentY;
    }

    @Override
    public void onDisplayed(IComponentRenderContext context) {}

    @Override
    public void render(GuiGraphics graphics, IComponentRenderContext context, float pticks, int mouseX, int mouseY) {
        if (stack.isEmpty()) return;
        context.renderItemStack(graphics, compX, compY, -1, -1, stack);
        if (!context.isAreaHovered(mouseX, mouseY, compX, compY, width, height)) return;
        context.setHoverTooltipComponents(buildTooltip());
    }

    private List<Component> buildTooltip() {
        List<Component> lines = new ArrayList<>();
        lines.add(stack.getHoverName().copy().withStyle(s -> s.withColor(0xFFAA00)));

        List<AspectAlchemyData.AspectEntry> aspects = AspectAlchemyData.ASPECT_MAP.get(stack.getItem());
        if (aspects == null) return lines;

        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        for (int i = 0; i < aspects.size(); i++) {
            AspectAlchemyData.AspectEntry entry = aspects.get(i);
            String advId = "aspectalchemy:discovery/" + itemId + "_" + i;
            if (ClientAdvancements.hasDone(advId)) {
                // В Mojmap для MobEffect используется getDisplayName()
                String effectName = entry.effect().getDisplayName().getString();
                String power = entry.power() > 1 ? " (" + toRoman(entry.power()) + ")" : "";
                lines.add(Component.literal("§7- " + effectName + power));
            } else {
                lines.add(Component.literal("§8- ???"));
            }
        }
        return lines;
    }

    private static String toRoman(int n) {
        return switch (n) {
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(n);
        };
    }
}