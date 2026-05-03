package nezerx.aspectalchemy.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;
import nezerx.aspectalchemy.init.ModBlocks;

public final class AspectAlchemyFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // 🔹 Рендер-слои для блоков (если нужны прозрачные/пропускающие свет)
        // Пример для каулдрона, если он использует кастомную отрисовку жидкости:
      //  BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.ASPECT_CAULDRON.get(), RenderType.cutout());
     //   BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.ASPECT_EMPTY_CAULDRON.get(), RenderType.cutout());

        // 🔹 Здесь можно зарегистрировать:
        // - BlockEntityRenderer'ы (если есть кастомный рендер)
        // - Клиентские события
        // - Кастомные оверлеи, худы и т.д.
    }
}