package com.example.carwash.monitoring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ServiceMetricsCollector {

    private final ConcurrentMap<String, LongAdder> startedByService = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, LongAdder> completedByService = new ConcurrentHashMap<>();
    private final LongAdder activeServices = new LongAdder();

    public void registerStart(String serviceName) {
        startedByService.computeIfAbsent(serviceName, key -> new LongAdder()).increment();
        activeServices.increment();
    }

    public void registerCompletion(String serviceName) {
        completedByService.computeIfAbsent(serviceName, key -> new LongAdder()).increment();
        activeServices.decrement();
    }

    public ServiceMetricsSnapshot snapshot() {
        Map<String, Long> started = startedByService.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().sum()));

        Map<String, Long> completed = completedByService.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().sum()));

        return new ServiceMetricsSnapshot(started, completed, activeServices.sum());
    }

    public record ServiceMetricsSnapshot(
            Map<String, Long> startedByService,
            Map<String, Long> completedByService,
            long servicesInProgress) {
    }
}
