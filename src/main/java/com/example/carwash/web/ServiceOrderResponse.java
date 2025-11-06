package com.example.carwash.web;

import com.example.carwash.domain.ServiceOrder;
import com.example.carwash.domain.ServiceStatus;
import java.time.LocalDateTime;

public record ServiceOrderResponse(
        Long id,
        String customerName,
        String vehiclePlate,
        String vehicleModel,
        String serviceName,
        ServiceStatus status,
        LocalDateTime requestedAt,
        LocalDateTime scheduledFor,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        String notes) {

    public static ServiceOrderResponse from(ServiceOrder order) {
        return new ServiceOrderResponse(
                order.getId(),
                order.getCustomer().getName(),
                order.getVehicle().getLicensePlate(),
                order.getVehicle().getModel(),
                order.getService().getName(),
                order.getStatus(),
                order.getRequestedAt(),
                order.getScheduledFor(),
                order.getStartedAt(),
                order.getCompletedAt(),
                order.getNotes());
    }
}