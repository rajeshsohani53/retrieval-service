# Retrieval Service

A standalone Spring Boot **microservice** with one job: storing document chunks as vectors in **Qdrant** and finding the chunks most similar to a question.

It is part of a **Retrieval-Augmented Generation (RAG)** system built from scratch in Java with Spring Boot and LangChain4j, split into five independent microservices.

---

## Role in the System

This is **service #4** in a five-service RAG pipeline.

| Port | Service | Responsibility |
|------|---------|----------------|
| 8080 | API Gateway / Orchestrator | Single entry point; coordinates the flow |
| 8081 | Document Service | Raw document → clean chunks |
| 8082 | Embedding Service | Text → vector |
| **8083** | **Retrieval Service** | **Stores vectors; query vector → most relevant chunks** ← *this service* |
| 8084 | LLM / Answer Service | Chunks + question → final answer |

- **Ingest:** the gateway sends each chunk's text with its vector, and this service stores them in Qdrant.
- **Ask:** the gateway sends the question's vector, and this service returns the top-K most similar chunks.

This service does **not** create embeddings. It receives vectors that were already computed by the Embedding Service, which keeps each service focused on a single job.

---

## Tech Stack

- **Java 17**
- **Spring Boot 3.3.5** (Spring Web)
- **LangChain4j 1.19.0** (`EmbeddingStore` abstraction + `langchain4j-qdrant`)
- **Qdrant** vector database (connected over gRPC)
- **Maven**

The service code depends only on LangChain4j's `EmbeddingStore` interface. Qdrant is wired in through a single config bean (`QdrantConfig`), so the vector store can be swapped without changing the service logic.

---

## API

### `POST /api/retrieval/store`

Stores many chunks at once.

```bash
curl -X POST http://localhost:8083/api/retrieval/store \
     -H "Content-Type: application/json" \
     -d '{"chunks": [{"text": "first chunk", "vector": [0.01, -0.02, ...]}]}'
```

```json
{ "stored": 1 }
```

### `POST /api/retrieval/search`

Returns the text of the `topK` most similar chunks.

```bash
curl -X POST http://localhost:8083/api/retrieval/search \
     -H "Content-Type: application/json" \
     -d '{"queryVector": [0.01, -0.02, ...], "topK": 3}'
```

```json
{ "results": ["most relevant chunk", "second", "third"] }
```

---

## Running Locally

**Prerequisites:** JDK 17+, Docker (for Qdrant).

**1. Start Qdrant**

```bash
docker run -p 6333:6333 -p 6334:6334 qdrant/qdrant
```

**2. Create the `documents` collection.** The vector size must match the embedding model (3072 for `gemini-embedding-001`).

```bash
curl -X PUT http://localhost:6333/collections/documents \
     -H "Content-Type: application/json" \
     -d '{"vectors": {"size": 3072, "distance": "Cosine"}}'
```

**3. Run the service**

```bash
./mvnw spring-boot:run
```

The service starts on **http://localhost:8083**.

### Configuration

| Setting | Value | Where |
|---------|-------|-------|
| Server port | `8083` | `application.properties` |
| Qdrant host / gRPC port | `localhost:6334` | `QdrantConfig.java` |
| Collection name | `documents` | `QdrantConfig.java` |

---

## Project Structure

```
retrieval-service/
├── src/main/java/com/raj/retrievalservice/
│   ├── RetrievalServiceApplication.java                       # entry point
│   ├── config/QdrantConfig.java                               # Qdrant EmbeddingStore bean
│   ├── controller/RetrievalController/RetrievalController.java    # REST endpoints
│   └── service/RetrievalService.java                          # store + similarity search
├── src/main/resources/application.properties
└── pom.xml
```
