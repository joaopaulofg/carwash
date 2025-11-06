# Carwash Event-Driven Demo

Projeto de exemplo que demonstra uma arquitetura orientada a eventos aplicada a um lava-jato utilizando Spring Boot, Spring Data JPA e H2.

## Visão geral

- **Serviços de domínio** (`VehicleServiceManager`) publicam eventos de aplicação (`VehicleServiceStartedEvent`, `VehicleServiceCompletedEvent`) sempre que uma ordem de serviço inicia ou termina.
- **Listeners desacoplados** respondem aos eventos para registrar logs no banco (`ServiceOrderEventListener`) e alimentar métricas em memória (`ServiceMetricsListener`).
- **H2 in-memory** armazena clientes, veículos, catálogo de serviços e eventos para facilitar os testes.
- **Endpoints REST** expõem a orquestração de ordens e consulta das métricas.

## Executando

1. Instale uma JDK 17 ou superior.
2. Na raiz do projeto execute:

```bash
mvn spring-boot:run
```

> Se preferir o Maven Wrapper, instale o Maven localmente ou ajuste o projeto para incluir `mvnw/mvnw.cmd`.

3. Acesse `http://localhost:8080` e utilize os endpoints:

- `GET /api/service-orders` lista as ordens existentes.
- `POST /api/service-orders` cria uma nova ordem.
- `POST /api/service-orders/{id}/start` inicia uma ordem (dispara evento de início).
- `POST /api/service-orders/{id}/complete` finaliza uma ordem (dispara evento de conclusão).
- `GET /api/service-orders/{id}/events` consulta o histórico de eventos persistido.
- `GET /api/metrics/services` consulta as métricas derivadas dos eventos.
- `GET /h2-console` abre o console do H2 (`jdbc:h2:mem:carwash`, usuário `sa`, senha vazia).

## Próximos passos

- Introduzir mensageria (por exemplo, Kafka ou RabbitMQ) para trocar os eventos entre microsserviços.
- Tornar os listeners assíncronos (`@Async`) e resilientes com dead-letter, retries e DLQs.
- Criar testes automatizados para validar o fluxo de eventos e regras de negócio.

