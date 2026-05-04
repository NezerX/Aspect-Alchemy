package nezerx.aspectalchemy.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import nezerx.aspectalchemy.AspectAlchemyMod;
import nezerx.aspectalchemy.block.entity.AspectCauldronBlockEntity;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(AspectAlchemyMod.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<AspectCauldronBlockEntity>> ASPECT_CAULDRON = BLOCK_ENTITIES.register("aspect_cauldron",
            () -> BlockEntityType.Builder.of(AspectCauldronBlockEntity::new,
                    ModBlocks.ASPECT_CAULDRON.get()).build(null)   // <-- это ок, лямбда уже есть
    );

    public static void register() {
        BLOCK_ENTITIES.register();
    }
}