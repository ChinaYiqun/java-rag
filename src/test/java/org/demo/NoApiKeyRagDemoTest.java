package org.demo;

import org.entity.Document;
import org.junit.Test;
import org.rag.NaiveRAG;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NoApiKeyRagDemoTest {

    @Test
    public void retrievesLocalDeploymentPassageAndProducesCitedAnswer() throws Exception {
        NaiveRAG rag = NoApiKeyRagDemo.createPipeline()
                .chunking()
                .embedding()
                .sorting()
                .LLMChat();

        List<Document> retrieved = rag.getRetrievedChunks();
        assertEquals(1, retrieved.size());
        assertTrue(retrieved.get(0).getChunkText().contains("Ollama"));
        assertTrue(retrieved.get(0).getChunkText().contains("local machine"));
        assertTrue(rag.getResponse().contains("Ollama"));
        assertTrue(rag.getResponse().contains("[1]"));
    }

    @Test
    public void printedDemoExplainsTheRetrievedResult() throws Exception {
        String output = NoApiKeyRagDemo.runDemo();

        assertTrue(output.contains("no API key demo"));
        assertTrue(output.contains("Top retrieved passage"));
        assertTrue(output.contains("Offline extractive answer"));
    }
}
