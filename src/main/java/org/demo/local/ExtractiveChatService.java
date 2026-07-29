package org.demo.local;

import org.json.JSONArray;
import org.json.JSONObject;
import org.service.LLM.ChatService;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Offline generator used by the no-API-key demo.
 *
 * <p>This class deliberately does not pretend to be an LLM. It extracts the
 * highest-ranked passage from the grounded prompt so users can verify the
 * complete retrieval -> context -> answer data flow locally.</p>
 */
public final class ExtractiveChatService implements ChatService {

    private static final Pattern FIRST_PASSAGE = Pattern.compile(
            "\\[1\\]\\s*\\n([\\s\\S]*?)(?:\\n\\n\\[2\\]|\\n\\nAnswer:)",
            Pattern.MULTILINE
    );

    @Override
    public String generateText(String url, JSONObject params) throws IOException {
        if (params == null) {
            throw new IOException("chat parameters cannot be null");
        }

        JSONArray messages = params.optJSONArray("messages");
        if (messages == null || messages.length() == 0) {
            throw new IOException("chat request contains no messages");
        }

        JSONObject userMessage = messages.getJSONObject(messages.length() - 1);
        String prompt = userMessage.optString("content", "");
        Matcher matcher = FIRST_PASSAGE.matcher(prompt);
        if (!matcher.find()) {
            throw new IOException("grounded prompt does not contain passage [1]");
        }

        String passage = matcher.group(1).trim().replaceAll("\\s+", " ");
        return "Offline extractive answer: " + passage + " [1]";
    }
}
