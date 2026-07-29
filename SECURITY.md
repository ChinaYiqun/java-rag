# Security Policy

## Exposed credentials

Java-RAG previously contained provider and infrastructure credentials in a public source file. Removing a credential from the current branch does **not** make it secret again because it remains available in Git history, forks, caches, and clones.

Treat every credential that was committed as compromised.

### Required response

1. Revoke and rotate the affected LLM/embedding provider key.
2. Revoke and rotate the Jina key.
3. Revoke and rotate the SerpAPI key.
4. Change the Redis password and restrict Redis to trusted networks.
5. Change the Elasticsearch password and restrict Elasticsearch to trusted networks.
6. Review provider, Redis, and Elasticsearch access logs for unexpected use.
7. Store replacement secrets in a deployment secret manager or environment variables, never in tracked files.

Rewriting Git history may reduce accidental discovery, but it does not replace credential rotation.

## Runtime configuration

Use the variables documented in [`.env.example`](.env.example). Java-RAG reads environment variables or JVM system properties; it does not automatically load `.env` files.

Example:

```bash
export RAG_API_KEY='replace-me'
export RAG_REDIS_PASSWORD='replace-me'
mvn test
```

JVM properties take precedence over environment variables:

```bash
java -Drag.api.key='replace-me' -Drag.redis.password='replace-me' -jar app.jar
```

## Reporting a vulnerability

Do not open a public issue containing credentials, exploit details, private endpoints, or user data. Contact the repository owner privately and include the affected component, impact, reproduction steps, and suggested remediation.
