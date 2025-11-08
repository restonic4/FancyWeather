package com.restonic4.fancyweather.mixin;

import com.restonic4.fancyweather.Constants;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public class SleepRainResetMixin {
    @Shadow @Final private ServerLevelData serverLevelData;

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;setDayTime(J)V"
            )
    )
    private void beforeSetDayTime(BooleanSupplier pHasTimeLeft, CallbackInfo ci) {
        ServerLevel self = (ServerLevel) (Object) this;

        long startingTicks = self.getLevelData().getDayTime();
        long newTicks = startingTicks + 24000L;
        newTicks = newTicks - newTicks % 24000L;

        long difference = newTicks - startingTicks;

        Constants.LOG.info("Difference: {}", difference);

        this.serverLevelData.setRainTime((int) (this.serverLevelData.getRainTime() - difference));
        this.serverLevelData.setThunderTime((int) (this.serverLevelData.getThunderTime() - difference));

        if (this.serverLevelData.getRainTime() <= 0) {
            this.serverLevelData.setRaining(false);
        }

        if (this.serverLevelData.getThunderTime() <= 0) {
            this.serverLevelData.setThundering(false);
        }
    }

    @Inject(method = "resetWeatherCycle", at = @At("HEAD"), cancellable = true)
    public void resetWeatherCycle(CallbackInfo ci) {
        ci.cancel();
    }
}
