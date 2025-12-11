package com.restonic4.fancyweather.mixin;

import com.restonic4.fancyweather.Constants;
import com.restonic4.fancyweather.custom.sync.Synchronizer;
import com.restonic4.fancyweather.custom.sync.WeatherSave;
import com.restonic4.fancyweather.utils.FileManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "handleCustomPayload", at = @At("HEAD"))
    private void onHandleCustomPayload(ClientboundCustomPayloadPacket packet, CallbackInfo ci) {
        ResourceLocation id = packet.getIdentifier(); // channel id
        if (!Synchronizer.SYNC_PACKET_ID.equals(id)) return; // not our packet

        Minecraft mc = Minecraft.getInstance();
        boolean isIntegrated = mc.hasSingleplayerServer();
        if (isIntegrated) return; // If integrated, we do not want to overwrite the already exiting weather data

        // Read payload (we used writeUtf on server). Use the usual max length to match FriendlyByteBuf limits.
        FriendlyByteBuf buf = packet.getData();
        String json;
        try {
            json = buf.readUtf(); // same max as writeUtf/writeUtf default
        } catch (Exception e) {
            // safe-guard in case of malformed payload
            e.printStackTrace();
            return;
        }

        // Parse on network thread, but set state on client main thread:
        WeatherSave save;
        try {
            save = FileManager.GSON.fromJson(json, WeatherSave.class);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // schedule on client thread to be safe
        Minecraft.getInstance().execute(() -> {
            Synchronizer.setLoadedData(save);
            // optionally log / notify client code
            Constants.LOG.info("FancyWeather: received sync packet and updated loadedData from server");
        });
    }
}
