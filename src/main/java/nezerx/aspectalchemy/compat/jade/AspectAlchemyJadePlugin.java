package nezerx.aspectalchemy.compat.jade;

// ✅ Обновлённые импорты (было lol.bai → стало snownee)
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import net.minecraft.util.Identifier;
import nezerx.aspectalchemy.block.AspectCauldronBlock;
import nezerx.aspectalchemy.block.entity.AspectCauldronBlockEntity;

@WailaPlugin
public class AspectAlchemyJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        // Регистрируем сбор данных на сервере
        registration.registerBlockDataProvider(
                AspectCauldronProvider.INSTANCE,
                AspectCauldronBlockEntity.class
        );
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // Регистрируем отображение на клиенте
        registration.registerBlockComponent(
                AspectCauldronProvider.INSTANCE,
                AspectCauldronBlock.class
        );

        // 🔹 Опционально: добавь конфиг для включения/выключения фичи
        registration.addConfig(
                Identifier.of("aspectalchemy", "cauldron"),  // ID настройки
                true  // включено по умолчанию
        );
    }
}