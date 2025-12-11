package com.restonic4.fancyweather.mixin;

import com.restonic4.fancyweather.custom.events.ServerEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelChunk.class)
public class LevelChunkMixin {
    @Shadow @Final private Level level;

    @Inject(method = "setLoaded", at = @At("HEAD"))
    private void loaded(boolean state, CallbackInfo ci) {
        if (this.level instanceof ServerLevel serverLevel) {
            if (state) {
                ServerEvents.CHUNK_LOADED.invoker().onEvent(serverLevel, (LevelChunk) (Object) this);
            } else {
                ServerEvents.CHUNK_UNLOADED.invoker().onEvent(serverLevel, (LevelChunk) (Object) this);
            }
        } else if (this.level instanceof ClientLevel clientLevel) {
            // TODO: To implement
        }
    }
}
