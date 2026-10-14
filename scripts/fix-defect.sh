#!/usr/bin/env bash
# Task 10: correct the deliberately introduced search defect.
set -euo pipefail
cd "$(dirname "$0")/.."
FILE=src/main/java/com/certportal/dao/CertificateDao.java
if grep -q "LOWER(recipient_name) LIKE ?" "$FILE"; then
  echo "Defect not present. Nothing to do."; exit 0
fi
sed -i 's/ AND (recipient_name LIKE ?/ AND (LOWER(recipient_name) LIKE ?/' "$FILE"
grep -q "LOWER(recipient_name) LIKE ?" "$FILE" && echo "Fixed. Now: git add -A && git commit -m 'Fix case-insensitive search' && git push" \
  || { echo "Could not restore the line automatically - edit $FILE by hand."; exit 1; }
