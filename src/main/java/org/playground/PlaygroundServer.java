package org.playground;

import fi.iki.elonen.NanoHTTPD;
import org.json.JSONObject;
import org.parser.FileParserFactory;
import org.service.LLM.ChatService;
import org.service.LLM.OpenAIChatService;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Lightweight HTTP API for the Java-RAG playground.
 */
public final class PlaygroundServer extends NanoHTTPD {

    private final PlaygroundEngine engine;

    public PlaygroundServer(int port, PlaygroundEngine engine) throws IOException {
        super(port);
        this.engine = engine;
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("Java-RAG Playground API listening on http://0.0.0.0:" + port);
        System.out.println("Mode: " + engine.getMode() + " | Model: " + engine.getModel());
    }

    @Override
    public Response serve(IHTTPSession session) {
        if (Method.OPTIONS.equals(session.getMethod())) {
            return withCors(newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, ""));
        }

        try {
            String uri = session.getUri();
            Method method = session.getMethod();

            if (Method.GET.equals(method) && "/api/health".equals(uri)) {
                return json(Response.Status.OK, new JSONObject()
                        .put("status", "ok")
                        .put("mode", engine.getMode())
                        .put("model", engine.getModel()));
            }

            if (Method.GET.equals(method) && "/api/documents".equals(uri)) {
                return json(Response.Status.OK, new JSONObject().put("documents", engine.listDocuments()));
            }

            if (Method.POST.equals(method) && "/api/documents".equals(uri)) {
                return uploadDocument(session);
            }

            if (Method.POST.equals(method) && "/api/documents/text".equals(uri)) {
                JSONObject body = parseJsonBody(session);
                JSONObject document = engine.addDocument(
                        body.optString("name", "sample.md"),
                        body.optString("content", "")
                );
                return json(Response.Status.CREATED, new JSONObject().put("document", document));
            }

            if (Method.DELETE.equals(method) && uri.startsWith("/api/documents/")) {
                String rawId = uri.substring("/api/documents/".length());
                String documentId = URLDecoder.decode(rawId, "UTF-8");
                if (!engine.deleteDocument(documentId)) {
                    return error(Response.Status.NOT_FOUND, "document was not found");
                }
                return json(Response.Status.OK, new JSONObject().put("deleted", true));
            }

            if (Method.POST.equals(method) && "/api/chat".equals(uri)) {
                JSONObject body = parseJsonBody(session);
                return json(Response.Status.OK, engine.ask(body.optString("question", "")));
            }

            return error(Response.Status.NOT_FOUND, "endpoint was not found");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return error(Response.Status.BAD_REQUEST, exception.getMessage());
        } catch (Exception exception) {
            exception.printStackTrace();
            return error(Response.Status.INTERNAL_ERROR, "request failed: " + safeMessage(exception));
        }
    }

    private Response uploadDocument(IHTTPSession session) throws IOException, ResponseException {
        String contentType = session.getHeaders().get("content-type");
        if (contentType == null || !contentType.toLowerCase().startsWith("multipart/form-data")) {
            return error(Response.Status.BAD_REQUEST, "multipart/form-data is required");
        }

        Map<String, String> files = new HashMap<>();
        session.parseBody(files);
        String temporaryPath = files.get("file");
        String originalFileName = session.getParms().get("originalFileName");
        if (isBlank(originalFileName)) {
            originalFileName = session.getParms().get("file");
        }
        if (isBlank(temporaryPath) || isBlank(originalFileName)) {
            return error(Response.Status.BAD_REQUEST, "file and originalFileName are required");
        }

        String parsed = FileParserFactory.easyParse(temporaryPath, originalFileName);
        JSONObject document = engine.addDocument(originalFileName, parsed);
        return json(Response.Status.CREATED, new JSONObject().put("document", document));
    }

    private JSONObject parseJsonBody(IHTTPSession session) throws IOException, ResponseException {
        Map<String, String> files = new HashMap<>();
        session.parseBody(files);
        String body = files.get("postData");
        if (isBlank(body)) {
            throw new IllegalArgumentException("JSON request body is required");
        }
        return new JSONObject(body);
    }

    private Response json(Response.Status status, JSONObject value) {
        return withCors(newFixedLengthResponse(status, "application/json; charset=utf-8", value.toString()));
    }

    private Response error(Response.Status status, String message) {
        return json(status, new JSONObject().put("error", message == null ? "unknown error" : message));
    }

    private Response withCors(Response response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "Content-Type");
        response.addHeader("Cache-Control", "no-store");
        return response;
    }

    private static String safeMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return exception.getClass().getSimpleName();
        }
        return message.replaceAll("[\\r\\n]+", " ");
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String environment(String name, String fallback) {
        String value = System.getenv(name);
        return isBlank(value) ? fallback : value.trim();
    }

    private static int integerEnvironment(String name, int fallback) {
        String value = System.getenv(name);
        if (isBlank(value)) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(name + " must be an integer");
        }
    }

    public static void main(String[] args) throws IOException {
        int port = integerEnvironment("RAG_PLAYGROUND_PORT", 8080);
        int topK = integerEnvironment("RAG_PLAYGROUND_TOP_K", 4);
        String mode = environment("RAG_PLAYGROUND_MODE", "local").toLowerCase();

        ChatService chatService;
        String chatUrl;
        String model;
        if ("ollama".equals(mode)) {
            chatService = new OpenAIChatService(environment("RAG_OLLAMA_API_KEY", "ollama"));
            chatUrl = environment("RAG_OLLAMA_CHAT_URL", "http://localhost:11434/v1/chat/completions");
            model = environment("RAG_OLLAMA_MODEL", "qwen2.5:3b");
        } else if ("local".equals(mode)) {
            chatService = new PlaygroundExtractiveChatService();
            chatUrl = "offline://extractive";
            model = "offline-extractive";
        } else {
            throw new IllegalArgumentException("RAG_PLAYGROUND_MODE must be local or ollama");
        }

        PlaygroundEngine engine = new PlaygroundEngine(chatService, chatUrl, model, mode, topK);
        new PlaygroundServer(port, engine);
    }
}
