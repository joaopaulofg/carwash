package com.example.carwash.repository;

import com.example.carwash.domain.ServiceOrder;
import com.example.carwash.domain.ServiceStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Long> {

    List<ServiceOrder> findByStatus(ServiceStatus status);
}

