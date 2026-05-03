package nezerx.aspectalchemy.compat.patchouli;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

import java.util.ArrayList;
import java.util.List;

public class IngredientProcessor implements IComponentProcessor {
    private final List<ItemStack> ingredients = new ArrayList<>();

    @Override
    public void setup(Level level, IVariableProvider variables) {
        ingredients.clear();
        for (int i = 0; i < 20; i++) {
            String key = "item" + i;
            if (variables.has(key)) {
                // В 1.21.1 метод as(Class) все еще работает, если объект правильно сериализован
                ItemStack stack = variables.get(key, level.registryAccess()).as(ItemStack.class);
                ingredients.add(stack != null && !stack.isEmpty() ? stack : ItemStack.EMPTY);
            } else {
                ingredients.add(ItemStack.EMPTY);
            }
        }
    }

    @Override
    public IVariable process(Level level, String key) {
        if (key.startsWith("slot")) {
            try {
                int index = Integer.parseInt(key.substring(4));
                if (index < 0 || index >= ingredients.size()) {
                    return IVariable.empty();
                }
                ItemStack stack = ingredients.get(index);
                if (stack.isEmpty()) {
                    return IVariable.empty();
                }
                // В 1.21.1 для предметов нужно передавать HolderLookup.Provider (registryAccess)
                return IVariable.from(stack, level.registryAccess());
            } catch (NumberFormatException e) {
                return IVariable.empty();
            }
        }
        return null;
    }
}