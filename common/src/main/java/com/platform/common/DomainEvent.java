package com.platform.common;

import java.time.Instant;
import java.util.UUID;

public class DomainEvent {
    UUID eventId;
    Instant occurredOn;
    String aggregateType;
    String aggregateId;
}
