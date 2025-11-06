package com.example.carwash.web;

import com.example.carwash.domain.ServiceEventLog;
import java.time.LocalDateTime;

public record ServiceEventLogResponse(
        Long id,
        Long serviceOrderId,
        String eventType,
        String description,
        LocalDateTime occurredAt) {

    public static ServiceEventLogResponse from(ServiceEventLog log) {
        return new ServiceEventLogResponse(
                log.getId(),
                log.getServiceOrderId(),
                log.getEventType(),
                log.getDescription(),
                log.getOccurredAt());
    }
}
