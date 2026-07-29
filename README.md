<div align="center">
  <img src="webapp/resources/biglog.png" alt="Java-RAG logo" width="560" />

# Java-RAG

**A framework-free, hackable RAG toolkit for Java 8+**

See every stage of retrieval-augmented generation instead of hiding it behind framework magic.

[![Java-RAG CI](https://github.com/ChinaYiqun/java-rag/actions/workflows/ci.yml/badge.svg)](https://github.com/ChinaYiqun/java-rag/actions/workflows/ci.yml)
[![Java 8+](https://img.shields.io/badge/Java-8%2B-ED8B00?logo=openjdk&logoColor=white)](https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html)
[![License](https://img.shields.io/github/license/ChinaYiqun/java-rag)](LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/ChinaYiqun/java-rag?style=social)](https://github.com/ChinaYiqun/java-rag/stargazers)

[English](README.md) · [简体中文](README_ch.md) · [Architecture](docs/ARCHITECTURE.md) · [Contributing](CONTRIBUTING.md)
</div>

---

Java-RAG is a pure-Java implementation of the complete RAG data flow:

```text
Document -> Parse -> Chunk -> Embed -> Retrieve -> Top-K Context -> Grounded Prompt -> Answer
```

It is designed for developers who want to **learn RAG internals**, **embed RAG into an existing Java system**, or **replace individual providers without adopting Spring Boot or another mandatory application framework**.

## Why Java-RAG?

- **Runnable with no API key** — the included offline demo exercises retrieval, context construction and cited output with one command.
- **Transparent pipeline** — query embeddings, chunk embeddings, ranking and Top-K context remain visible and testable.
- **Provider-neutral interfaces** — inject your own splitter, embedding service and OpenAI-compatible chat provider.
- **Java 8 compatible** — useful for existing enterprise and legacy JVM environments.
- **Real infrastructure building blocks** — file parsers, Redis, Elasticsearch, MinIO, MySQL, reranking and load-balancing code are included.
- **Security guardrails** — CI rejects credential-like values and runtime secrets are externalized.

## Run a complete RAG flow in 60 seconds

No model download. No database. No API key.

```bash
git clone https://github.com/ChinaYiqun/java-rag.git
cd java-rag
bash scripts/run-no-api-demo.sh
```

Windows users without Bash can run the underlying Maven command:

```powershell
mvn -q -DskipTests compile org.codehaus.mojo:exec-maven-plugin:3.5.0:java -Dexec.mainClass=org.demo.NoApiKeyRagDemo
```

Expected output:

```text
Java-RAG: no API key demo
Question: Which option keeps prompts and model data on the local machine?

Top retrieved passage:
Ollama runs language models on the local machine...

Answer:
Offline extractive answer: Ollama runs language models on the local machine... [1]
```

The offline demo uses deterministic feature hashing and an extractive generator. It is intentionally simple: its purpose is to prove the full data flow locally, not to replace a production embedding model or LLM.

## How it works

```mermaid
flowchart LR
    A[Document] --> B[Parser]
    B --> C[TextSplitter]
    C --> D[EmbeddingService]
    D --> E[Similarity Ranking]
    E --> F[Top-K Passages]
    F --> G[Grounded Prompt]
    G --> H[ChatService]
    H --> I[Answer with Citations]
```

`NaiveRAG` now performs the behavior its name promises:

1. split source text into chunks;
2. embed the query once;
3. batch-embed all chunks;
4. rank chunks by distance;
5. inject only the selected Top-K passages into the prompt;
6. ask the model to answer from context and cite passages as `[1]`, `[2]`, and so on.

See [the architecture guide](docs/ARCHITECTURE.md) for extension points and maturity levels.

## Minimal Java example

```java
Document document = new Document("./handbook.pdf");

NaiveRAG rag = new NaiveRAG(document, "What is the refund policy?")
        .parsing()
        .chunking()
        .embedding()
        .sorting()
        .LLMChat();

System.out.println(rag.getResponse());
System.out.println(rag.getRetrievedChunks());
```

Configure real providers through environment variables or JVM system properties:

```bash
export RAG_API_KEY='replace-me'
export RAG_LLM_URL='https://your-provider.example/v1/chat/completions'
export RAG_EMBEDDING_API_URL='https://your-provider.example/v1/embeddings'
```

The full constructor accepts custom implementations:

```java
NaiveRAG rag = new NaiveRAG(
        document,
        question,
        textSplitter,
        embeddingService,
        chatService,
        embeddingUrl,
        chatUrl,
        model,
        4
);
```

## What is included

| Area | Capabilities |
|---|---|
| RAG pipelines | Naive RAG, experimental Advanced RAG and Modular RAG |
| Parsing | PDF, Word, PowerPoint, Excel, Markdown and HTML |
| Chunking | Fixed-size, paragraph, sentence, recursive and semantic splitting |
| Embeddings | OpenAI-compatible/Baichuan-style services, Jina-related integrations, offline hashing demo |
| Retrieval | In-memory distance ranking, recall strategies, reranking components |
| Storage | Elasticsearch, Redis, MySQL and MinIO integrations |
| LLM access | OpenAI-compatible chat interface and Ollama-oriented usage |
| Agents | Multi-agent examples and tool-oriented building blocks |
| Operations | Nacos configuration, round-robin and weighted-random load balancing |

## Project status

| Component | Status | Meaning |
|---|---|---|
| `NaiveRAG` grounded pipeline | **Usable** | Deterministic offline tests verify Top-K context reaches generation |
| External configuration and CI | **Usable** | Java 8 build, secret guard and no-key demo run on every PR |
| File parsers and infrastructure clients | **Available** | APIs exist; integration-test depth varies by component |
| `AdvancedRAG` / `ModularRAG` | **Experimental** | Planned to converge on a shared pipeline core |
| Evaluation and observability | **Planned** | Retrieval metrics, answer grounding and trace artifacts are next milestones |

## Java-RAG vs larger Java AI frameworks

Java-RAG is not trying to replace the ecosystems around LangChain4j or Spring AI.

Use a larger framework when you want a broad provider catalog, framework integrations and a mature production abstraction. Use Java-RAG when you want:

- a small codebase that shows how RAG actually works;
- framework-independent components for an existing Java application;
- a teaching, experimentation or interview project;
- direct control over chunking, retrieval, prompt construction and provider calls;
- Java 8 compatibility.

## Documentation

- [Architecture and extension points](docs/ARCHITECTURE.md)
- [Installation](doc/install.md)
- [LLM conversations](doc/LLM.md)
- [Document parsing](doc/parser.md)
- [Chunking](doc/chunk.md)
- [Embeddings](doc/embedding.md)
- [Search and reranking](doc/search.md)
- [Advanced and modular pipelines](doc/pipeline.md)
- [Database integrations](doc/db.md)
- [Load balancing](doc/balance.md)
- [Security policy](SECURITY.md)

## Development

```bash
bash scripts/check-no-hardcoded-secrets.sh
mvn --batch-mode --no-transfer-progress test
bash scripts/run-no-api-demo.sh
```

Contributions are welcome. Good first contributions include parser tests, splitter fixtures, offline provider examples, retrieval metadata, dependency cleanup and documentation improvements. See [CONTRIBUTING.md](CONTRIBUTING.md).

## Roadmap

- [ ] Shared `RagPipeline` core for Naive, Advanced and Modular modes
- [ ] Separate indexing and online retrieval APIs
- [ ] Provider-neutral retrieval results with source metadata and scores
- [ ] Hybrid BM25 + vector retrieval example
- [ ] Reranking and query-rewrite examples
- [ ] Reproducible RAG evaluation suite
- [ ] Docker Compose production demo and lightweight web playground

## Security

Never commit API keys, passwords, tokens, `.env` files or private endpoints. Runtime credentials are loaded from environment variables or JVM properties. Read [SECURITY.md](SECURITY.md) before deploying the infrastructure integrations.

## License

[Apache License 2.0](LICENSE)

---

If Java-RAG helps you understand or build RAG on the JVM, **star the repository** and share the no-key demo with another Java developer. A star helps more developers discover a framework-free RAG learning path.
