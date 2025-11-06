package com.example.carwash.service;

import com.example.carwash.domain.Customer;
import com.example.carwash.domain.ServiceCatalogItem;
import com.example.carwash.domain.ServiceEventLog;
import com.example.carwash.domain.ServiceOrder;
import com.example.carwash.domain.ServiceStatus;
import com.example.carwash.domain.Vehicle;
import com.example.carwash.events.VehicleServiceCompletedEvent;
import com.example.carwash.events.VehicleServiceStartedEvent;
import com.example.carwash.repository.CustomerRepository;
import com.example.carwash.repository.ServiceCatalogItemRepository;
import com.example.carwash.repository.ServiceEventLogRepository;
import com.example.carwash.repository.ServiceOrderRepository;
import com.example.carwash.repository.VehicleRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleServiceManager {

    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final ServiceCatalogItemRepository catalogRepository;
    private final ServiceOrderRepository orderRepository;
    private final ServiceEventLogRepository eventLogRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public VehicleServiceManager(CustomerRepository customerRepository,
            VehicleRepository vehicleRepository,
            ServiceCatalogItemRepository catalogRepository,
            ServiceOrderRepository orderRepository,
            ServiceEventLogRepository eventLogRepository,
            ApplicationEventPublisher eventPublisher,
            Clock clock) {
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.catalogRepository = catalogRepository;
        this.orderRepository = orderRepository;
        this.eventLogRepository = eventLogRepository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Transactional
    public ServiceOrder createOrder(Long customerId, Long vehicleId, Long serviceId,
            LocalDateTime scheduledFor, String notes) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Veículo não encontrado"));
        ServiceCatalogItem service = catalogRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Serviço não encontrado"));

        if (!vehicle.getOwner().getId().equals(customer.getId())) {
            throw new IllegalStateException("Veículo informado não pertence ao cliente");
        }

        ServiceOrder order = new ServiceOrder(customer, vehicle, service, scheduledFor, notes);
        return orderRepository.save(order);
    }

    @Transactional
    public ServiceOrder startService(Long orderId) {
        ServiceOrder order = getOrderOrThrow(orderId);

        if (order.getStatus() != ServiceStatus.REQUESTED) {
            throw new IllegalStateException("Somente ordens solicitadas podem ser iniciadas");
        }

        LocalDateTime now = LocalDateTime.now(clock);
        order.markInProgress(now);

        VehicleServiceStartedEvent event = new VehicleServiceStartedEvent(order, now);
        eventPublisher.publishEvent(event);
        return order;
    }

    @Transactional
    public ServiceOrder completeService(Long orderId) {
        ServiceOrder order = getOrderOrThrow(orderId);

        if (order.getStatus() != ServiceStatus.IN_PROGRESS) {
            throw new IllegalStateException("Somente ordens em progresso podem ser finalizadas");
        }

        LocalDateTime now = LocalDateTime.now(clock);
        order.markCompleted(now);

        VehicleServiceCompletedEvent event = new VehicleServiceCompletedEvent(order, now);
        eventPublisher.publishEvent(event);
        return order;
    }

    @Transactional(readOnly = true)
    public ServiceOrder getOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Ordem de serviço não encontrada"));
    }

    @Transactional(readOnly = true)
    public List<ServiceOrder> listPendingOrders() {
        return orderRepository.findByStatus(ServiceStatus.REQUESTED);
    }

    @Transactional(readOnly = true)
    public List<ServiceEventLog> listEventLog(Long orderId) {
        return eventLogRepository.findByServiceOrderIdOrderByOccurredAtAsc(orderId);
    }
}

