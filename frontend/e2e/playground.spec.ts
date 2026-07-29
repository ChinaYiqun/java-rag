import { expect, test } from '@playwright/test'
import { mkdirSync } from 'node:fs'
import { resolve } from 'node:path'

test('uploads sample, asks a grounded question, and captures real evidence', async ({ page }) => {
  await page.goto('/')

  await expect(page.getByRole('heading', { name: 'Java-RAG Playground' })).toBeVisible()
  await expect(page.getByText('local · offline-extractive')).toBeVisible()

  await page.getByRole('button', { name: /加载内置示例/ }).click()

  const documentCard = page.locator('.document-card').filter({ hasText: 'java-rag-playground-guide.md' })
  await expect(documentCard).toBeVisible()
  await expect(documentCard).toContainText('chunks')

  const question = page.getByPlaceholder('向已上传文档提问…')
  await expect(question).toBeEnabled()
  await question.fill('如何启动可视化 Playground？')
  await page.getByRole('button', { name: /^发送/ }).click()

  const latestAssistantMessage = page.locator('.message-row.assistant').last()
  await expect(latestAssistantMessage).toContainText('docker compose up --build')
  await expect(latestAssistantMessage).toContainText('[1]')

  const evidenceCard = page.locator('.evidence-card')
  await expect(evidenceCard).toBeVisible()
  await expect(evidenceCard).toContainText('java-rag-playground-guide.md')
  await expect(evidenceCard).toContainText('Chunk #')
  await expect(evidenceCard).toContainText('相似度')
  await expect(evidenceCard).toContainText('docker compose up --build')

  const screenshotDirectory = resolve(process.cwd(), '../docs/assets')
  mkdirSync(screenshotDirectory, { recursive: true })
  await page.screenshot({
    path: resolve(screenshotDirectory, 'playground.png'),
    fullPage: true,
  })
})
