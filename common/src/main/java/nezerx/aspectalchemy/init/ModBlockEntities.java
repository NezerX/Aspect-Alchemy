package nezerx.aspectalchemy.init;

import nezerx.aspectalchemy.AspectAlchemyMod;
import nezerx.aspectalchemy.block.entity.AspectCauldronBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {

    public static final BlockEntityType<AspectCauldronBlockEntity> ASPECT_CAULDRON =
            BlockEntityType.Builder.of(AspectCauldronBlockEntity::new, ModBlocks.ASPECT_CAULDRON).build(null);

    public static void register() {
        Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(AspectAlchemyMod.MOD_ID, "aspect_cauldron"),
                ASPECT_CAULDRON
        );
    }
}