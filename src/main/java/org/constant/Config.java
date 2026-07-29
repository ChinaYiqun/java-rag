package org.constant;

/**
 * Runtime configuration for Java-RAG.
 *
 * <p>Configuration is resolved in this order:</p>
 * <ol>
 *     <li>JVM system property, for example {@code -Drag.api.key=...}</li>
 *     <li>Environment variable, for example {@code RAG_API_KEY=...}</li>
 *     <li>A safe non-secret default where one exists</li>
 * </ol>
 *
 * <p>Secrets intentionally have no default value. Never commit credentials to source control.</p>
 */
public final class Config {

    private Config() {
    }

    // LLM and embedding configuration
    public static final String API_KEY = value("RAG_API_KEY", "rag.api.key", "");
    public static final String EMBEDDING_API_URL = value(
            "RAG_EMBEDDING_API_URL",
            "rag.embedding.api-url",
            "https://api.baichuan-ai.com/v1/embeddings"
    );
    public static final String LLM_MODEL = value("RAG_LLM_MODEL", "rag.llm.model", "Baichuan3-Turbo");
    public static final String LLM_URL = value(
            "RAG_LLM_URL",
            "rag.llm.url",
            "https://api.baichuan-ai.com/v1/chat/completions"
    );

    // Redis configuration
    public static final String REDIS_HOST = value("RAG_REDIS_HOST", "rag.redis.host", "127.0.0.1");
    public static final int REDIS_PORT = intValue("RAG_REDIS_PORT", "rag.redis.port", 6379);
    public static final String REDIS_PASSWORD = value("RAG_REDIS_PASSWORD", "rag.redis.password", "");
    public static int REDIS_EXPIRE_SECONDS = intValue(
            "RAG_REDIS_EXPIRE_SECONDS",
            "rag.redis.expire-seconds",
            180
    );

    // Elasticsearch configuration. Field names are retained for source compatibility.
    public static final String esUrl = value("RAG_ES_URL", "rag.es.url", "http://127.0.0.1:9200");
    public static final String esUserName = value("RAG_ES_USERNAME", "rag.es.username", "elastic");
    public static final String esPassWord = value("RAG_ES_PASSWORD", "rag.es.password", "");

    // Jina configuration
    public static final String Jina_API_KEY = value("RAG_JINA_API_KEY", "rag.jina.api-key", "");
    public static final String Jina_multi_vector = value(
            "RAG_JINA_MULTI_VECTOR_URL",
            "rag.jina.multi-vector-url",
            "https://api.jina.ai/v1/multi-vector"
    );

    // SerpAPI configuration
    public static final String SerpAPI = value("RAG_SERP_API_KEY", "rag.serp.api-key", "");

    /**
     * Fail fast at the boundary where a provider credential is actually required.
     */
    public static String require(String value, String configurationName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Missing required configuration: " + configurationName
                            + ". Set the documented environment variable or JVM system property."
            );
        }
        return value;
    }

    private static String value(String environmentName, String propertyName, String defaultValue) {
        String propertyValue = System.getProperty(propertyName);
        if (propertyValue != null && !propertyValue.trim().isEmpty()) {
            return propertyValue.trim();
        }

        String environmentValue = System.getenv(environmentName);
        if (environmentValue != null && !environmentValue.trim().isEmpty()) {
            return environmentValue.trim();
        }

        return defaultValue;
    }

    private static int intValue(String environmentName, String propertyName, int defaultValue) {
        String rawValue = value(environmentName, propertyName, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(rawValue);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                    "Configuration " + environmentName + " / " + propertyName
                            + " must be an integer, but was: " + rawValue,
                    exception
            );
        }
    }
}
