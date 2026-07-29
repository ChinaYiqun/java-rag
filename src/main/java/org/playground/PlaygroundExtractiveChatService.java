package org.playground;

import org.json.JSONArray;
import org.json.JSONObject;
import org.service.LLM.ChatService;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Offline answer generator for the visual playground.
 *
 * <p>It extracts the first ranked passage from the source-labelled prompt. It
 * is intentionally not presented as an LLM; it exists so the complete browser
 * workflow remains runnable without credentials or a model download.</p>
 */
public final class PlaygroundExtractiveChatService implements ChatService {

    private static final Pattern FIRST_PASSAGE = Pattern.compile(
            "\\[1\\][^\\n]*\\n([\\s\\S]*?)(?:\\n\\n\\[2\\]|\\n\\nAnswer:)",
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

        String prompt = messages.getJSONObject(messages.length() - 1).optString("content", "");
        Matcher matcher = FIRST_PASSAGE.matcher(prompt);
        if (!matcher.find()) {
            throw new IOException("grounded prompt does not contain source-labelled passage [1]");
        }

        String passage = matcher.group(1).trim().replaceAll("\\s+", " ");
        return "Offline extractive answer: " + passage + " [1]";
    }
}
