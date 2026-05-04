package nezerx.aspectalchemy.client.patchouli;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup) {
        if (item_id != null && !item_id.isEmpty()) {
            String resolvedId = lookup.apply(IVariable.wrap(item_id)).asString();
            if (resolvedId != null && !resolvedId.isEmpty()) {
                Identifier id = Identifier.tryParse(resolvedId);
                if (id != null) {
                    var item = Registries.ITEM.get(id);
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
    public void render(DrawContext graphics, IComponentRenderContext context, float pticks, int mouseX, int mouseY) {
        if (stack.isEmpty()) return;
        context.renderItemStack(graphics, compX, compY, -1, -1, stack);
        if (!context.isAreaHovered(mouseX, mouseY, compX, compY, width, height)) return;
        context.setHoverTooltipComponents(buildTooltip());
    }

    private List<Text> buildTooltip() {
        List<Text> lines = new ArrayList<>();
        lines.add(stack.getName().copy().styled(s -> s.withColor(0xFFAA00)));

        List<AspectAlchemyData.AspectEntry> aspects = AspectAlchemyData.ASPECT_MAP.get(stack.getItem());
        if (aspects == null) return lines;

        String itemId = Registries.ITEM.getId(stack.getItem()).getPath();
        for (int i = 0; i < aspects.size(); i++) {
            AspectAlchemyData.AspectEntry entry = aspects.get(i);
            String advId = "aspectalchemy:discovery/" + itemId + "_" + i;
            if (ClientAdvancements.hasDone(advId)) {
                String effectName = entry.effect().getName().getString();
                String power = entry.power() > 1 ? " (" + toRoman(entry.power()) + ")" : "";
                lines.add(Text.literal("§7- " + effectName + power));
            } else {
                lines.add(Text.literal("§8- ???"));
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