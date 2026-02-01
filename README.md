# 🚀 LoomLanes-Engine: Distributed Priority Task Scheduler

**LoomLanes-Engine** is a high-throughput, low-latency distributed task scheduling system designed to handle millions of jobs with absolute reliability. Built on **Java 21 (Project Loom)** and **Spring Boot 3.x**, it utilizes **Virtual Threads** to achieve massive concurrency while maintaining a clean, synchronous programming model.

[![Java Version](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/technologies/javase/21-relnote-issues.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Architecture](https://img.shields.io/badge/Architecture-Distributed%20Systems-blue.svg)]()

---

## 🏗 High-Level Architecture (HLD)

The system is designed for massive scale, decoupling task ingestion from execution through a multi-stage priority pipeline.

```mermaid
graph TD
    subgraph "Ingestion Layer (REST)"
        A[Client] -->|POST /task| B[Task Controller]
        B --> C{Rate Limiter}
        C -->|Lua script| D[(Redis: Token Bucket)]
    end

    subgraph "Priority Orchestration"
        D -->|Allowed| E[Priority Engine]
        E -->|ZADD| F[(Redis: Priority ZSet)]
        F -->|ZPOPMIN| G[Task Poller]
    end

    subgraph "Message Backbone"
        G -->|Publish| H[Kafka: Task_Execution]
        H -->|Fail/Retry| I[Kafka: Task_DLQ]
    end

    subgraph "Execution Layer"
        J[Worker Pool]
        subgraph "Internal Worker Strategy"
            J -->|Spawn| K(((Virtual Thread)))
            K -->|Idempotency| L[Redis: SETNX]
            L -->|Execute| M[Business Logic]
        end
    end

    subgraph "Observability"
        N[Actuator] --> O[Prometheus]
        O --> P[Grafana]
    end
```

## 🛠 Tech Stack

-   **Runtime**: Java 21 (Project Loom / Virtual Threads).
-   **Framework**: Spring Boot 3.4.x.
-   **Storage/State**: Redis (Rate Limiting, Priority Queue, Idempotency Locks).
-   **Messaging**: Apache Kafka (Distributed commit log for job reliability).
-   **Infrastructure**: Docker Desktop (Host) & WSL-Fedora (Build/Runtime).
-   **Observability**: Micrometer + Prometheus + Grafana.

## ⚡ Key Performance Features

-   **Zero-Blocking Execution**: Leverages Java 21 Virtual Threads to handle high-I/O tasks (Redis/Kafka calls) without thread exhaustion or OS context-switching overhead.
-   **Atomic Rate Limiting**: Distributed Token Bucket implemented via Redis Lua scripts to ensure sub-millisecond overhead.
-   **Distributed Idempotency**: Guaranteed "exactly-once" processing using Redis `SETNX` distributed locking.
-   **Smart Priority Lanes**: Redis Sorted Sets (ZSet) allow O(log N) insertion and O(1) extraction of the highest priority tasks.
-   **Resilience Backbone**: Integrated Dead Letter Queue (DLQ) strategy for automatic retries and failure isolation.

## 💻 Development Environment Setup

This project utilizes a **Hybrid Architecture** for maximum performance:
*   **Build & Application Runtime**: Fedora (WSL) — Optimized for Linux native filesystem I/O.
*   **Infrastructure/Containers**: Docker Desktop (Windows Host) — Managing Redis & Kafka.
*   **IDE**: IntelliJ IDEA (Windows) — Connected via WSL-JDK bridge.

### Prerequisites
- Docker Desktop (with WSL Integration enabled for Fedora)
- Java 21 SDK (Installed in Fedora)
- Maven 3.9+ (Installed in Fedora)

### 1. Start Infrastructure
In your Fedora terminal:
```bash
docker compose up -d
```

### 2. Configure Environment
Ensure your `application.properties` points to `localhost:6379` (Redis) and `localhost:9092` (Kafka). Docker Desktop handles the bridge between WSL and Windows automatically.

### 3. Build and Run
```bash
./mvnw clean install
./mvnw spring-boot:run
```

## 📈 Monitoring
Metrics are exposed via Spring Actuator in Prometheus format:
- **Health Check**: `http://localhost:8080/actuator/health`
- **Prometheus Metrics**: `http://localhost:8080/actuator/prometheus`

---
**Author**: Saksham Gupta with ❤️  
**Status**: Active Development (HFT-Architecture Protocol)