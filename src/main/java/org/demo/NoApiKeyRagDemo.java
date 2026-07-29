package org.demo;

import org.chunk.TextSplitter;
import org.demo.local.ExtractiveChatService;
import org.demo.local.HashingEmbeddingService;
import org.entity.Document;
import org.rag.NaiveRAG;

import java.io.IOException;
import java.util.Arrays;

/**
 * A complete RAG run with no API key, model download, framework, or database.
 */
public final class NoApiKeyRagDemo {

    private static final String KNOWLEDGE_BASE =
            "OpenAI-compatible providers expose remote HTTP APIs for chat and embeddings. "
                    + "They are convenient when a team wants managed inference and elastic capacity.\n\n"
                    + "Ollama runs language models on the local machine. Prompts, retrieved context, "
                    + "and model data can stay on that machine, which is useful for private offline deployments.\n\n"
                    + "Elasticsearch can persist document chunks and vectors for larger knowledge bases. "
                    + "It is optional for the in-memory Java-RAG learning path.";

    private static final String QUESTION =
            "Which option keeps prompts and model data on the local machine?";

    private NoApiKeyRagDemo() {
    }

    public static NaiveRAG createPipeline() {
        Document document = new Document();
        document.setChunkText(KNOWLEDGE_BASE);

        TextSplitter paragraphSplitter = text -> Arrays.asList(text.split("\\n\\s*\\n"));

        return new NaiveRAG(
                document,
                QUESTION,
                paragraphSplitter,
                new HashingEmbeddingService(),
                new ExtractiveChatService(),
                "local://hashing-embedding",
                "local://extractive-generator",
                "local-extractive-demo",
                1
        );
    }

    public static String runDemo() throws IOException {
        NaiveRAG rag = createPipeline()
                .chunking()
                .embedding()
                .sorting()
                .LLMChat();

        StringBuilder output = new StringBuilder();
        output.append("Java-RAG: no API key demo\n");
        output.append("Question: ").append(QUESTION).append("\n\n");
        output.append("Top retrieved passage:\n");
        output.append(rag.getRetrievedChunks().get(0).getChunkText()).append("\n\n");
        output.append("Answer:\n").append(rag.getResponse()).append('\n');
        return output.toString();
    }

    public static void main(String[] args) throws IOException {
        System.out.print(runDemo());
    }
}
