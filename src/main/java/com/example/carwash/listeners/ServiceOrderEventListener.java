package com.example.carwash.listeners;

import com.example.carwash.domain.ServiceEventLog;
import com.example.carwash.events.VehicleServiceCompletedEvent;
import com.example.carwash.events.VehicleServiceStartedEvent;
import com.example.carwash.repository.ServiceEventLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
@Order(0)
public class ServiceOrderEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceOrderEventListener.class);
    private final ServiceEventLogRepository eventLogRepository;

    public ServiceOrderEventListener(ServiceEventLogRepository eventLogRepository) {
        this.eventLogRepository = eventLogRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onServiceStarted(VehicleServiceStartedEvent event) {
        LOGGER.info("Service '{}' started for {} at {}", event.getServiceName(),
                event.getLicensePlate(), event.getStartedAt());

        String description = "Service started at " + event.getStartedAt();
        eventLogRepository.save(
                new ServiceEventLog(event.getServiceOrderId(), "SERVICE_STARTED", description));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onServiceCompleted(VehicleServiceCompletedEvent event) {
        LOGGER.info("Service '{}' completed for {} at {}", event.getServiceName(),
                event.getLicensePlate(), event.getCompletedAt());

        String description = "Service completed at " + event.getCompletedAt();
        eventLogRepository.save(
                new ServiceEventLog(event.getServiceOrderId(), "SERVICE_COMPLETED", description));
    }
}

