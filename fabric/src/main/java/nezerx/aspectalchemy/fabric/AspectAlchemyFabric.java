package nezerx.aspectalchemy.fabric;

import net.fabricmc.api.ModInitializer;
import nezerx.aspectalchemy.AspectAlchemyMod;

public final class AspectAlchemyFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // Запускаем общую инициализацию
        AspectAlchemyMod.init();

        // 🔹 Здесь можно добавить Fabric-специфичный код, если понадобится:
        // - Регистрация событий через Fabric API
        // - Platform-specific регистрации
    }
}
