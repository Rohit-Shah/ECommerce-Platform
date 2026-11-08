package com.platform.common;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {
    String eventId();
    Instant occurredOn();
    String aggregateType();
    String aggregateId();
}
