#!/bin/sh

echo "Running static analysis..."

# # Format code using KtLint, then run Detekt and KtLint static analysis before committing
#./gradlew detekt ktlintCheck --daemon
./gradlew detekt --daemon

#./gradlew app:ktlintFormat : we also can format the while committing but it will again change the code then we have to add new commit

status=$?

if [ "$status" = 0 ]; then
  echo "Static analysis found no issues. Proceeding with push."
  exit 0
else
  echo 1>&2 "Static analysis found issues you need to fix before pushing."
  echo 1>&2 "Use './gradlew app:ktlintFormat' to solve KtLint formatting issues"
  exit 1
fi
