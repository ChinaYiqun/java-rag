package org.service.LLM;

import org.json.JSONObject;

import java.io.IOException;

/**
 * Minimal abstraction for an OpenAI-compatible chat completion provider.
 *
 * <p>The interface keeps the core RAG pipeline testable without making network
 * requests and allows alternative providers to be injected.</p>
 */
public interface ChatService {
    String generateText(String url, JSONObject params) throws IOException;
}
