# Arquitetura Orientada a Eventos – Sistema de Lava-Rápido

Este documento detalha a implementação de um sistema de lava-rápido desenvolvido em Spring Boot com foco em **arquitetura orientada a eventos**, persistência em **H2** e monitoramento em tempo real por meio de métricas internas.

## 1. Visão Geral da Solução

- **Camada de domínio** modela clientes, veículos, catálogo de serviços e ordens de serviço (pacote `com.example.carwash.domain`).
- **Serviço de aplicação** (`VehicleServiceManager`) orquestra o fluxo de negócio e publica eventos ao iniciar e finalizar serviços.
- **Eventos de aplicação** (`VehicleServiceStartedEvent`, `VehicleServiceCompletedEvent`) propagam mudanças significativas de estado.
- **Listeners desacoplados** persistem logs (auditoria) e atualizam métricas em memória.
- **API REST** expõe operações de CRUD, transições de estado e consultas de métricas/histórico.
- **Inicialização da base** injeta dados de exemplo logo no bootstrap da aplicação.

## 2. Domínio e Persistência

| Entidade | Descrição | Arquivo |
| --- | --- | --- |
| `Customer` | Representa o cliente que solicita serviços (`customers`). | `src/main/java/com/example/carwash/domain/Customer.java` |
| `Vehicle` | Veículo vinculado a um cliente; possui placa, modelo (`vehicles`). | `src/main/java/com/example/carwash/domain/Vehicle.java` |
| `ServiceCatalogItem` | Catálogo de serviços disponíveis, com preço e duração estimada. | `src/main/java/com/example/carwash/domain/ServiceCatalogItem.java` |
| `ServiceOrder` | Ordem de serviço que liga cliente, veículo e serviço; possui `ServiceStatus`. | `src/main/java/com/example/carwash/domain/ServiceOrder.java` |
| `ServiceEventLog` | Histórico persistido de eventos capturados pelos listeners. | `src/main/java/com/example/carwash/domain/ServiceEventLog.java` |

Os repositórios Spring Data JPA (`com.example.carwash.repository`) encapsulam a persistência em H2 (`spring.datasource` definido em `src/main/resources/application.yml`). O Hibernate gera o schema automaticamente via `ddl-auto: update` e mantém logs formatados (`hibernate.format_sql`).

## 3. Serviço de Aplicação e Fluxo de Negócio

`VehicleServiceManager` (`src/main/java/com/example/carwash/service/VehicleServiceManager.java`) concentra as regras:

1. **Criação de ordens** – valida cliente, veículo e serviço (assegura que o veículo pertence ao cliente) e persiste uma ordem com status `REQUESTED`.
2. **Início de serviço** – somente ordens `REQUESTED` podem migrar; registra `startedAt`, troca status para `IN_PROGRESS` e publica `VehicleServiceStartedEvent`.
3. **Conclusão de serviço** – aceita apenas ordens `IN_PROGRESS`; registra `completedAt`, troca status para `COMPLETED` e publica `VehicleServiceCompletedEvent`.
4. **Consulta** – expõe leitura de ordens, pendências e histórico de eventos.

O serviço injeta `Clock` (definido em `CarwashApplication.java`) para facilitar testes e garantir horários consistentes.

## 4. Eventos de Aplicação

Eventos representam fatos relevantes de domínio que outros componentes podem consumir sem forte acoplamento.

- `VehicleServiceStartedEvent` (`src/main/java/com/example/carwash/events/VehicleServiceStartedEvent.java`) carrega ID da ordem, nome do serviço, placa do veículo e timestamp de início.
- `VehicleServiceCompletedEvent` (`src/main/java/com/example/carwash/events/VehicleServiceCompletedEvent.java`) carrega informações similares com o horário de conclusão.

### Publicação

O `VehicleServiceManager` usa `ApplicationEventPublisher` para publicar eventos imediatamente após a mudança de estado. A anotação `@Transactional` garante que os eventos só sejam disparados quando a transação for confirmada.

### Consumo

Dois listeners anotados com `@TransactionalEventListener` (`com.example.carwash.listeners`) recebem os eventos dentro do mesmo contexto transacional, assegurando consistência:

1. **`ServiceOrderEventListener`** (`src/main/java/com/example/carwash/listeners/ServiceOrderEventListener.java`) grava uma linha no `ServiceEventLog` e registra logs (`Logger`). Assim, criamos uma trilha de auditoria persistente dos eventos disparados.
2. **`ServiceMetricsListener`** (`src/main/java/com/example/carwash/listeners/ServiceMetricsListener.java`) alimenta o coletor de métricas em memória com incrementos/decrementos oportunos.

Essa abordagem possibilita adicionar novos consumidores (notificações por e-mail, integração com faturamento etc.) sem alterar o serviço principal.

## 5. Métricas e Monitoramento

`ServiceMetricsCollector` (`src/main/java/com/example/carwash/monitoring/ServiceMetricsCollector.java`) mantém contadores em memória:

- Quantidade de serviços iniciados por tipo.
- Quantidade de serviços concluídos por tipo.
- Total de serviços atualmente em progresso (`LongAdder`).

Os métodos `registerStart` e `registerCompletion` são chamados pelos listeners quando eventos são capturados. A classe expõe um snapshot imutável (`ServiceMetricsSnapshot`) acessado via `ServiceMetricsController` (`src/main/java/com/example/carwash/web/ServiceMetricsController.java`) no endpoint `GET /api/metrics/services`.

### Possíveis Evoluções

- Exportar métricas para Prometheus (Micrometer).
- Transformar listeners em assíncronos com `@Async`.
- Enviar eventos para uma fila externa (Kafka/RabbitMQ) para monitoramento distribuído.

## 6. API REST

`ServiceOrderController` (`src/main/java/com/example/carwash/web/ServiceOrderController.java`) oferece endpoints:

| Endpoint | Método | Descrição |
| --- | --- | --- |
| `/api/service-orders` | `GET` | Lista todas as ordens. |
| `/api/service-orders/pending` | `GET` | Lista ordens com status `REQUESTED`. |
| `/api/service-orders/{id}` | `GET` | Detalha uma ordem específica. |
| `/api/service-orders` | `POST` | Cria ordem (requisição `CreateServiceOrderRequest`). |
| `/api/service-orders/{id}/start` | `POST` | Inicia ordem (dispara evento de início). |
| `/api/service-orders/{id}/complete` | `POST` | Conclui ordem (dispara evento de finalização). |
| `/api/service-orders/{id}/events` | `GET` | Histórico de eventos persistidos para a ordem. |
| `/api/service-orders/status/{status}` | `GET` | Filtra ordens por status. |

`ServiceMetricsController` expõe `GET /api/metrics/services` para obter o snapshot de métricas.

Erros são padronizados por `RestExceptionHandler` (`src/main/java/com/example/carwash/web/RestExceptionHandler.java`), convertendo exceções de negócio em respostas HTTP adequadas.

## 7. Inicialização de Dados

`CarwashDataInitializer` (`src/main/java/com/example/carwash/config/CarwashDataInitializer.java`) popula a H2 com:

- Dois clientes (Anna e João).
- Dois veículos associados.
- Três serviços no catálogo.
- Três ordens iniciais com diferentes datas programadas.

Ao subir o aplicativo, a API já possui dados suficientes para testar o fluxo completo de eventos e métricas.

## 8. Execução Local

1. Certifique-se de ter JDK 17+ instalado.
2. Na raiz do repositório, execute `./mvnw spring-boot:run` (Linux/macOS) ou `mvnw.cmd spring-boot:run` (Windows).
3. Acesse:
   - `http://localhost:8080/api/service-orders` para listar ordens.
   - `http://localhost:8080/api/service-orders/{id}/start` e `/complete` para acionar eventos.
   - `http://localhost:8080/api/service-orders/{id}/events` para verificar o log persistido.
   - `http://localhost:8080/api/metrics/services` para acompanhar contadores.
   - `http://localhost:8080/h2-console` (JDBC `jdbc:h2:mem:carwash`, usuário `sa`, senha vazia).

## 9. Como os Eventos Suportam a Arquitetura

- **Desacoplamento**: o produtor (serviço de ordens) não conhece consumidores específicos; listeners podem ser adicionados/removidos sem impactar a lógica principal.
- **Extensibilidade**: novos fluxos (notificações, faturamento, BI) podem ouvir os mesmos eventos e reagir independentemente.
- **Observabilidade**: o fluxo de métricas e auditoria é uma consequência dos eventos, não um requisito acoplado ao caso de uso principal.
- **Consistência**: `@TransactionalEventListener` garante que eventos só sejam disparados após a confirmação de transações, evitando logs ou métricas inconsistentes.

## 10. Próximos Passos Sugeridos

1. **Mensageria Externa**: publicar os eventos em um broker (Kafka/RabbitMQ) para integrar serviços e escalar horizontalmente.
2. **Processamento Assíncrono**: tornar listeners assíncronos para não bloquear a requisição principal.
3. **Micrometer/Prometheus**: exportar métricas para um sistema de monitoramento consolidado.
4. **Dashboards**: consumir o endpoint de métricas em um frontend ou painel de observabilidade.
5. **Testes Automatizados**: criar testes de integração focados em eventos e verificação das métricas/relatórios gerados.

Com esse fluxo, o projeto torna-se uma base sólida para entender e experimentar arquitetura orientada a eventos dentro do ecossistema Spring.

