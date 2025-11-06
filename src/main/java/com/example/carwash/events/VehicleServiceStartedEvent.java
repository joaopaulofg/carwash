package com.example.carwash.events;

import com.example.carwash.domain.ServiceOrder;
import java.time.LocalDateTime;

public class VehicleServiceStartedEvent {

    private final Long serviceOrderId;
    private final String serviceName;
    private final String licensePlate;
    private final LocalDateTime startedAt;

    public VehicleServiceStartedEvent(ServiceOrder order, LocalDateTime startedAt) {
        this.serviceOrderId = order.getId();
        this.serviceName = order.getService().getName();
        this.licensePlate = order.getVehicle().getLicensePlate();
        this.startedAt = startedAt;
    }

    public Long getServiceOrderId() {
        return serviceOrderId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }
}

