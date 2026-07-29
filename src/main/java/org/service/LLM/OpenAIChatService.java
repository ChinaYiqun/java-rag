package org.service.LLM;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.constant.Config;
import org.json.JSONObject;
import org.service.db.RedisClient;

import java.io.IOException;
import java.util.List;

public class OpenAIChatService implements ChatService {

    private final String apiKey;
    private final OkHttpClient client;

    public OpenAIChatService(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
    }

    /**
     * Send a request to an OpenAI-compatible chat completion endpoint.
     *
     * @param url API URL
     * @param params request body
     * @return generated text
     * @throws IOException when the request or response is invalid
     */
    @Override
    public String generateText(String url, JSONObject params) throws IOException {
        RequestBody body = RequestBody.create(
                params.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected HTTP status: " + response.code());
            }
            if (response.body() == null) {
                throw new IOException("Chat provider returned an empty response body");
            }

            String responseBody = response.body().string();
            JSONObject jsonObject = new JSONObject(responseBody);
            return jsonObject
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        }
    }

    /**
     * Generate a response using conversation history stored in Redis.
     */
    public String generateText(String url, String chatId, JSONObject newMessage) throws IOException {
        RedisClient redisClient = RedisClient.getInstance();
        List<String> historyMessages = redisClient.lrange(chatId, 0, -1);
        JSONObject[] messageArray = new JSONObject[historyMessages.size() + 1];
        for (int i = 0; i < historyMessages.size(); i++) {
            messageArray[i] = new JSONObject(historyMessages.get(i));
        }
        messageArray[messageArray.length - 1] = newMessage;

        JSONObject params = new JSONObject()
                .put("model", Config.LLM_MODEL)
                .put("messages", messageArray)
                .put("temperature", 0.3)
                .put("stream", false);

        String generatedText = generateText(url, params);

        redisClient.lpush(chatId, newMessage.toString(), Config.REDIS_EXPIRE_SECONDS);
        redisClient.lpush(
                chatId,
                new JSONObject()
                        .put("role", "assistant")
                        .put("content", generatedText)
                        .toString(),
                Config.REDIS_EXPIRE_SECONDS
        );

        return generatedText;
    }

    public static void main(String[] args) {
        String apiKey = Config.require(Config.API_KEY, "RAG_API_KEY");
        OpenAIChatService openAIChatService = new OpenAIChatService(apiKey);

        try {
            JSONObject params = new JSONObject()
                    .put("model", Config.LLM_MODEL)
                    .put("messages", new JSONObject[]{
                            new JSONObject().put("role", "user").put("content", "1+1 = ?")
                    })
                    .put("temperature", 0.3)
                    .put("stream", false);

            String generatedText = openAIChatService.generateText(Config.LLM_URL, params);
            System.out.println(generatedText);
        } catch (IOException e) {
            throw new IllegalStateException("Chat request failed", e);
        }
    }
}
