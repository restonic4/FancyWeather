package com.restonic4.fancyweather.mixin;

import com.restonic4.fancyweather.custom.events.ClientEvents;
import com.restonic4.fancyweather.custom.sync.Synchronizer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "tick", at = @At("HEAD"))
    private void startTick(BooleanSupplier $$0, CallbackInfo ci) {
        ClientEvents.TICK_STARTED.invoker().onEvent(this.minecraft);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void endTick(BooleanSupplier $$0, CallbackInfo ci) {
        ClientEvents.TICK_ENDED.invoker().onEvent(this.minecraft);
    }

    @Redirect(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;setDayTime(J)V"))
    private void syncTimeWithRealWorld(ClientLevel clientLevel, long newValue) {
        if (Synchronizer.isEnabled()) {
            clientLevel.setDayTime(Synchronizer.getSyncedTime(clientLevel));
        } else {
            clientLevel.setDayTime(newValue);
        }
    }
}
