package com.rainvibe.mixin;

import com.rainvibe.RainVibeClient;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Makes only the CLIENT think it rains. Vanilla draws rain, sound, dark sky itself. */
@Mixin(World.class)
public abstract class WorldMixin {

    @Inject(method = "getRainGradient", at = @At("RETURN"), cancellable = true)
    private void rainvibe$rain(float delta, CallbackInfoReturnable<Float> cir) {
        if (((World) (Object) this).isClient()) {
            float fake = RainVibeClient.fakeRain();
            if (fake > cir.getReturnValueF()) cir.setReturnValue(fake);
        }
    }

    @Inject(method = "getThunderGradient", at = @At("RETURN"), cancellable = true)
    private void rainvibe$thunder(float delta, CallbackInfoReturnable<Float> cir) {
        if (((World) (Object) this).isClient()) {
            float fake = RainVibeClient.fakeThunder();
            if (fake > cir.getReturnValueF()) cir.setReturnValue(fake);
        }
    }
}
