package nezerx.aspectalchemy.neoforge;

import net.neoforged.fml.common.Mod;
import nezerx.aspectalchemy.AspectAlchemyMod;

@Mod(AspectAlchemyMod.MOD_ID)
public final class AspectAlchemyNeoForge {
    public AspectAlchemyNeoForge() {
        // Инициализация общей логики (Common)
        AspectAlchemyMod.init();
    }
}