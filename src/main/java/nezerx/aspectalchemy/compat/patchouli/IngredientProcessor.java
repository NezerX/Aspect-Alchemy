package nezerx.aspectalchemy.compat.patchouli;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import nezerx.aspectalchemy.data.AspectAlchemyData;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

import java.util.ArrayList;
import java.util.List;

public class IngredientProcessor implements IComponentProcessor {
    private final List<ItemStack> ingredients = new ArrayList<>();

    @Override
    public void setup(World world, IVariableProvider variables) {
        ingredients.clear();
        for (int i = 0; i < 20; i++) {
            String key = "item" + i;
            if (variables.has(key)) {
                ItemStack stack = variables.get(key).as(ItemStack.class);
                ingredients.add(stack != null && !stack.isEmpty() ? stack : ItemStack.EMPTY);
            } else {
                ingredients.add(ItemStack.EMPTY);
            }
        }
    }

    @Override
    public IVariable process(World world, String key) {
        if (key.startsWith("slot")) {
            int index = Integer.parseInt(key.substring(4));
            if (index < 0 || index >= ingredients.size()) return IVariable.empty();
            ItemStack stack = ingredients.get(index);
            if (stack.isEmpty()) return IVariable.empty();
            return IVariable.from(stack);
        }
        return null;
    }
}