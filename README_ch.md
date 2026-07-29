<div align="center">
  <img src="webapp/resources/biglog.png" alt="Java-RAG logo" width="560" />

# Java-RAG

**面向 Java 8+ 的无框架、可拆解、可二次开发 RAG 工具箱**

不把检索增强生成藏进框架黑盒，让你看清并替换每一个环节。

[![Java-RAG CI](https://github.com/ChinaYiqun/java-rag/actions/workflows/ci.yml/badge.svg)](https://github.com/ChinaYiqun/java-rag/actions/workflows/ci.yml)
[![Java 8+](https://img.shields.io/badge/Java-8%2B-ED8B00?logo=openjdk&logoColor=white)](https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html)
[![License](https://img.shields.io/github/license/ChinaYiqun/java-rag)](LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/ChinaYiqun/java-rag?style=social)](https://github.com/ChinaYiqun/java-rag/stargazers)

[English](README.md) · [简体中文](README_ch.md) · [架构说明](docs/ARCHITECTURE.md) · [参与贡献](CONTRIBUTING.md)
</div>

---

Java-RAG 使用纯 Java 展示完整 RAG 数据链路：

```text
文档 -> 解析 -> 分块 -> 向量化 -> 检索排序 -> Top-K 上下文 -> Grounded Prompt -> 答案
```

它适合三类开发者：

- 想真正理解 RAG 内部流程，而不是只会调用框架 API；
- 需要把 RAG 嵌入已有 Java 系统、银行系统或传统企业应用；
- 希望自由替换模型、向量服务、分块策略和存储，而不强制引入 Spring Boot。

## 为什么值得关注？

- **零 API Key 即可运行**：自带完整离线 Demo，不需要模型下载、数据库或付费接口；
- **流程透明**：Query Embedding、Chunk Embedding、排序、Top-K 和最终 Prompt 都能检查；
- **接口可替换**：Splitter、EmbeddingService、ChatService 均可注入；
- **兼容 Java 8**：更适合传统企业和存量 JVM 环境；
- **包含真实工程组件**：PDF/Word/PPT/Excel 解析、Redis、Elasticsearch、MinIO、MySQL、重排和负载均衡；
- **带安全护栏**：CI 检查硬编码凭据，运行时配置全部外置。

## 60 秒跑通完整 RAG

无需 API Key，无需数据库，无需下载模型。

```bash
git clone https://github.com/ChinaYiqun/java-rag.git
cd java-rag
bash scripts/run-no-api-demo.sh
```

Windows 没有 Bash 时可直接执行：

```powershell
mvn -q -DskipTests compile org.codehaus.mojo:exec-maven-plugin:3.5.0:java -Dexec.mainClass=org.demo.NoApiKeyRagDemo
```

预期输出：

```text
Java-RAG: no API key demo
Question: Which option keeps prompts and model data on the local machine?

Top retrieved passage:
Ollama runs language models on the local machine...

Answer:
Offline extractive answer: Ollama runs language models on the local machine... [1]
```

离线 Demo 使用确定性的 Feature Hashing 和抽取式生成器，目的是让你本地验证完整 RAG 数据链路，不是假装替代生产级 Embedding 模型或大模型。

## 工作流程

```mermaid
flowchart LR
    A[文档] --> B[解析器]
    B --> C[分块器]
    C --> D[EmbeddingService]
    D --> E[相似度排序]
    E --> F[Top-K 文档块]
    F --> G[证据化 Prompt]
    G --> H[ChatService]
    H --> I[带引用答案]
```

当前 `NaiveRAG` 已真正完成：

1. 文本分块；
2. Query Embedding 只计算一次；
3. 批量生成文档块向量；
4. 根据距离排序；
5. 只把 Top-K 内容放入 Prompt；
6. 要求模型基于上下文回答，并使用 `[1]`、`[2]` 标注证据。

详细设计见 [架构说明](docs/ARCHITECTURE.md)。

## 最小使用示例

```java
Document document = new Document("./企业制度.pdf");

NaiveRAG rag = new NaiveRAG(document, "报销审批需要哪些材料？")
        .parsing()
        .chunking()
        .embedding()
        .sorting()
        .LLMChat();

System.out.println(rag.getResponse());
System.out.println(rag.getRetrievedChunks());
```

真实模型配置使用环境变量或 JVM 参数：

```bash
export RAG_API_KEY='replace-me'
export RAG_LLM_URL='https://your-provider.example/v1/chat/completions'
export RAG_EMBEDDING_API_URL='https://your-provider.example/v1/embeddings'
```

完整构造函数支持替换每个核心组件：

```java
NaiveRAG rag = new NaiveRAG(
        document,
        question,
        textSplitter,
        embeddingService,
        chatService,
        embeddingUrl,
        chatUrl,
        model,
        4
);
```

## 已包含能力

| 领域 | 能力 |
|---|---|
| RAG Pipeline | Naive RAG、实验性 Advanced RAG、Modular RAG |
| 文档解析 | PDF、Word、PowerPoint、Excel、Markdown、HTML |
| 分块 | 固定长度、段落、句子、递归、语义分块 |
| 向量化 | OpenAI 兼容/百川风格接口、Jina 相关实现、离线 Hashing Demo |
| 检索 | 内存距离排序、多路召回、重排组件 |
| 存储 | Elasticsearch、Redis、MySQL、MinIO |
| 模型调用 | OpenAI 兼容 Chat 接口、Ollama 使用路径 |
| Agent | 多 Agent 示例和工具调用基础组件 |
| 工程能力 | Nacos 配置、轮询和权重随机负载均衡 |

## 当前成熟度

| 模块 | 状态 | 说明 |
|---|---|---|
| `NaiveRAG` 证据化流程 | **可用** | 离线测试验证 Top-K 文档块确实进入生成阶段 |
| 配置与 CI | **可用** | Java 8 构建、凭据检查、零 Key Demo 每次 PR 都运行 |
| 文档解析和基础设施客户端 | **已提供** | 不同组件的集成测试深度仍有差异 |
| `AdvancedRAG` / `ModularRAG` | **实验中** | 后续统一到共享 Pipeline Core |
| RAG 评测和可观测性 | **规划中** | 将补充检索指标、答案 Grounding 和运行链路证据 |

## 与 LangChain4j、Spring AI 的区别

Java-RAG 不试图替代成熟 Java AI 框架。

需要大量模型适配、框架生态和成熟生产抽象时，可以优先使用 LangChain4j 或 Spring AI。下面这些情况更适合 Java-RAG：

- 想看懂 RAG 每一步到底做了什么；
- 需要一个适合教学、面试、实验和二次开发的小型代码库；
- 已有传统 Java 项目，不希望强制迁移框架；
- 希望直接控制分块、检索、Prompt 和模型调用；
- 必须兼容 Java 8。

## 文档导航

- [架构与扩展点](docs/ARCHITECTURE.md)
- [安装教程](doc/install.md)
- [LLM 对话](doc/LLM.md)
- [文档解析](doc/parser.md)
- [分块策略](doc/chunk.md)
- [Embedding](doc/embedding.md)
- [搜索与重排](doc/search.md)
- [Advanced / Modular RAG](doc/pipeline.md)
- [数据库集成](doc/db.md)
- [负载均衡](doc/balance.md)
- [安全说明](SECURITY.md)

## 开发验证

```bash
bash scripts/check-no-hardcoded-secrets.sh
mvn --batch-mode --no-transfer-progress test
bash scripts/run-no-api-demo.sh
```

欢迎参与。适合首次贡献的任务包括：补充 Parser/Splitter 测试、增加离线 Provider 示例、补检索结果元数据、清理依赖、完善中英文文档。详见 [CONTRIBUTING.md](CONTRIBUTING.md)。

## Roadmap

- [ ] 提取 Naive、Advanced、Modular 共用的 `RagPipeline` Core
- [ ] 拆分离线索引和在线检索 API
- [ ] 统一带来源、分数和元数据的检索结果对象
- [ ] 增加 BM25 + Vector 混合检索示例
- [ ] 增加重排和 Query Rewrite 示例
- [ ] 增加可复现的 RAG 评测套件
- [ ] 增加 Docker Compose 生产 Demo 和轻量 Web Playground

## 安全

禁止提交 API Key、密码、Token、`.env` 文件和私有服务地址。运行时凭据从环境变量或 JVM 参数读取。部署数据库和模型服务前请先阅读 [SECURITY.md](SECURITY.md)。

## License

[Apache License 2.0](LICENSE)

---

如果 Java-RAG 帮你理解或搭建了 Java RAG，欢迎点一个 **Star**，也可以把零 Key Demo 分享给其他 Java 开发者。Star 会帮助更多人发现这条不依赖框架黑盒的 RAG 学习路径。
