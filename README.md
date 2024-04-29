## Table of contents

1. [Introduction](#introduction)  
   1.1 [Project Overview](#11-project-overview)  
   1.2 [Key Features](#12-key-features)  
   1.3 [Technologies Used](#13-technologies-used)

2. Architecture  
   2.1 System Architecture  
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

![Architecture](docs/images/architecture.png)  
The architecture of this project is designed to accommodate complex business processes within the domain of messaging and chat operations, using a flexible and scalable approach that leverages Domain-Driven Design (DDD), event sourcing, and reactive programming principles. While structured as a monolithic application for ease of development and deployment, it maintains a clear separation of
concerns through its modular design, enabling potential evolution into a microservices architecture.
Each core component of the system — including `saga_orchestrator`, `user`, `room`, and `message` — encapsulates its own business logic and state management, coordinated through a centralized saga orchestration mechanism that ensures transactional consistency across various operations. This design allows the system to handle complex workflows such as user registration, room management, and message
handling
in a cohesive and robust manner.
The adoption of event sourcing as a fundamental architectural pattern not only enables the system to preserve a complete history of all changes but also provides the flexibility to respond to future requirements and scaling needs. Coupled with a reactive programming approach, the system is well-equipped to handle a high volume of messages with efficiency and resilience, providing real-time
feedback and interactions to the users.
In the following sections, we'll dive deeper into the individual architectural components and their roles within the larger system.
