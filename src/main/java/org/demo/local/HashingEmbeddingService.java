package org.demo.local;

import org.service.embedding.EmbeddingService;

import java.io.IOException;
import java.util.Locale;

/**
 * Small deterministic embedding provider for demos and tests.
 *
 * <p>It uses feature hashing instead of a neural model, so it is not intended
 * for production retrieval quality. Its purpose is to make the full RAG
 * pipeline runnable without an API key, model download, or database.</p>
 */
public final class HashingEmbeddingService implements EmbeddingService {

    private static final int DEFAULT_DIMENSIONS = 256;

    private final int dimensions;

    public HashingEmbeddingService() {
        this(DEFAULT_DIMENSIONS);
    }

    public HashingEmbeddingService(int dimensions) {
        if (dimensions <= 0) {
            throw new IllegalArgumentException("dimensions must be greater than zero");
        }
        this.dimensions = dimensions;
    }

    @Override
    public double[] getEmbedding(String url, String input) throws IOException {
        return embed(input);
    }

    @Override
    public double[][] getEmbeddings(String url, String[] inputs) throws IOException {
        if (inputs == null) {
            throw new IOException("inputs cannot be null");
        }
        double[][] result = new double[inputs.length][];
        for (int i = 0; i < inputs.length; i++) {
            result[i] = embed(inputs[i]);
        }
        return result;
    }

    private double[] embed(String input) throws IOException {
        if (input == null || input.trim().isEmpty()) {
            throw new IOException("input cannot be blank");
        }

        double[] vector = new double[dimensions];
        String normalized = input.toLowerCase(Locale.ROOT);
        String[] tokens = normalized.split("[^\\p{L}\\p{N}]+");

        int tokenCount = 0;
        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }
            tokenCount++;
            addFeature(vector, token, 1.0);

            // Character trigrams make the demo a little more tolerant of
            // pluralization and related word forms without external models.
            if (token.length() >= 3) {
                for (int i = 0; i <= token.length() - 3; i++) {
                    addFeature(vector, "#" + token.substring(i, i + 3), 0.35);
                }
            }
        }

        if (tokenCount == 0) {
            throw new IOException("input does not contain embeddable tokens");
        }

        normalize(vector);
        return vector;
    }

    private void addFeature(double[] vector, String feature, double weight) {
        int index = Math.floorMod(feature.hashCode(), dimensions);
        vector[index] += weight;
    }

    private static void normalize(double[] vector) {
        double squaredMagnitude = 0.0;
        for (double value : vector) {
            squaredMagnitude += value * value;
        }
        if (squaredMagnitude == 0.0) {
            return;
        }
        double magnitude = Math.sqrt(squaredMagnitude);
        for (int i = 0; i < vector.length; i++) {
            vector[i] /= magnitude;
        }
    }
}
