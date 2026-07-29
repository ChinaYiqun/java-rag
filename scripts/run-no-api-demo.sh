#!/usr/bin/env bash
set -euo pipefail

mvn --quiet --no-transfer-progress -DskipTests compile \
  org.codehaus.mojo:exec-maven-plugin:3.5.0:java \
  -Dexec.mainClass=org.demo.NoApiKeyRagDemo
