# Technical Notes: Architecture, Decisions, and Tradeoffs

## Overview
This service ingests event plans from external providers (XML over HTTP), stores them locally, and exposes a search endpoint by date range. The design prioritizes availability and predictable latency, even when provider services are slow or offline.

## How it works

- A scheduled job pulls XML from the provider and upserts events into a local H2 database.
- Events are **never deleted**. If a plan ever appears with `sell_mode=online`, it is marked as `ever_online` and will be returned by `/search` even if it disappears from later provider responses.
- `/search` queries only the local database, so it remains fast and available even when the provider is down.

## Running the App

Running on Docker
```bash
make run
```

Running using built jar
```bash
make run-jar
```

## Endpoint

```
GET /search?starts_at=2021-06-01T00:00:00Z&ends_at=2021-06-30T23:59:59Z
```
## Swagger UI

Once the app is running, open:

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/v3/api-docs`



## Technology Stack Decision

- **Java 17** was chosen for LTS stability, modern language features, and broad production support.
- **Spring Boot** provides strong ecosystem support for web, scheduling, configuration, caching, and testing, enabling rapid, maintainable delivery with minimal boilerplate.

## Architecture

**Style:** Hexagonal (Ports & Adapters)

- **Domain**
  - `PlanEvent`, `Zone` are the core domain models.
  - Domain ports:
    - `ProviderFeedApi` for provider ingestion.
    - `EventPersistenceApi` for storage.
- **Adapters (out)**
  - Provider adapter:
    - Fetches provider XML (via `ProviderClient`).
    - Parses XML (via `ProviderXmlParser` / `XmlMapper`).
    - Maps to `PlanEvent`.
  - Persistence adapter:
    - Maps domain to JPA entities.
    - Saves/queries via Spring Data JPA.
- **Adapters (in)**
  - Web controller maps domain to API DTOs for `/search`.
- **Infrastructure**
  - Spring Boot config for WebClient, Redis cache, and scheduling.

This split keeps the core model insulated from HTTP, XML, and persistence details, making it easier to add new providers or alternative storage systems.

## Technical Decisions & Rationale

1. **Local persistence as source of truth for `/search`**
   - Search must remain fast and reliable regardless of provider availability.
   - Provider sync is asynchronous and failure tolerant.

2. **Scheduled sync**
   - Sync runs on a fixed interval (configurable).
   - Failures are isolated and logged; search remains operational.

3. **Provider identity and uniqueness**
   - `PlanEvent` includes `providerId` + `externalId`.
   - Unique event `id` is derived as `providerId:externalId` to avoid collisions.

4. **Caching with Redis + in-memory fallback**
   - Redis provides shared cache across instances.
   - When Redis is unavailable, a Caffeine fallback cache is used.
   - TTL ensures results expire predictably.

5. **XML parsing with Jackson XmlMapper**
   - Provider payload uses attributes rather than nested text nodes.
   - Jackson maps directly to intermediate XML DTOs for clarity and resilience.

6. **DTO mapping in the web adapter**
   - Domain models remain transport-agnostic.
   - Mapping logic lives in a dedicated `EventSummaryMapper`.

## Tradeoffs

- **H2 for simplicity vs. production-grade DB**
  - H2 is lightweight for a challenge but not recommended for production.
  - Postgres/MySQL would provide better concurrency and indexing.

- **Single scheduled sync vs. streaming ingestion**
  - The current approach is simple and reliable.
  - For very large payloads, a streaming pipeline or message queue would be superior.

- **Cache TTL static**
  - A single TTL is easy to reason about.
  - More advanced scenarios may require cache invalidation by provider or event id.

## Multi-provider Support

Providers are configured under `providers.list[]`, each with a unique `id`. The sync service loops through all configured providers and ingests them independently.

**To add a new provider:**
1. Add a new entry in `application.yml` under `providers.list`.
2. Implement a provider adapter if the format is different (e.g., JSON).
3. Map the provider payload into `PlanEvent`.

## Going the Extra Mile: Scaling Strategy

### 1. Scalability (thousands of plans, hundreds of zones)

**Potential challenges:**
- Large XML payloads
- High memory usage
- Slow batch inserts

**Planned improvements:**
- Stream XML parsing instead of loading full documents.
- Batch writes using JDBC batch or `saveAll` with batch config.
- Normalize zones into their own table with indexed FK.
- Skip writes if payload hash unchanged.

### 2. High Traffic (5k–10k RPS)

**Potential challenges:**
- DB read pressure
- Cache stampede
- Slow aggregation of prices

**Planned improvements:**
- Precompute `min_price` and `max_price` at ingestion and store on events.
- Add Redis read-through cache with per-range keys.
- Use multi-node deployment behind a load balancer.
- Add rate limiting at the edge (API Gateway).

### 3. Optimization Strategies

- **Database indexes** on `start_date`, `end_date`, `provider_id`, and `ever_online`.
- **Materialized projection table** with only search fields.
- **Async ingestion** with message queues for backpressure.
- **Observability** with metrics for latency, provider errors, cache hit ratio.

## Summary
The current implementation focuses on correctness, maintainability, and resilience. The architectural choices make it straightforward to scale horizontally, add new providers, and optimize for high traffic loads when needed.
