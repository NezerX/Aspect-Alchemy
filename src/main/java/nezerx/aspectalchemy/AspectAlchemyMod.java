package nezerx.aspectalchemy;

import nezerx.aspectalchemy.init.ModBlocks;
import nezerx.aspectalchemy.init.ModBlockEntities;
import nezerx.aspectalchemy.init.ModItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AspectAlchemyMod implements ModInitializer {
    public static final String MOD_ID = "aspectalchemy";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Aspect Alchemy!");

        ModBlocks.register();
        ModBlockEntities.register();
        ModItems.register();
    }
}