## Table of contents

1. [Introduction](#introduction)  
   1.1 [Project Overview](#11-project-overview)  
   1.2 [Key Features](#12-key-features)  
   1.3 [Technologies Used](#13-technologies-used)

2. [Architecture](#2-architecture)  
   2.1 [System Architecture](#21-system-architecture)  
   2.2 Saga Orchestration  
   2.3 Domain-Driven Design Overview  
   2.4 Event Sourcing Details  
   2.5 Reactive Programming Approach

3. PI Documentation  
   3.1 Endpoints  
   3.2 Usage Examples

4. WebSocket Communication  
   4.1 Event Types  
   4.2 Usage and Examples

5. Database Schema  
   5.1 Diagrams  
   5.2 Descriptions

6. Testing  
   6.1 Unit Tests  
   6.2 Integration Tests

7. Deployment  
   7.1 Recommended Deployment Practices  
   7.2 Environment Setup

8. Incomplete Features  
   8.1 Current Limitations  
   8.1 Planned Features

9. Contributing  
   9.1 How to Contribute  
   9.2 Code of Conduct  
   9.3 Contributors

9. License

10. Contact Information

## 1. Introduction

### 1.1 Project Overview

The `saga-orchestrator-ddd-chat` is an advanced chat application that leverages Domain-Driven Design (DDD) principles and Saga orchestration to provide a robust solution for real-time messaging. This project is designed to showcase how complex business transactions (sagas) that span multiple microservices can be coordinated in a reactive and event-driven architecture. It's ideal for developers
looking to understand the implementation of sagas in microservices architecture or those developing complex systems requiring reliable communication mechanisms.

### 1.2 Key Features

- Real-time Messaging: Users can send and receive messages instantaneously.
- Saga Orchestration: Ensures that all steps in a business transaction are completed successfully or compensated if any step fails.
- Domain-Driven Design: Structures around the business domain, making it easier to understand and align with business requirements.
- Event Sourcing: Persist changes to the application state as a sequence of events, allowing for robust auditing and historical state reconstruction.
- Reactive Programming: Builds an asynchronous, non-blocking backend that efficiently handles concurrent user requests.

### 1.3 Technologies Used

- Micronaut Framework: Used for building modular, easily testable microservice applications.
- Kotlin: The primary language for building the application, offering conciseness and safety.
- Project Reactor: Provides a reactive programming model, which helps in building scalable and efficient applications.
- Jackson: For JSON serialization and deserialization.
- WebSocket Communication: For real-time bi-directional communication between clients and servers.
- PostgreSQL/MySQL: As the database system, depending on the deployment.

## 2. Architecture

The architecture of this project is designed to accommodate complex business processes within the domain of messaging and chat operations, using a flexible and scalable approach that leverages Domain-Driven Design (DDD), event sourcing, and reactive programming principles. While structured as a monolithic application for ease of development and deployment, it maintains a clear separation of
concerns through its modular design, enabling potential evolution into a microservices architecture.
Each core component of the system — including `saga_orchestrator`, `user`, `room`, and `message` — encapsulates its own business logic and state management, coordinated through a centralized saga orchestration mechanism that ensures transactional consistency across various operations. This design allows the system to handle complex workflows such as user registration, room management, and message
handling
in a cohesive and robust manner.
The adoption of event sourcing as a fundamental architectural pattern not only enables the system to preserve a complete history of all changes but also provides the flexibility to respond to future requirements and scaling needs. Coupled with a reactive programming approach, the system is well-equipped to handle a high volume of messages with efficiency and resilience, providing real-time
feedback and interactions to the users.
In the following sections, we'll dive deeper into the individual architectural components and their roles within the larger system.

### 2.1 System Architecture

The system architecture is conceived as a unified platform that supports real-time messaging and chat functionalities. It is a confluence of distinct yet interrelated modules that operate both independently and collaboratively, forming a cohesive ecosystem for messaging services. The following diagram provides a visual overview of the system's architectural design:
![Architecture](docs/images/architecture.png)

- **Core Domains**  
  At the heart of the architecture are the core domain components:
    - `User`: Manages user-related operations such as registration and profile management.
    - `Room`: Handles room-related functionalities including creation, membership management, and room settings.
    - `Message`: Takes care of all aspects of messaging, from sending and receiving messages to message translation and status updates.

  These domains are designed following DDD principles, allowing for a clear separation of concerns and focused domain models.


- **Saga Orchestration**  
  The `saga_orchestrator` acts as the central coordinator for distributed transactions and complex business processes. It ensures data consistency and orchestrates the flow of events across different domain boundaries.


- **Infrastructure Services**  
  Infrastructure services provide support functionalities such as:

    - `read_service`: Responsible for read operations, storing and retrieving the state of domain objects for query operations.
    - `websocket_service`: Handles real-time communication with connected clients, ensuring timely updates and notifications are pushed via websockets.


- **Data Persistence**  
  Event sourcing is employed to persist the state changes as a sequence of events. Each core domain has its event table (`saga_event`, `user_domain_event`, `room_domain_event`, `message_domain_event`) which records all domain events that have occurred.


- **View Models**  
  The read side of the system is represented by view models (`user_view`, `room_view`, `message_view`, `room_members`) that are optimized for queries and provide the necessary data for the read service and other query operations.

This architecture provides a robust foundation for scaling, maintenance, and future enhancements.

### 2.2 Saga Orchestration

The saga orchestration mechanism is a critical aspect of the system architecture, designed to manage distributed transactions across various bounded contexts through a state machine implemented in the `AbstractSagaStateManager` class. This orchestration ensures that all transactions are consistently and reliably handled, leveraging event sourcing to maintain and rebuild the state of each saga.
The diagram below
illustrates the transitions and states:
![Saga State Machine](docs/images/saga-state-machine.png)

- **The Saga State Machine**  
  The state machine at the core of saga orchestration initiates in a READY state and transitions through various stages based on the flow of events. It progresses to INITIATED upon a START event, broadcasting INITIATED event to relevant domains. Each domain processes this initiation asynchronously and responds with either APPROVED or REJECTED events.


- **Handling Events and State Transitions**
  In the IN_APPROVING state, the saga orchestrator verifies transaction completion by checking for the necessary approvals from all involved services. If the conditions are met, it transitions to the COMPLETE state and issues a COMPLETED event. Conversely, upon receiving a REJECTED event, the saga orchestrator moves to an error state and emits an ERROR event, which is consumed only
  by `websocket_service`.


- **Compensation Logic and Error Handling**  
  The REJECTED event is critical as it is consumed not only by the `saga_orchestrator` but also by all participating domains. This triggers compensation logic across the system, where necessary rollback or corrective actions are taken. The `saga _orchestrator` additionally emits an ERROR event, which is specifically consumed by the `websocket_service` to relay error information back to the
  user,
  ensuring transparency and responsiveness.


- **Integration with Event Sourcing**  
  Employing event sourcing, the `saga_orchestrator` updates and rebuilds the state of each transaction dynamically, based on the events processed. This approach enhances the resilience and scalability of the system by decoupling state management from the transactional operations, thereby allowing for more robust error handling and recovery mechanisms.


- **Collaboration with read_service and websocket_service**  
  Both `read_service` and `websocket_service` play crucial roles in handling the COMPLETED and ERROR events. While `read_service` updates views and read models ensuring data consistency, `websocket_service` facilitates real-time communication with users, enhancing the interactive experience by providing timely updates on the status of transactions.

- **Customization for Specific Domains**  
  Through the abstract implementation provided by `AbstractSagaStateManager`, each saga can be customized for specific domain needs, defining its unique command, dto, events, and completion logic. For example, the `RoomCreateSaga` requires approvals from `room_service` and `user_service` to conclude successfully.

```kotlin
class RoomCreateSaga(
    val operationId: UUID,
    private val responsibleUserId: UUID,
) : AbstractSagaStateManager<RoomCreateCommand, RoomDTO>() {
    override fun startEvent() = SagaEventType.ROOM_CREATE_START
    override fun approveEvent() = SagaEventType.ROOM_CREATE_APPROVED
    override fun rejectEvent() = SagaEventType.ROOM_CREATE_REJECTED

    override fun isComplete() = approvedServices.containsAll(
        listOf(
            ServiceEnum.ROOM_SERVICE, ServiceEnum.USER_SERVICE
        )
    )

    override fun mainDomainService() = ServiceEnum.ROOM_SERVICE

    override fun createInitiatedResponseEvent() =
        SagaEvent(SagaEventType.ROOM_CREATE_INITIATED, operationId, ServiceEnum.SAGA_SERVICE, responsibleUserId, command)

    override fun createCompletedResponseEvent() =
        SagaEvent(SagaEventType.ROOM_CREATE_COMPLETED, operationId, ServiceEnum.SAGA_SERVICE, responsibleUserId, dto)

    override fun createErrorResponseEvent() =
        SagaEvent(SagaEventType.ROOM_CREATE_ERROR, operationId, ServiceEnum.SAGA_SERVICE, responsibleUserId, errorDto!!)

    ...
}
```
