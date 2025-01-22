# JAVA-RAG

### 介绍
RAG (Retrieval-Augmented Generation)项目,pure Java 实现,不依赖JFinal,spring-boot等。更便于依托企业级环境进行改造,更利于二次开发
### 用法教程
#### 💽 数据库存储
- Redis 多轮对话读写
- MinIO 文件存储
- Elastic Search 搜索引擎
#### 🧠 LLM 对话
- OpenAI 聊天接口
- Ollama 聊天接口
- 带有多轮对话的聊天
#### 📚 文档解析
- Word
- PPT
- PDF
- EXCEL
- PPT
- MarkDow,HTML
#### ✂️ 分块
- 固定大小
- 句子分割
- 递归分割
- 语义分块
#### 📊 向量化模型
- Jina-Cobert
- Baichuan
#### 🔎 搜索
- 召回
- 排序
- 重排序
#### 项目结构
说明
```shell
├── LICENSE
├── README.en.md
├── README.md
├── assert
│   ├── es.sql
│   └── install.sh
├── pom.xml
├── src
│   ├── main
│   │   ├── java
│   │   │   └── org
│   │   │       ├── chunk
│   │   │       │   ├── FixedSizeSplitter.java
│   │   │       │   ├── RecursiveSplitter.java
│   │   │       │   ├── SemanticBlockSplitter.java
│   │   │       │   ├── SentenceSplitter.java
│   │   │       │   └── TextSplitter.java
│   │   │       ├── constant
│   │   │       │   └── Config.java
│   │   │       ├── entity
│   │   │       │   ├── Document.java
│   │   │       │   ├── File.java
│   │   │       │   ├── KnowledgeBase.java
│   │   │       │   ├── SearchInput.java
│   │   │       │   ├── SearchOutput.java
│   │   │       │   └── User.java
│   │   │       ├── parser
│   │   │       │   ├── ExcelParser.java
│   │   │       │   ├── FileParser.java
│   │   │       │   ├── FileParserFactory.java
│   │   │       │   ├── HTMLParser.java
│   │   │       │   ├── PDFParser.java
│   │   │       │   ├── PPTParser.java
│   │   │       │   ├── PureTextParser.java
│   │   │       │   └── WordParser.java
│   │   │       ├── search
│   │   │       │   └── Pipeline.java
│   │   │       ├── service
│   │   │       │   ├── LLMService.java
│   │   │       │   ├── Main.java
│   │   │       │   ├── db
│   │   │       │   │   ├── ESClient.java
│   │   │       │   │   ├── MinIOClient.java
│   │   │       │   │   ├── MysqlClient.java
│   │   │       │   │   └── RedisClient.java
│   │   │       │   └── embedding
│   │   │       │       ├── BaichuanEmbeddingService.java
│   │   │       │       ├── EmbeddingService.java
│   │   │       │       └── JinaEmbeddingService.java
│   │   │       └── utils
│   │   │           ├── HttpClientUtil.java
│   │   │           ├── SnowflakeIdGenerator.java
│   │   │           └── TrustAllCerts.java
│   │   └── resources
│   └── test
│       └── java
│           └── org
│               └── db
│                   ├── ESClientTest.java
│                   └── ElasticsearchConnectionTest.java
└── target

```

#### 安装教程

1.  clone 代码
```shell
git clone https://gitee.com/ChinaYiqun/java-rag.git
```    
2. 进入项目目录
```shell
cd java-rag
```
3. 配置 Maven 依赖
```shell
mvn clean install
```


#### 功能点

- OpenAI式 LLM/Embedding 接口
- 十分简洁的依赖项管理 , pom.xml(Maven)
- 支持多用户、多知识库 管理
- 搜索策略自由编排:多路召回/粗排/精排/重排
- 文件分块自由编排：固定大小/句子分割/递归分割/语义分块
- 主流文件解析支持 Apache POI
- 主流数据库集成 Elastic Search/Redis/Mysql/MinIO
- 配置灵活度高度定制 Nacos