package com.example.carwash.events;

import com.example.carwash.domain.ServiceOrder;
import java.time.LocalDateTime;

public class VehicleServiceCompletedEvent {

    private final Long serviceOrderId;
    private final String serviceName;
    private final String licensePlate;
    private final LocalDateTime completedAt;

    public VehicleServiceCompletedEvent(ServiceOrder order, LocalDateTime completedAt) {
        this.serviceOrderId = order.getId();
        this.serviceName = order.getService().getName();
        this.licensePlate = order.getVehicle().getLicensePlate();
        this.completedAt = completedAt;
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

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}

