package org.rag;

import org.chunk.FixedSizeSplitter;
import org.chunk.ParagraphSplitter;
import org.constant.Config;
import org.entity.Document;
import org.json.JSONObject;
import org.parser.FileParserFactory;
import org.service.LLM.OpenAIChatService;
import org.service.embedding.BaichuanEmbeddingService;
import org.utils.DistanceUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ModularRAG {
    private Document document;
    private List<Document> chunks;
    private String query;
    private String response;
    private List<Document> filteredChunks;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public ModularRAG() {
    }

    public ModularRAG(Document document, String query) {
        this.document = document;
        this.query = query;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    /**
     * 解析文档
     * @return 当前对象，方便链式调用
     */
    public ModularRAG parsing() {
        String filePath = document.getStoragePath();
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("Document storage path cannot be blank");
        }
        String chunkText = FileParserFactory.easyParse(filePath);
        document.setChunkText(chunkText);
        return this;
    }

    /**
     * 分块处理，使用多种分块策略
     * @return 当前对象，方便链式调用
     */
    public ModularRAG chunking() {
        // 可以使用更复杂的分块策略，这里简单示例使用固定大小分块
        FixedSizeSplitter fixedSizeSplitter = new FixedSizeSplitter(512);
        List<String> stringList = fixedSizeSplitter.split(document.getChunkText());
        chunks = new ArrayList<>();
        for (String chunkText : stringList) {
            Document chunkDoc = new Document();
            chunkDoc.setChunkText(chunkText);
            chunks.add(chunkDoc);
        }

        // 额外的分块策略：按段落分块
        ParagraphSplitter paragraphSplitter = new ParagraphSplitter();
        List<String> paragraphChunks = paragraphSplitter.split(document.getChunkText());
        for (String paragraphChunk : paragraphChunks) {
            Document paragraphDoc = new Document();
            paragraphDoc.setChunkText(paragraphChunk);
            chunks.add(paragraphDoc);
        }

        return this;
    }

    /**
     * 嵌入处理
     * @return 当前对象，方便链式调用
     * @throws IOException 可能的IO异常
     */
    public ModularRAG embedding() throws IOException {
        BaichuanEmbeddingService embeddingService = new BaichuanEmbeddingService(Config.API_KEY);
        // 获取查询的嵌入向量
        double[] queryEmbedding = embeddingService.getEmbedding(Config.EMBEDDING_API_URL, query);

        // 为每个文档块生成嵌入向量
        for (Document chunk : chunks) {
            double[] chunkEmbedding = embeddingService.getEmbedding(Config.EMBEDDING_API_URL, chunk.getChunkText());
            chunk.setTextEmb(chunkEmbedding);
        }
        return this;
    }

    /**
     * 排序处理
     * @return 当前对象，方便链式调用
     * @throws IOException 可能的IO异常
     */
    public ModularRAG sorting() throws IOException {
        BaichuanEmbeddingService embeddingService = new BaichuanEmbeddingService(Config.API_KEY);
        // 获取查询的嵌入向量
        double[] queryEmbedding = embeddingService.getEmbedding(Config.EMBEDDING_API_URL, query);
        // 根据嵌入向量与查询的嵌入向量之间的距离对文档块进行排序
        chunks.sort(Comparator.comparingDouble(chunk -> DistanceUtils.squaredErrorDistance(queryEmbedding, chunk.getTextEmb())));
        return this;
    }

    /**
     * 高级筛选，例如过滤掉一些不相关的块
     * @return 当前对象，方便链式调用
     */
    public ModularRAG advancedFiltering() {
        // 简单示例：过滤掉长度小于10的块
        filteredChunks = chunks.stream()
                .filter(chunk -> chunk.getChunkText().length() > 10)
                .collect(Collectors.toList());

        // 额外的筛选：过滤掉包含特定关键词的块
        String keywordToExclude = "无关内容";
        filteredChunks = filteredChunks.stream()
                .filter(chunk -> !chunk.getChunkText().contains(keywordToExclude))
                .collect(Collectors.toList());

        return this;
    }

    /**
     * 对筛选后的块进行重新排序
     * @return 当前对象，方便链式调用
     * @throws IOException 可能的IO异常
     */
    public ModularRAG reSortingFilteredChunks() throws IOException {
        BaichuanEmbeddingService embeddingService = new BaichuanEmbeddingService(Config.API_KEY);
        // 获取查询的嵌入向量
        double[] queryEmbedding = embeddingService.getEmbedding(Config.EMBEDDING_API_URL, query);
        // 根据嵌入向量与查询的嵌入向量之间的距离对筛选后的文档块进行排序
        filteredChunks.sort(Comparator.comparingDouble(chunk -> DistanceUtils.squaredErrorDistance(queryEmbedding, chunk.getTextEmb())));
        return this;
    }

    /**
     * 大模型聊天
     * @return 当前对象，方便链式调用
     */
    public ModularRAG LLMChat() {
        // 替换为您的API密钥
        String apiKey = Config.API_KEY;
        // 使用百川Baichuan3-Turbo模型
        String model = Config.LLM_MODEL;
        // API的URL
        String url = Config.LLM_URL;

        OpenAIChatService openAIChatService = new OpenAIChatService(apiKey);

        try {
            // 构建请求参数
            JSONObject params = new JSONObject()
                    .put("model", model)
                    .put("messages", new JSONObject[] {
                            new JSONObject().put("role", "user").put("content", query)
                    })
                    .put("temperature", 0.3)
                    .put("stream", false);

            // 调用大模型生成回复
            response = openAIChatService.generateText(url, params);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * 对生成的回复进行后处理
     * @return 当前对象，方便链式调用
     */
    public ModularRAG postProcessing() {
        // 简单示例：去除回复中的多余空格
        if (response != null) {
            response = response.replaceAll("\\s+", " ");
        }
        return this;
    }

    public static void main(String[] args) {
        ModularRAG modularRAG = new ModularRAG(
                new Document("./202X企业规划.pdf"),
                "简要总结这篇文章");
        try {
            modularRAG
                    // 解析
                    .parsing()
                    // 分块
                    .chunking()
                    // 向量化
                    .embedding()
                    // 排序
                    .sorting()
                    // 高级筛选
                    .advancedFiltering()
                    // 对筛选后的块重新排序
                    .reSortingFilteredChunks()
                    // 大模型回复
                    .LLMChat()
                    // 后处理
                    .postProcessing();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(modularRAG.response);
    }
}