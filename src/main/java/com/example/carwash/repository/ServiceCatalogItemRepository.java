package com.example.carwash.repository;

import com.example.carwash.domain.ServiceCatalogItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceCatalogItemRepository extends JpaRepository<ServiceCatalogItem, Long> {

    Optional<ServiceCatalogItem> findByCode(String code);
}

