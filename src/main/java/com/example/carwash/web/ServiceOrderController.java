package com.example.carwash.web;

import com.example.carwash.domain.ServiceOrder;
import com.example.carwash.domain.ServiceStatus;
import com.example.carwash.repository.ServiceOrderRepository;
import com.example.carwash.service.VehicleServiceManager;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/service-orders")
@Validated
public class ServiceOrderController {

    private final VehicleServiceManager serviceManager;
    private final ServiceOrderRepository orderRepository;

    public ServiceOrderController(VehicleServiceManager serviceManager,
            ServiceOrderRepository orderRepository) {
        this.serviceManager = serviceManager;
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public List<ServiceOrderResponse> listAll() {
        return orderRepository.findAll()
                .stream()
                .map(ServiceOrderResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/pending")
    public List<ServiceOrderResponse> listPending() {
        return serviceManager.listPendingOrders()
                .stream()
                .map(ServiceOrderResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ServiceOrderResponse getById(@PathVariable Long id) {
        ServiceOrder order = serviceManager.getOrderOrThrow(id);
        return ServiceOrderResponse.from(order);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceOrderResponse create(@Valid @RequestBody CreateServiceOrderRequest request) {
        ServiceOrder created = serviceManager.createOrder(request.getCustomerId(),
                request.getVehicleId(),
                request.getServiceId(),
                request.getScheduledFor(),
                request.getNotes());
        return ServiceOrderResponse.from(created);
    }

    @PostMapping("/{id}/start")
    public ServiceOrderResponse start(@PathVariable Long id) {
        ServiceOrder started = serviceManager.startService(id);
        return ServiceOrderResponse.from(started);
    }

    @PostMapping("/{id}/complete")
    public ServiceOrderResponse complete(@PathVariable Long id) {
        ServiceOrder completed = serviceManager.completeService(id);
        return ServiceOrderResponse.from(completed);
    }

    @GetMapping("/{id}/events")
    public List<ServiceEventLogResponse> events(@PathVariable Long id) {
        return serviceManager.listEventLog(id)
                .stream()
                .map(ServiceEventLogResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/status/{status}")
    public List<ServiceOrderResponse> byStatus(@PathVariable ServiceStatus status) {
        return orderRepository.findByStatus(status)
                .stream()
                .map(ServiceOrderResponse::from)
                .collect(Collectors.toList());
    }
}
