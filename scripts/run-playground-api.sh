#!/usr/bin/env bash
set -euo pipefail

mvn --quiet -DskipTests compile \
  org.codehaus.mojo:exec-maven-plugin:3.6.3:java \
  -Dexec.mainClass=org.playground.PlaygroundServer
