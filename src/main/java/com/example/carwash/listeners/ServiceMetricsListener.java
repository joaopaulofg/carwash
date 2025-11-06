package com.example.carwash.listeners;

import com.example.carwash.events.VehicleServiceCompletedEvent;
import com.example.carwash.events.VehicleServiceStartedEvent;
import com.example.carwash.monitoring.ServiceMetricsCollector;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ServiceMetricsListener {

    private final ServiceMetricsCollector metricsCollector;

    public ServiceMetricsListener(ServiceMetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
    }

    @TransactionalEventListener
    public void onServiceStarted(VehicleServiceStartedEvent event) {
        metricsCollector.registerStart(event.getServiceName());
    }

    @TransactionalEventListener
    public void onServiceCompleted(VehicleServiceCompletedEvent event) {
        metricsCollector.registerCompletion(event.getServiceName());
    }
}

