import { useEffect, useRef, useState } from 'react'
import type { ChangeEvent, DragEvent, FormEvent, KeyboardEvent } from 'react'

type Health = {
  status: string
  mode: string
  model: string
}

type DocumentItem = {
  id: string
  name: string
  characters: number
  chunks: number
}

type Citation = {
  index: number
  documentId: string
  documentName: string
  chunkId: number
  score: number
  content: string
}

type ChatResult = {
  question: string
  answer: string
  mode: string
  model: string
  citations: Citation[]
}

type ChatMessage = {
  id: string
  role: 'assistant' | 'user'
  content: string
}

const sampleDocument = {
  name: 'java-rag-playground-guide.md',
  content: `# Java-RAG Playground Guide

Java-RAG is a framework-free retrieval-augmented generation toolkit compatible with Java 8 and newer runtimes.

The visual playground starts with Docker Compose. Run docker compose up --build and open http://localhost:3000 in a browser.

The default local mode uses deterministic hashing embeddings and an extractive answer generator, so it requires no cloud API key or model download.

Every answer returns the retrieved document name, chunk identifier, similarity score, and original passage. This evidence is displayed beside the conversation.

To use a real local language model, set RAG_PLAYGROUND_MODE=ollama and start the Ollama Docker Compose profile.`,
}

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const response = await fetch(url, options)
  const payload = (await response.json()) as T & { error?: string }
  if (!response.ok) {
    throw new Error(payload.error || `Request failed with status ${response.status}`)
  }
  return payload
}

function createMessage(role: ChatMessage['role'], content: string): ChatMessage {
  return {
    id: `${Date.now()}-${Math.random().toString(16).slice(2)}`,
    role,
    content,
  }
}

function App() {
  const [health, setHealth] = useState<Health | null>(null)
  const [documents, setDocuments] = useState<DocumentItem[]>([])
  const [messages, setMessages] = useState<ChatMessage[]>([
    createMessage(
      'assistant',
      '上传文档或加载示例，然后向知识库提问。我会同时返回答案和真实命中的 Top-K 原文。',
    ),
  ])
  const [question, setQuestion] = useState('')
  const [latestResult, setLatestResult] = useState<ChatResult | null>(null)
  const [activeCitation, setActiveCitation] = useState(0)
  const [busy, setBusy] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [dragging, setDragging] = useState(false)
  const fileInput = useRef<HTMLInputElement>(null)
  const conversationEnd = useRef<HTMLDivElement>(null)

  useEffect(() => {
    void refresh()
  }, [])

  useEffect(() => {
    conversationEnd.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, busy])

  async function refresh() {
    try {
      const [healthResult, documentsResult] = await Promise.all([
        request<Health>('/api/health'),
        request<{ documents: DocumentItem[] }>('/api/documents'),
      ])
      setHealth(healthResult)
      setDocuments(documentsResult.documents)
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : '无法连接 Java-RAG API')
    }
  }

  async function refreshDocuments() {
    const result = await request<{ documents: DocumentItem[] }>('/api/documents')
    setDocuments(result.documents)
  }

  async function uploadFile(file: File) {
    setBusy('upload')
    setError(null)
    try {
      const form = new FormData()
      form.append('file', file)
      form.append('originalFileName', file.name)
      await request<{ document: DocumentItem }>('/api/documents', {
        method: 'POST',
        body: form,
      })
      await refreshDocuments()
      setMessages((current) => [
        ...current,
        createMessage('assistant', `已完成 ${file.name} 的解析、分块和索引。现在可以开始提问。`),
      ])
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : '文档上传失败')
    } finally {
      setBusy(null)
      if (fileInput.current) fileInput.current.value = ''
    }
  }

  async function loadSample() {
    setBusy('sample')
    setError(null)
    try {
      await request<{ document: DocumentItem }>('/api/documents/text', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(sampleDocument),
      })
      await refreshDocuments()
      setMessages((current) => [
        ...current,
        createMessage('assistant', '示例文档已加载。试试询问：“如何启动可视化 Playground？”'),
      ])
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : '示例加载失败')
    } finally {
      setBusy(null)
    }
  }

  async function removeDocument(documentId: string, name: string) {
    setBusy(documentId)
    setError(null)
    try {
      await request<{ deleted: boolean }>(`/api/documents/${encodeURIComponent(documentId)}`, {
        method: 'DELETE',
      })
      await refreshDocuments()
      setMessages((current) => [...current, createMessage('assistant', `已从知识库移除 ${name}。`)])
      if (latestResult?.citations.some((citation) => citation.documentId === documentId)) {
        setLatestResult(null)
      }
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : '删除失败')
    } finally {
      setBusy(null)
    }
  }

  async function askQuestion(event?: FormEvent) {
    event?.preventDefault()
    const trimmed = question.trim()
    if (!trimmed || busy) return

    setQuestion('')
    setBusy('chat')
    setError(null)
    setMessages((current) => [...current, createMessage('user', trimmed)])

    try {
      const result = await request<ChatResult>('/api/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ question: trimmed }),
      })
      setLatestResult(result)
      setActiveCitation(0)
      setMessages((current) => [...current, createMessage('assistant', result.answer)])
    } catch (caught) {
      const message = caught instanceof Error ? caught.message : '问答请求失败'
      setError(message)
      setMessages((current) => [...current, createMessage('assistant', `请求失败：${message}`)])
    } finally {
      setBusy(null)
    }
  }

  function handleFileInput(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0]
    if (file) void uploadFile(file)
  }

  function handleDrop(event: DragEvent<HTMLDivElement>) {
    event.preventDefault()
    setDragging(false)
    const file = event.dataTransfer.files?.[0]
    if (file) void uploadFile(file)
  }

  function handleQuestionKeyDown(event: KeyboardEvent<HTMLTextAreaElement>) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault()
      void askQuestion()
    }
  }

  const selectedCitation = latestResult?.citations[activeCitation]

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand-lockup">
          <div className="brand-mark">JR</div>
          <div>
            <h1>Java-RAG Playground</h1>
            <p>Framework-free RAG · Java 8+ · Evidence first</p>
          </div>
        </div>
        <div className="runtime-status">
          <span className={`status-dot ${health ? 'online' : ''}`} />
          <span>{health ? `${health.mode} · ${health.model}` : 'API connecting'}</span>
          <a href="https://github.com/ChinaYiqun/java-rag" target="_blank" rel="noreferrer">
            GitHub ↗
          </a>
        </div>
      </header>

      {error && (
        <div className="error-banner" role="alert">
          <span>{error}</span>
          <button onClick={() => setError(null)} aria-label="关闭错误提示">×</button>
        </div>
      )}

      <main className="workspace">
        <aside className="panel library-panel">
          <div className="panel-heading">
            <div>
              <span className="eyebrow">KNOWLEDGE BASE</span>
              <h2>知识库</h2>
            </div>
            <span className="count-badge">{documents.length}</span>
          </div>

          <div
            className={`drop-zone ${dragging ? 'dragging' : ''}`}
            onDragEnter={(event) => {
              event.preventDefault()
              setDragging(true)
            }}
            onDragOver={(event) => event.preventDefault()}
            onDragLeave={() => setDragging(false)}
            onDrop={handleDrop}
            onClick={() => fileInput.current?.click()}
            role="button"
            tabIndex={0}
            onKeyDown={(event) => {
              if (event.key === 'Enter' || event.key === ' ') fileInput.current?.click()
            }}
          >
            <input
              ref={fileInput}
              type="file"
              accept=".pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.md,.txt,.html,.java,.py"
              onChange={handleFileInput}
              hidden
            />
            <span className="upload-icon">↑</span>
            <strong>{busy === 'upload' ? '正在解析与索引…' : '拖入或选择文档'}</strong>
            <small>PDF · Word · PPT · Excel · Markdown · TXT</small>
          </div>

          <button className="sample-button" onClick={() => void loadSample()} disabled={Boolean(busy)}>
            <span>✦</span> {busy === 'sample' ? '加载中…' : '加载内置示例'}
          </button>

          <div className="document-list">
            {documents.length === 0 ? (
              <div className="empty-state compact">
                <span>◇</span>
                <p>还没有文档</p>
                <small>上传文件后会在这里看到分块数量。</small>
              </div>
            ) : (
              documents.map((document) => (
                <article className="document-card" key={document.id}>
                  <div className="file-glyph">{document.name.split('.').pop()?.slice(0, 3).toUpperCase()}</div>
                  <div className="document-meta">
                    <strong title={document.name}>{document.name}</strong>
                    <span>{document.chunks} chunks · {document.characters.toLocaleString()} chars</span>
                  </div>
                  <button
                    className="icon-button"
                    aria-label={`删除 ${document.name}`}
                    disabled={busy === document.id}
                    onClick={() => void removeDocument(document.id, document.name)}
                  >
                    ×
                  </button>
                </article>
              ))
            )}
          </div>
        </aside>

        <section className="panel conversation-panel">
          <div className="panel-heading conversation-heading">
            <div>
              <span className="eyebrow">GROUNDED CHAT</span>
              <h2>知识问答</h2>
            </div>
            <span className="mode-pill">Top-K {latestResult?.citations.length ?? 4}</span>
          </div>

          <div className="conversation-stream">
            {messages.map((message) => (
              <div className={`message-row ${message.role}`} key={message.id}>
                <div className="avatar">{message.role === 'assistant' ? 'AI' : 'YOU'}</div>
                <div className="message-bubble">{message.content}</div>
              </div>
            ))}
            {busy === 'chat' && (
              <div className="message-row assistant">
                <div className="avatar">AI</div>
                <div className="message-bubble thinking"><i /><i /><i /></div>
              </div>
            )}
            <div ref={conversationEnd} />
          </div>

          <form className="composer" onSubmit={askQuestion}>
            <textarea
              value={question}
              onChange={(event) => setQuestion(event.target.value)}
              onKeyDown={handleQuestionKeyDown}
              placeholder={documents.length ? '向已上传文档提问…' : '先上传文档或加载示例…'}
              disabled={!documents.length || busy === 'chat'}
              rows={3}
            />
            <div className="composer-footer">
              <span>Enter 发送 · Shift + Enter 换行</span>
              <button type="submit" disabled={!question.trim() || !documents.length || Boolean(busy)}>
                {busy === 'chat' ? '检索中' : '发送'} <b>↗</b>
              </button>
            </div>
          </form>
        </section>

        <aside className="panel evidence-panel">
          <div className="panel-heading">
            <div>
              <span className="eyebrow">RETRIEVAL EVIDENCE</span>
              <h2>检索证据</h2>
            </div>
            {latestResult && <span className="count-badge">{latestResult.citations.length}</span>}
          </div>

          {!latestResult ? (
            <div className="empty-state evidence-empty">
              <span>⌁</span>
              <p>等待一次问答</p>
              <small>这里会展示真实命中的文档、Chunk ID、相似度和原文。</small>
            </div>
          ) : (
            <>
              <div className="citation-tabs" role="tablist" aria-label="检索证据">
                {latestResult.citations.map((citation, index) => (
                  <button
                    key={`${citation.documentId}-${citation.chunkId}`}
                    className={activeCitation === index ? 'active' : ''}
                    onClick={() => setActiveCitation(index)}
                    role="tab"
                    aria-selected={activeCitation === index}
                  >
                    [{citation.index}]
                  </button>
                ))}
              </div>

              {selectedCitation && (
                <article className="evidence-card">
                  <div className="evidence-source">
                    <div className="source-mark">{selectedCitation.index}</div>
                    <div>
                      <strong>{selectedCitation.documentName}</strong>
                      <span>Chunk #{selectedCitation.chunkId}</span>
                    </div>
                  </div>

                  <div className="score-block">
                    <div className="score-label">
                      <span>相似度</span>
                      <strong>{Math.round(selectedCitation.score * 100)}%</strong>
                    </div>
                    <div className="score-track">
                      <i style={{ width: `${Math.max(3, Math.min(100, selectedCitation.score * 100))}%` }} />
                    </div>
                  </div>

                  <blockquote>{selectedCitation.content}</blockquote>
                  <div className="evidence-footer">
                    <span>原始检索片段</span>
                    <span>citation [{selectedCitation.index}]</span>
                  </div>
                </article>
              )}

              <div className="evidence-notice">
                <strong>Evidence-first</strong>
                <p>答案和证据来自同一次后端检索结果，前端不会自行拼接虚假引用。</p>
              </div>
            </>
          )}
        </aside>
      </main>
    </div>
  )
}

export default App
