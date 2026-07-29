# Java-RAG Visual Playground

The playground turns the framework-free Java RAG pipeline into a browser product:

```text
Upload document -> parse -> chunk -> embed -> retrieve -> answer -> inspect citations
```

## Start without an API key

Requirements:

- Docker Desktop or Docker Engine with Compose
- about 1 GB of free space for the Java and frontend images

```bash
docker compose up --build
```

Open `http://localhost:3000`.

The default `local` mode uses deterministic hashing embeddings and an extractive answer generator. It is suitable for validating the complete RAG data flow, UI, citations and integration points without downloading a model or sending documents to a cloud provider.

## Use Ollama for generation

The retrieval layer remains local and deterministic. Ollama replaces only the answer-generation stage.

```bash
RAG_PLAYGROUND_MODE=ollama \
RAG_OLLAMA_MODEL=qwen2.5:3b \
docker compose --profile ollama up --build
```

The `ollama-pull` service downloads the configured model into the `ollama-data` volume. The first startup can take longer while the model is downloaded.

Useful variables:

| Variable | Default | Meaning |
|---|---|---|
| `RAG_PLAYGROUND_MODE` | `local` | `local` or `ollama` |
| `RAG_PLAYGROUND_TOP_K` | `4` | Maximum evidence passages returned per answer |
| `RAG_OLLAMA_MODEL` | `qwen2.5:3b` | Ollama chat model |
| `RAG_OLLAMA_CHAT_URL` | `http://ollama:11434/v1/chat/completions` | OpenAI-compatible Ollama endpoint |

## Supported uploads

The current parser factory supports:

- PDF
- Word (`.doc`, `.docx`)
- PowerPoint (`.ppt`, `.pptx`)
- Excel (`.xls`, `.xlsx`)
- Markdown, text, HTML, Java and Python source files

The playground stores parsed text, chunks and vectors in memory. Restarting the backend clears the knowledge base.

## Develop without Docker

Start the Java API:

```bash
bash scripts/run-playground-api.sh
```

In a second terminal, start the React application. Vite 8 requires Node.js 20.19 or newer.

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`. Vite proxies `/api` to `http://localhost:8080`.

## API contract

### Health

```http
GET /api/health
```

```json
{
  "status": "ok",
  "mode": "local",
  "model": "offline-extractive"
}
```

### Upload a file

```http
POST /api/documents
Content-Type: multipart/form-data
```

Fields:

- `file`: uploaded file
- `originalFileName`: original file name including extension

### Add text directly

```http
POST /api/documents/text
Content-Type: application/json
```

```json
{
  "name": "handbook.md",
  "content": "Document text..."
}
```

### List and delete documents

```http
GET /api/documents
DELETE /api/documents/{documentId}
```

### Ask the knowledge base

```http
POST /api/chat
Content-Type: application/json
```

```json
{
  "question": "How do I start the playground?"
}
```

The response keeps the answer and evidence together:

```json
{
  "answer": "Run docker compose up --build [1]",
  "mode": "local",
  "model": "offline-extractive",
  "citations": [
    {
      "index": 1,
      "documentId": "...",
      "documentName": "guide.md",
      "chunkId": 0,
      "score": 0.82,
      "content": "Run docker compose up --build..."
    }
  ]
}
```

## Current boundaries

- The default embedding provider is for deterministic demos, not semantic production quality.
- The knowledge base is in memory and single-process.
- Authentication, quotas, persistent vector storage and asynchronous ingestion are not implemented yet.
- Uploaded content is treated as untrusted data when constructing the generation prompt.

These boundaries are deliberate. The playground is a working product surface over the existing transparent RAG pipeline, while production storage and model providers remain replaceable extension points.
