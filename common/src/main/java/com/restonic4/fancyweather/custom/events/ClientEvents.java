package com.restonic4.fancyweather.custom.events;

import com.restonic4.fancyweather.custom.events.factory.Event;
import com.restonic4.fancyweather.custom.events.factory.EventFactory;
import net.minecraft.client.Minecraft;

public class ClientEvents {
    public static final Event<TickStarted> TICK_STARTED = EventFactory.createArray(TickStarted.class, callbacks -> (client) -> {
        for (TickStarted callback : callbacks) {
            callback.onEvent(client);
        }
    });

    @FunctionalInterface
    public interface TickStarted {
        void onEvent(Minecraft client);
    }

    public static final Event<TickEnded> TICK_ENDED = EventFactory.createArray(TickEnded.class, callbacks -> (client) -> {
        for (TickEnded callback : callbacks) {
            callback.onEvent(client);
        }
    });

    @FunctionalInterface
    public interface TickEnded {
        void onEvent(Minecraft client);
    }
}
