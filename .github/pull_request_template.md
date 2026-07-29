## What changed

<!-- Describe the focused outcome of this PR. -->

## Why

<!-- Explain the problem, root cause, or developer need. -->

## Compatibility

- [ ] Java 8 compatibility preserved
- [ ] No mandatory application framework added to the core
- [ ] No credentials, tokens, passwords, or private endpoints included

## Validation

```bash
bash scripts/check-no-hardcoded-secrets.sh
mvn --batch-mode --no-transfer-progress test
bash scripts/run-no-api-demo.sh
```

<!-- Add any extra checks, sample output, or screenshots. -->
