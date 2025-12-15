Configuration Management System
Overview
This project implements a simplified but realistic Configuration Management System designed for a microservices ecosystem. The system allows teams to create, version, distribute, and consume application configurations in a consistent, scalable, and decoupled way.
Although intentionally simplified, the solution covers key backend engineering concepts including RESTful APIs, database migrations, caching, event-driven architecture with Kafka, client SDK design, and local development using Docker Compose. The project is implemented incrementally in clearly defined steps to demonstrate design decisions and trade-offs at each stage.
The system is built using Java 21, Spring Boot 3.2, Gradle (multi-module), PostgreSQL, Redis, and Kafka, following modern backend best practices.
________________________________________
High-Level Architecture
At a high level, the system consists of:
•	A central Configuration Management API responsible for storing and serving configurations
•	A PostgreSQL database as the source of truth
•	Redis as a read-through cache for hot configuration reads
•	Kafka for asynchronous publication of configuration change events
•	A Kafka consumer service that reacts to configuration changes
•	A plain Java Client SDK used by applications to consume configurations
The architecture follows clear separation of concerns: persistence, caching, asynchronous messaging, and client access are all handled independently.
________________________________________
Project Structure (Gradle Multi-Module)
The project is implemented as a Gradle multi-module build to ensure clean separation between services and libraries.
Root Project: config-system
The root project serves as an aggregator and build coordinator. It does not produce an executable artifact.
Responsibilities:
•	Defines the global group and version
•	Centralizes Java toolchain configuration (Java 21)
•	Defines shared repositories
•	Includes all submodules
This setup ensures consistency across all modules while avoiding duplication of build configuration.
________________________________________
Module: config-management-api
Type: Spring Boot application
Role: Central configuration service
This module is the core of the system. It exposes RESTful endpoints that allow configurations to be created, updated, deleted, and retrieved.
Responsibilities:
•	Expose REST APIs for configuration CRUD operations
•	Persist configuration versions in PostgreSQL
•	Manage schema migrations using Flyway
•	Cache frequently accessed configurations in Redis
•	Publish configuration change events to Kafka
•	Provide API documentation using Swagger/OpenAPI
________________________________________
Module: config-update-processor
Type: Spring Boot application
Role: Kafka consumer
This module represents a downstream service that reacts to configuration changes asynchronously.
Responsibilities:
•	Subscribe to Kafka configuration change events
•	Deserialize and process events safely
•	Log or handle configuration changes
•	Demonstrate event-driven communication
This service intentionally does not use a database or cache to keep the focus on Kafka consumption and resilience.
________________________________________
Module: config-sdk
Type: Plain Java library
Role: Client SDK
This module provides a lightweight Java client that applications can use to fetch configurations without dealing with HTTP or JSON parsing directly.
Responsibilities:
•	Encapsulate HTTP calls to the Configuration Management API
•	Deserialize responses into simple DTOs
•	Provide a clean, reusable Java API
•	Remain framework-agnostic and lightweight
________________________________________
Step-by-Step Implementation
Step 1: Project Setup and Gradle Multi-Module Configuration
The project starts with a Gradle multi-module setup using the Gradle Wrapper. The root project defines shared configuration such as group ID, version, repositories, and Java toolchain. Submodules inherit these settings, ensuring consistency and reducing duplication.
Each submodule focuses on a single responsibility, making the system easier to understand, maintain, and extend.
________________________________________
Step 2: Database Integration and Schema Management
PostgreSQL is used as the primary data store and source of truth for configurations. Database schema evolution is handled using Flyway, which automatically applies versioned SQL migration scripts on application startup.
Key design choices:
•	Each configuration change creates a new versioned record
•	Soft deletion is implemented using a deleted flag
•	Proper indexing is applied to support fast queries by application name and environment
Flyway migrations are stored in resources/db/migration and applied in order (V1__, V2__, etc.), ensuring reproducible database state across environments.
________________________________________
Step 3: REST API, Validation, and Error Handling
The Configuration Management API exposes REST endpoints for all CRUD operations, including retrieving the latest configuration for a given application and environment.
Key aspects:
•	Input validation using standard validation annotations
•	Clear separation between entities and DTOs
•	Centralized exception handling using @RestControllerAdvice
•	Consistent and meaningful error responses
Swagger/OpenAPI is used to document the API and allow interactive testing without additional tools. Can be reached here http://localhost:8080/swagger-ui/index.html#/
________________________________________
Step 4: Redis Caching Strategy
Redis is introduced as a read-through cache for the most frequently accessed endpoint: fetching the latest configuration.
Design decisions:
•	Only GET latest responses are cached
•	Redis is never used as a source of truth
•	Cache keys follow a clear naming convention (config:latest:{app}:{env})
•	Cache entries are invalidated on create, update, and delete operations
This approach ensures consistency while keeping cache logic simple and predictable.
________________________________________
Step 5: Kafka Producer Integration
Kafka is used to publish configuration change events asynchronously whenever a configuration is created, updated, or deleted.
Key points:
•	Events include event type, application name, environment, version, and timestamp
•	String keys (app:env) are used to ensure ordering per application/environment
•	JSON serialization is handled by Spring Kafka’s JsonSerializer
Kafka integration decouples configuration changes from downstream processing and demonstrates event-driven system design.
________________________________________
Step 6: Kafka Consumer and Resilience
The config-update-processor module is implemented as a Kafka consumer.
Key design choices:
•	Uses ErrorHandlingDeserializer to prevent consumer crashes
•	Disables Kafka type headers for stable JSON deserialization
•	Explicitly defines the target event type
•	Uses a dedicated consumer group
This step addresses common Kafka pitfalls such as infinite retry loops and deserialization errors, resulting in a stable and resilient consumer.
________________________________________
Step 7: Client SDK
The Client SDK is implemented as a plain Java library using the java-library Gradle plugin.
Key design choices:
•	Uses Java’s built-in HttpClient
•	Uses Jackson for JSON deserialization
•	Exposes a simple API (getLatest(appName, env))
•	Ignores unknown JSON fields to remain forward-compatible
•	Represents configuration data as JsonNode for schema flexibility
A small demo class demonstrates SDK usage and confirms end-to-end functionality.
________________________________________
Local Development Environment
All infrastructure dependencies are provided via Docker Compose, including:
•	PostgreSQL
•	Redis
•	Zookeeper
•	Kafka
This allows the entire system to be started locally with a single command and ensures a consistent development environment.
________________________________________
Key Design Principles Demonstrated
•	Clear separation of concerns
•	Database as source of truth
•	Cache used strictly for performance optimization
•	Event-driven communication using Kafka
•	Resilient consumer design
•	Lightweight and reusable client SDK
•	Forward-compatible API and SDK design
________________________________________
Integration Testing Strategy
This project includes basic but realistic integration tests that validate the interaction between the Configuration Management API and its external dependencies. The goal of these tests is to ensure that the system behaves correctly when all components are wired together, rather than testing individual units in isolation. The integration tests are implemented in the config-management-api module and are executed using Spring Boot’s testing support.
Test Environment and Infrastructure
Integration tests run against real infrastructure components provided by Docker Compose. PostgreSQL, Redis, Kafka, and Zookeeper are started manually via Docker, while the Spring Boot application itself is started automatically by the test framework.
During test execution, Spring Boot starts the full application context, including an embedded web server on a random port. REST requests are sent using TestRestTemplate, which communicates with the running application exactly as an external client would.
This approach ensures that:
•	Real database connections are used
•	Real Redis caching behavior is exercised
•	Kafka producers are initialized correctly
•	No mocks or in-memory substitutes are involved

Configuration Isolation
Test configuration is defined in a dedicated application-test.yml file located under src/test/resources. This file mirrors the connection settings defined in docker-compose.yml, ensuring that tests connect to the same Dockerized services as local development runs.
The test profile is activated using @ActiveProfiles("test"), keeping test configuration isolated from normal application runtime configuration.
Test Data Isolation
Each integration test uses a unique application name generated at runtime. This guarantees complete isolation between test cases and prevents interference caused by shared database state.
By isolating test data instead of resetting the database between tests, the test suite remains fast, deterministic, and easy to reason about.
Covered Scenarios
The integration tests verify the following core behaviors:
•	Creating a new configuration via the REST API and retrieving it as the latest version
•	Ensuring that repeated reads return consistent results (including cached reads)
•	Updating an existing configuration and validating that a new version is created
•	Verifying that the latest configuration reflects the most recent update
•	Soft-deleting a configuration and confirming that no active configuration remains
The tests assert observable behavior rather than internal implementation details, focusing on HTTP responses and returned data instead of strict status codes or internal caching mechanics.
Application Lifecycle During Tests
The Spring Boot application is not started manually for integration testing. Instead, the test framework manages the full application lifecycle:
1.	The Spring context is initialized
2.	An embedded web server is started on a random port
3.	REST controllers, services, repositories, and messaging components are registered
4.	Integration tests execute real HTTP requests
5.	The application context is shut down after test execution
External services such as PostgreSQL, Redis, and Kafka are expected to be running prior to executing the tests.
Design Rationale
This testing approach was intentionally chosen to balance realism and simplicity. By testing against real infrastructure without excessive setup or mocking, the system behavior closely resembles production conditions while remaining easy to run locally.
The result is a set of integration tests that provide confidence in system wiring, configuration correctness, and overall behavior without adding unnecessary complexity.
Resetting Local Data (Development Utilities)
During local development and testing, it is often necessary to reset application state stored in PostgreSQL, Redis, or Kafka. Since all infrastructure components are running in Docker containers, data can be safely reset without affecting source code or application configuration.
The following commands are intended for local development only.
________________________________________
PostgreSQL
PostgreSQL is the primary data store for configuration data. It runs inside a Docker container and persists data using a Docker volume.
Connect to PostgreSQL
docker exec -it config-postgres psql -U config_user -d configdb
Remove all configuration data (keep schema)
TRUNCATE TABLE configurations RESTART IDENTITY CASCADE;
Reset Flyway migration history
TRUNCATE TABLE flyway_schema_history;
Drop and recreate the database (full reset)
If the database is in use, active connections must be terminated first:
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'configdb';
Then:
DROP DATABASE configdb;
CREATE DATABASE configdb;
Flyway migrations will be reapplied automatically on application startup.
Full PostgreSQL reset (recommended)
docker compose down -v
docker compose up -d postgres
This removes the PostgreSQL volume and recreates the database from scratch.
________________________________________
Redis
Redis is used as a cache for frequently accessed configuration reads.
Connect to Redis
docker exec -it config-redis redis-cli
Clear all cached data
FLUSHALL
Clear only the current database
FLUSHDB
Restart Redis container
docker compose restart redis
________________________________________
Kafka
Kafka stores configuration change events in topics.
Consume events from the beginning
docker exec -it config-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic configuration-changes \
  --from-beginning
Delete a Kafka topic
docker exec -it config-kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --delete \
  --topic configuration-changes
Recreate a Kafka topic
docker exec -it config-kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --create \
  --topic configuration-changes \
  --partitions 1 \
  --replication-factor 1
Restart Kafka and Zookeeper
docker compose restart kafka zookeeper
________________________________________
Full Environment Reset (Clean Slate)
To completely reset all local data across PostgreSQL, Redis, and Kafka:
docker compose down -v
docker compose up -d
This will:
•	Stop all containers
•	Remove all persisted volumes
•	Recreate infrastructure with empty state
________________________________________
Notes
•	Docker volumes are local to the machine and are not shared via GitHub
•	Cloning the repository on another machine always starts with a clean environment
•	These commands are safe for local development but should never be used in production
Conclusion
This project demonstrates a complete, end-to-end configuration management system implemented with modern backend technologies and best practices. While intentionally simplified, it covers real-world concerns such as data consistency, performance optimization, asynchronous communication, and client usability.
The step-by-step structure makes the project easy to understand, extend, and reuse as a foundation for future distributed systems.

