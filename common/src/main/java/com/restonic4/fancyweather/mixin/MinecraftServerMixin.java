package com.restonic4.fancyweather.mixin;

import com.restonic4.fancyweather.custom.events.ServerEvents;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "loadLevel", at = @At("TAIL"))
    private void loadWorld(CallbackInfo ci) {
        ServerEvents.WORLD_LOADED.invoker().onEvent((MinecraftServer) (Object) this);
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    private void unloadWorld(CallbackInfo ci) {
        ServerEvents.WORLD_UNLOADING.invoker().onEvent((MinecraftServer) (Object) this);
    }
}
