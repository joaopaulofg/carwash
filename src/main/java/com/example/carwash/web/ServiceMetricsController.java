package com.example.carwash.web;

import com.example.carwash.monitoring.ServiceMetricsCollector;
import com.example.carwash.monitoring.ServiceMetricsCollector.ServiceMetricsSnapshot;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
public class ServiceMetricsController {

    private final ServiceMetricsCollector metricsCollector;

    public ServiceMetricsController(ServiceMetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
    }

    @GetMapping("/services")
    public ServiceMetricsSnapshot servicesMetrics() {
        return metricsCollector.snapshot();
    }
}
