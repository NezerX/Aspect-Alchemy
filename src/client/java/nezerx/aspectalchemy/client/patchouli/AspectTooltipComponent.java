package nezerx.aspectalchemy.client.patchouli;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffect;
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

    public String item_id;  // читается из JSON напрямую через Gson
    public int width = 18;
    public int height = 18;

    private transient int compX, compY;
    private transient ItemStack stack = ItemStack.EMPTY;

    @Override
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup) {
        if (item_id != null && !item_id.isEmpty()) {
            // lookup резолвит "#item0#" → "minecraft:rabbit_foot" как строку
            String resolvedId = lookup.apply(IVariable.wrap(item_id)).asString();
            if (resolvedId != null && !resolvedId.isEmpty()) {
                // берём только path, отбрасываем namespace если есть
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

        // рендерим предмет, но передаём мышь за экран чтобы item не выставлял свой тултип
        context.renderItemStack(graphics, compX, compY, -1, -1, stack);

        // показываем наш тултип
        if (!context.isAreaHovered(mouseX, mouseY, compX, compY, width, height)) return;
        context.setHoverTooltipComponents(buildTooltip());
    }

    private List<Text> buildTooltip() {
        List<Text> lines = new ArrayList<>();
        lines.add(stack.getName().copy().styled(s -> s.withColor(0xFFAA00)));

        List<StatusEffect> aspects = AspectAlchemyData.ASPECT_MAP.get(stack.getItem());
        if (aspects == null) return lines;

        String itemId = Registries.ITEM.getId(stack.getItem()).getPath();
        for (int i = 0; i < aspects.size(); i++) {
            String advId = "aspectalchemy:discovery/" + itemId + "_" + i;
            if (ClientAdvancements.hasDone(advId)) {
                lines.add(Text.literal("§7- " + aspects.get(i).getName().getString()));
            } else {
                lines.add(Text.literal("§8- ???"));
            }
        }
        return lines;
    }
}