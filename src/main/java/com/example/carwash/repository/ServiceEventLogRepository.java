package com.example.carwash.repository;

import com.example.carwash.domain.ServiceEventLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceEventLogRepository extends JpaRepository<ServiceEventLog, Long> {

    List<ServiceEventLog> findByServiceOrderIdOrderByOccurredAtAsc(Long serviceOrderId);
}

