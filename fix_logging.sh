#!/bin/bash

# Fix logging calls in Java files
find src -name "*.java" -exec sed -i '' \
  -e 's/log\.info("\([^"]*\){}", \([^)]*\));/log.info("\1" + \2);/g' \
  -e 's/log\.error("\([^"]*\){}", \([^)]*\));/log.severe("\1" + \2);/g' \
  -e 's/log\.warn("\([^"]*\){}", \([^)]*\));/log.warning("\1" + \2);/g' \
  -e 's/log\.error("\([^"]*\){}", \([^)]*\), \([^)]*\));/log.severe("\1" + \2 + " " + \3);/g' \
  -e 's/log\.warn("\([^"]*\){}", \([^)]*\), \([^)]*\));/log.warning("\1" + \2 + " " + \3);/g' \
  {} \;
