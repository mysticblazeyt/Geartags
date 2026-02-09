package com.mysticblazeyt.geartags.client.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class AfterEntitiesEvents {
    public static final Event<AfterEntitiesCallback> AFTER_ENTITIES = EventFactory.createArrayBacked(
        AfterEntitiesCallback.class,
        listeners -> (worldRenderer, matrices, renderStates, queue, tickDelta) -> {
            for (AfterEntitiesCallback listener : listeners) {
                listener.afterEntities(worldRenderer, matrices, renderStates, queue, tickDelta);
            }
        }
    );

    private AfterEntitiesEvents() {}
}