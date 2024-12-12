package org.andarworld.core.events;

import java.math.BigDecimal;

public record ProductResultEvent(
        String uuid,
        String name,
        String quantity,
        BigDecimal price
) {
}
