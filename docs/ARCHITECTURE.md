# Java-RAG Architecture

Java-RAG keeps the retrieval-augmented generation pipeline visible and replaceable. The project intentionally avoids requiring Spring Boot or another application framework so teams can embed the core pieces into an existing Java system.

## Pipeline

```mermaid
flowchart LR
    A[Document] --> B[Parser]
    B --> C[TextSplitter]
    C --> D[EmbeddingService]
    D --> E[Retriever / Ranker]
    E --> F[Top-K Context]
    F --> G[Grounded Prompt]
    G --> H[ChatService]
    H --> I[Answer with Sources]
```

`NaiveRAG` currently implements the complete in-memory path:

```text
chunk -> embed -> rank -> Top-K context -> grounded generation
```

The parser step is optional when callers already have text and set `Document.chunkText` directly.

## Core extension points

| Extension point | Responsibility | Current implementations |
|---|---|---|
| `TextSplitter` | Convert parsed text into retrieval units | fixed-size, paragraph, sentence, recursive and semantic splitters |
| `EmbeddingService` | Embed queries and document chunks | Baichuan, Jina-related integrations, local hashing demo |
| `ChatService` | Generate from a grounded prompt | OpenAI-compatible HTTP service, local extractive demo |
| Search strategies | Recall, ranking and reranking | in-memory distance ranking plus Elasticsearch-oriented components |
| Parsers | Convert files into text | PDF, Word, PowerPoint, Excel, Markdown and HTML |

## Dependency injection

The full `NaiveRAG` constructor accepts the splitter, embedding provider, chat provider, endpoints, model and Top-K value. This makes the pipeline testable without network calls and lets applications replace individual stages.

```java
NaiveRAG rag = new NaiveRAG(
        document,
        question,
        splitter,
        embeddingService,
        chatService,
        embeddingUrl,
        chatUrl,
        model,
        4
);
```

## Maturity levels

| Area | Status | Notes |
|---|---|---|
| `NaiveRAG` grounded retrieval flow | Usable | Offline unit tests verify that only Top-K passages reach generation |
| Provider configuration | Usable | Environment variables and JVM properties; no credentials in source |
| Local zero-key demo | Usable | Deterministic learning/demo path, not a production embedding model |
| File parsers and infrastructure clients | Available | Integration testing depth varies by component |
| `AdvancedRAG` and `ModularRAG` | Experimental | Planned to converge on shared pipeline interfaces |
| Evaluation and observability | Planned | Retrieval metrics, answer grounding and run traces are roadmap items |

## Design principles

1. **Visible data flow** — retrieval context should never disappear behind framework magic.
2. **Replaceable providers** — model and storage choices should not own the application architecture.
3. **Offline testability** — pipeline behavior must be testable without paid APIs.
4. **Grounded generation** — selected passages enter the prompt and answers are asked to cite them.
5. **Enterprise-friendly integration** — Java 8 compatibility and small interfaces ease adoption in existing systems.

## Next architecture milestones

1. Extract a shared `RagPipeline` core used by Naive, Advanced and Modular modes.
2. Separate indexing and online retrieval stages.
3. Add provider-neutral retrieval results with score, source and metadata.
4. Add hybrid BM25 + vector retrieval and reranking examples.
5. Add RAG evaluation fixtures and reproducible benchmark reports.
