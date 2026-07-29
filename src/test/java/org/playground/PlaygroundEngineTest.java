package org.playground;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PlaygroundEngineTest {

    @Test
    public void returnsAnswerWithSourceMetadataAndScores() throws Exception {
        PlaygroundEngine engine = new PlaygroundEngine(
                new PlaygroundExtractiveChatService(),
                "offline://extractive",
                "offline-extractive",
                "local",
                2
        );

        JSONObject guide = engine.addDocument(
                "deployment.md",
                "Windows developers can start the Java-RAG playground with Docker Desktop. "
                        + "The browser interface is available on port 3000 after docker compose up."
        );
        engine.addDocument(
                "security.md",
                "API keys must be supplied through environment variables and never committed to Git."
        );

        JSONObject result = engine.ask("How do Windows developers start the playground?");
        JSONArray citations = result.getJSONArray("citations");

        assertEquals("local", result.getString("mode"));
        assertTrue(result.getString("answer").contains("[1]"));
        assertEquals("deployment.md", citations.getJSONObject(0).getString("documentName"));
        assertTrue(citations.getJSONObject(0).getDouble("score") >= citations.getJSONObject(1).getDouble("score"));
        assertEquals(2, engine.listDocuments().length());

        assertTrue(engine.deleteDocument(guide.getString("id")));
        assertFalse(engine.deleteDocument("missing"));
        assertEquals(1, engine.listDocuments().length());
    }

    @Test(expected = IllegalStateException.class)
    public void requiresDocumentsBeforeChat() throws Exception {
        PlaygroundEngine engine = new PlaygroundEngine(
                new PlaygroundExtractiveChatService(),
                "offline://extractive",
                "offline-extractive",
                "local",
                2
        );
        engine.ask("What is Java-RAG?");
    }
}
