package org.andarworld.core.events;

public record ProductCreateEvent(
        String uuid,
        String name,
        String quantity
) {
}
