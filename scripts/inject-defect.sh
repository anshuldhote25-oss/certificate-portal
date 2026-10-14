#!/usr/bin/env bash
# Task 10: deliberately introduce a defect so the Selenium search test fails.
# The search query stops being case-insensitive, so searching "searchable abc" no longer finds "Searchable abc".
set -euo pipefail
cd "$(dirname "$0")/.."
FILE=src/main/java/com/certportal/dao/CertificateDao.java
if ! grep -q "LOWER(recipient_name) LIKE ?" "$FILE"; then
  echo "Defect already present (or the line changed). Nothing to do."; exit 0
fi
sed -i 's/LOWER(recipient_name) LIKE ?/recipient_name LIKE ?/' "$FILE"
echo "Defect injected into $FILE. Now: git add -A && git commit -m 'Introduce search defect (demo)' && git push"
