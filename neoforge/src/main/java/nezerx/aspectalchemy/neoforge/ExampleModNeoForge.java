package nezerx.aspectalchemy.neoforge;

import net.neoforged.fml.common.Mod;

import nezerx.aspectalchemy.ExampleMod;

@Mod(ExampleMod.MOD_ID)
public final class ExampleModNeoForge {
    public ExampleModNeoForge() {
        // Run our common setup.
        ExampleMod.init();
    }
}
