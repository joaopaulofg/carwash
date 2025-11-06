package com.example.carwash.config;

import com.example.carwash.domain.Customer;
import com.example.carwash.domain.ServiceCatalogItem;
import com.example.carwash.domain.ServiceOrder;
import com.example.carwash.domain.Vehicle;
import com.example.carwash.repository.CustomerRepository;
import com.example.carwash.repository.ServiceCatalogItemRepository;
import com.example.carwash.repository.ServiceOrderRepository;
import com.example.carwash.repository.VehicleRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CarwashDataInitializer implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final ServiceCatalogItemRepository serviceCatalogRepository;
    private final ServiceOrderRepository orderRepository;

    public CarwashDataInitializer(CustomerRepository customerRepository,
            VehicleRepository vehicleRepository,
            ServiceCatalogItemRepository serviceCatalogRepository,
            ServiceOrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.serviceCatalogRepository = serviceCatalogRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (serviceCatalogRepository.count() > 0) {
            return;
        }

        Customer anna = customerRepository.save(
                new Customer("Anna Ribeiro", "anna.ribeiro@example.com", "(11) 98888-7777"));
        Customer joao = customerRepository.save(
                new Customer("João Lima", "joao.lima@example.com", "(11) 97777-1111"));

        Vehicle annaCar = vehicleRepository
                .save(new Vehicle("BRA0A23", "Honda HR-V 2023", anna));
        Vehicle joaoCar = vehicleRepository
                .save(new Vehicle("SPX9D45", "Fiat Pulse 2022", joao));

        ServiceCatalogItem simpleWash = serviceCatalogRepository.save(
                new ServiceCatalogItem("BASICO", "Lavagem Rápida",
                        "Lavagem externa padrão com secagem manual e cera spray.",
                        30, new BigDecimal("49.90")));

        ServiceCatalogItem premiumWash = serviceCatalogRepository.save(
                new ServiceCatalogItem("PREMIUM", "Lavagem Premium",
                        "Lavagem completa interna e externa com higienização de estofados.",
                        90, new BigDecimal("149.90")));

        ServiceCatalogItem detail = serviceCatalogRepository.save(
                new ServiceCatalogItem("DETAIL", "Detailing Completo",
                        "Detecção de micro riscos, polimento técnico e vitrificação.",
                        180, new BigDecimal("299.90")));

        orderRepository.save(new ServiceOrder(anna, annaCar, simpleWash,
                LocalDateTime.now().plusHours(1), "Cliente aguardará na loja"));
        orderRepository.save(new ServiceOrder(joao, joaoCar, premiumWash,
                LocalDateTime.now().plusDays(1), "Aplicar proteção cerâmica extra"));
        orderRepository.save(new ServiceOrder(joao, joaoCar, detail,
                LocalDateTime.now().plusDays(3), "Agendar em período com clima seco"));
    }
}

