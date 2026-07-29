package org.chat;

import org.chunk.TextSplitter;
import org.entity.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.rag.NaiveRAG;
import org.service.LLM.ChatService;
import org.service.embedding.EmbeddingService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NaiveRAGTest {

    @Test
    public void retrievedContextIsSentToTheChatProvider() throws Exception {
        Document document = new Document();
        document.setChunkText("source text");

        TextSplitter splitter = text -> Arrays.asList(
                "RAG combines retrieval with generation.",
                "This unrelated passage should not reach the model."
        );
        RecordingEmbeddingService embeddingService = new RecordingEmbeddingService();
        RecordingChatService chatService = new RecordingChatService();

        NaiveRAG rag = new NaiveRAG(
                document,
                "What does RAG combine?",
                splitter,
                embeddingService,
                chatService,
                "https://embedding.invalid",
                "https://chat.invalid",
                "test-model",
                1
        );

        rag.chunking()
                .embedding()
                .sorting()
                .LLMChat();

        assertEquals(1, embeddingService.queryCalls);
        assertEquals(1, embeddingService.batchCalls);
        assertEquals("RAG combines retrieval with generation.", rag.getRetrievedChunks().get(0).getChunkText());
        assertEquals("grounded answer", rag.getResponse());

        JSONArray messages = chatService.lastRequest.getJSONArray("messages");
        String groundedPrompt = messages.getJSONObject(1).getString("content");
        assertTrue(groundedPrompt.contains("What does RAG combine?"));
        assertTrue(groundedPrompt.contains("RAG combines retrieval with generation."));
        assertFalse(groundedPrompt.contains("This unrelated passage should not reach the model."));
    }

    @Test(expected = IllegalStateException.class)
    public void sortingRequiresEmbeddings() {
        Document document = new Document();
        document.setChunkText("source text");

        NaiveRAG rag = new NaiveRAG(
                document,
                "question",
                text -> Arrays.asList("chunk"),
                new RecordingEmbeddingService(),
                new RecordingChatService(),
                "https://embedding.invalid",
                "https://chat.invalid",
                "test-model",
                1
        );

        rag.chunking().sorting();
    }

    private static class RecordingEmbeddingService implements EmbeddingService {
        private int queryCalls;
        private int batchCalls;

        @Override
        public double[] getEmbedding(String url, String input) {
            queryCalls++;
            return new double[]{0.0, 0.0};
        }

        @Override
        public double[][] getEmbeddings(String url, String[] inputs) {
            batchCalls++;
            return new double[][]{
                    {0.1, 0.1},
                    {10.0, 10.0}
            };
        }
    }

    private static class RecordingChatService implements ChatService {
        private JSONObject lastRequest;

        @Override
        public String generateText(String url, JSONObject params) throws IOException {
            lastRequest = params;
            return "grounded answer";
        }
    }
}
