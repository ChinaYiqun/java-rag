# Contributing to Java-RAG

Thanks for helping make a transparent, framework-free Java RAG toolkit.

## Start in five minutes

Requirements:

- JDK 8 or newer
- Maven 3.6+

```bash
git clone https://github.com/ChinaYiqun/java-rag.git
cd java-rag
mvn --no-transfer-progress test
bash scripts/run-no-api-demo.sh
```

The no-API-key demo must work without network model calls, databases, or credentials.

## Good first contributions

Small, reviewable pull requests are preferred. Useful starter areas include:

- add tests for an existing splitter or parser;
- improve error messages and null handling;
- add an offline fixture for an infrastructure integration;
- improve English or Chinese documentation;
- add source metadata to retrieved passages;
- add a provider implementation behind an existing interface;
- remove duplicated dependencies or dead code.

## Development rules

1. Do not commit API keys, passwords, tokens, private endpoints, `.env` files, or generated credentials.
2. Keep Java 8 compatibility unless a version change is discussed first.
3. Avoid introducing a mandatory application framework into the core pipeline.
4. Add deterministic tests for behavior changes. Tests should not require paid APIs.
5. Keep each pull request focused on one clear outcome.
6. Preserve grounded generation: retrieved passages must be traceable into the final prompt or result.

## Run checks

```bash
bash scripts/check-no-hardcoded-secrets.sh
mvn --batch-mode --no-transfer-progress test
bash scripts/run-no-api-demo.sh
```

## Pull requests

A useful PR description explains:

- what changed;
- why it matters;
- compatibility impact;
- how it was tested;
- screenshots or sample output when the behavior is visible.

## Reporting security issues

Do not open public issues containing active credentials or exploitable private infrastructure details. Follow [SECURITY.md](SECURITY.md).
