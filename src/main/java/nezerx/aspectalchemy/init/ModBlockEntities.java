package nezerx.aspectalchemy.init;

import nezerx.aspectalchemy.AspectAlchemyMod;
import nezerx.aspectalchemy.block.entity.AspectCauldronBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static final BlockEntityType<AspectCauldronBlockEntity> ASPECT_CAULDRON =
            FabricBlockEntityTypeBuilder.create(AspectCauldronBlockEntity::new, ModBlocks.ASPECT_CAULDRON).build();

    public static void register() {
        Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(AspectAlchemyMod.MOD_ID, "aspect_cauldron"), ASPECT_CAULDRON);
    }
}