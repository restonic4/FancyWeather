package com.restonic4.fancyweather.mixin;

import com.restonic4.fancyweather.custom.events.ServerEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(method = "tick", at = @At("HEAD"))
    private void startTick(BooleanSupplier $$0, CallbackInfo ci) {
        ServerEvents.TICK_STARTED.invoker().onEvent(this.server);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void endTick(BooleanSupplier $$0, CallbackInfo ci) {
        ServerEvents.TICK_ENDED.invoker().onEvent(this.server);
    }
}
