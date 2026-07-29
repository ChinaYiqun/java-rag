package org.constant;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConfigTest {

    @BeforeClass
    public static void setUpConfiguration() {
        System.setProperty("rag.api.key", "test-api-key");
        System.setProperty("rag.redis.port", "6380");
    }

    @AfterClass
    public static void clearConfiguration() {
        System.clearProperty("rag.api.key");
        System.clearProperty("rag.redis.port");
    }

    @Test
    public void systemPropertiesOverrideDefaults() {
        assertEquals("test-api-key", Config.API_KEY);
        assertEquals(6380, Config.REDIS_PORT);
    }

    @Test(expected = IllegalStateException.class)
    public void requireRejectsBlankSecrets() {
        Config.require("  ", "RAG_API_KEY");
    }
}
