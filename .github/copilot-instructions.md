# Copilot Instructions for User Service (DDD Architecture)

## Project Architecture
This is a Java microservice (`user-service`) following **Domain-Driven Design (DDD)** principles with multi-tenant/multi-domain support. The service manages users, roles, and permissions with PostgreSQL and Redis in a containerized environment.

## DDD Folder Structure
```
ai-agent-service/
└── src
    ├── main
    │   ├── java
    │   │   └── com/nguyentrungtung/aiagent/
    │   │       │
    │   │       ├── AiAgentApplication.java      // --- Điểm khởi chạy Spring Boot
    │   │       │
    │   │       ├── agent/                       // <<< TRÁI TIM CỦA HỆ THỐNG AGENTIC >>>
    │   │       │   ├── MasterAgentService.java  // --- Service điều phối cấp cao nhất
    │   │       │   │
    │   │       │   ├── memory/                  // --- Trí nhớ (Ngữ cảnh & Lịch sử)
    │   │       │   │   ├── AgentMemoryService.java
    │   │       │   │   └── repository/
    │   │       │   │       └── ConversationHistoryRepository.java
    │   │       │   │
    │   │       │   ├── prompt/                  // --- Bộ não (Nơi chứa các mẫu prompt)
    │   │       │   │   └── SystemPromptFactory.java
    │   │       │   │
    │   │       │   ├── request/                 // --- Định nghĩa đối tượng Request cho Agent
    │   │       │   │   └── AgentRequest.java
    │   │       │   │
    │   │       │   ├── response/                // --- Định nghĩa đối tượng Response cho Agent
    │   │       │   │   └── AgentResponse.java
    │   │       │   │
    │   │       │   ├── tools/                   // --- Các Worker (Công cụ Agent có thể dùng)
    │   │       │   │   ├── Tool.java
    │   │       │   │   ├── ToolRegistry.java
    │   │       │   │   └── impl/
    │   │       │   │       ├── PricingInfoTool.java
    │   │       │   │       ├── WebsiteCreationTool.java
    │   │       │   │       └── WebsiteTemplateTool.java
    │   │       │   │
    │   │       │   └── workflow/                // --- Nơi định nghĩa và triển khai các WORKFLOW
    │   │       │       ├── chain/
    │   │       │       │   ├── ChainWorkflow.java (Interface)
    │   │       │       │   ├── ConsultingChainWorkflow.java (Implementation)
    │   │       │       │   └── WorkflowStep.java (Interface)
    │   │       │       │
    │   │       │       ├── orchestrator/
    │   │       │       │   ├── DefaultOrchestrationWorkflow.java (Implementation)
    │   │       │       │   ├── OrchestrationWorkflow.java (Interface)
    │   │       │       │   └── Worker.java (Interface)
    │   │       │       │
    │   │       │       └── route/
    │   │       │           ├── IntentBasedRoutingWorkflow.java (Implementation)
    │   │       │           └── RoutingWorkflow.java (Interface)
    │   │       │
    │   │       ├── api/                         // --- Lớp giao tiếp (REST Endpoints & DTOs)
    │   │       │   ├── ChatController.java
    │   │       │   └── dto/
    │   │       │       ├── ChatRequest.java
    │   │       │       └── ChatResponse.java
    │   │       │
    │   │       ├── config/                      // --- Cấu hình tập trung
    │   │       │   ├── OpenAiConfig.java
    │   │       │   ├── RestTemplateConfig.java
    │   │       │   └── VectorStoreConfig.java
    │   │       │
    │   │       ├── domain/                      // --- Các đối tượng nghiệp vụ cốt lõi (Entities)
    │   │       │   └── ConversationHistory.java
    │   │       │
    │   │       └── infrastructure/              // --- Lớp kết nối với hệ thống bên ngoài
    │   │           └── laravel_api/
    │   │               ├── LaravelApiClient.java
    │   │               └── dto/
    │   │                   ├── PricingPlanDTO.java
    │   │                   ├── TemplateDTO.java
    │   │                   ├── WebsiteCreationRequestDTO.java
    │   │                   └── WebsiteCreationResponseDTO.java
    │   │
    │   └── resources
    │       ├── application.properties
    │       └── db_setup.sql
    │
    └── test
        └── ...

pom.xml
```

## Technology Stack
- **Java 21**, Spring Boot 3.x
- **PostgreSQL 15** (multi-tenant/multi-domain)
- **Spring Data JPA** + Hibernate, ModelMapper, Bean Validation, Lombok

- **Never hard-code**

## Key Business Requirements
- **User Management**: CRUD, search, pagination
- **RBAC**: Role-based access control with granular permissions, runtime evaluation, inheritance
- **No Authentication**: Service assumes requests are pre-authenticated

## Critical Development Patterns
- **Repository Pattern**: Interface in domain, implementation in infrastructure
- **Service Layer Pattern**: Domain services (pure) + Application services (Spring)
- **Exception Handling**: Use GlobalHandleException in `/shared/exception`

## Database Schema Rules
- **Primary Keys**: UUID format
- **Required Fields**: tenant_id, user_id, sessiom_id, metadata
- **Unique Constraints**: username/email unique per tenant/domain

## Development Environment
- **Container Setup**: Java 21, Maven, PostgreSQL 15.4
- **Database**: `postgres/postgres/postgres` at localhost:5432
- **pgAdmin**: localhost:5050 (`admin@admin.com/admin`)
- **Application Ports**: 8081 (main API)

## Testing Strategy
- **Unit Tests**: Domain logic, validation, mapping
- **Integration Tests**: Repository, cache, business flows with Testcontainers
- **Contract Tests**: API endpoints with MockMvc
- **Performance Tests**: Load testing key endpoints

## Build & Deployment
- **Build**: `mvn package` (Maven with Java 21)
- **Docker**: OpenJDK 21, Spring Boot version 3.x
- **Containerization**: Dockerfile for service, docker-compose for local setup

## Design Patterns to Apply
- **Repository, Service Layer, Factory, Observer, Builder, Strategy, Specification, Saga**
- **Domain Model**: Keep domain pure, free from infrastructure dependencies
- **Layered Architecture**: Strict separation of concerns between DDD layers