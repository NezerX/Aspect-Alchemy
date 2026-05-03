package nezerx.aspectalchemy.compat.jade;

import net.minecraft.resources.ResourceLocation;
import nezerx.aspectalchemy.block.AspectCauldronBlock;
import nezerx.aspectalchemy.block.entity.AspectCauldronBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class AspectAlchemyJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(
                AspectCauldronProvider.INSTANCE,
                AspectCauldronBlockEntity.class
        );
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(
                AspectCauldronProvider.INSTANCE,
                AspectCauldronBlock.class
        );

        registration.addConfig(
                ResourceLocation.fromNamespaceAndPath("aspectalchemy", "cauldron"),
                true
        );
    }
}