#!/usr/bin/env bash
# Creates the 15 backlog issues on GitHub (Task 2/4). Needs the GitHub CLI:  gh auth login
# Run from the repository folder after the GitHub repository exists:  scripts/create-issues.sh
set -uo pipefail

for l in "enhancement:a2eeef" "devops:5319e7" "bug:d73a4a"; do
  gh label create "${l%%:*}" --color "${l##*:}" 2>/dev/null || true
done

issue() { gh issue create --title "$1" --label "$3" --body "$2"; }

issue "T01 Repository, Maven skeleton, README, branch policy" "Sprint 1. Create the repo, README, .gitignore, templates and branch rules." devops
issue "T02 Database schema, demo users and sample data" "Sprint 1. H2 schema, hashed demo users, sample certificates. Story US-1, US-2." enhancement
issue "T03 Login, session, CSRF token, role model" "Sprint 1. Story US-1. Acceptance: valid login opens dashboard, wrong password shows error, anonymous users are redirected." enhancement
issue "T04 Create certificate with validation" "Sprint 1. Story US-2. Acceptance: validated form, saved as Draft with generated number." enhancement
issue "T05 View and list certificates" "Sprint 1. Story US-3." enhancement
issue "T06 Dashboard with status counts" "Sprint 1. Story US-8. Acceptance: total equals the sum of status counts." enhancement
issue "T07 Update (edit) certificate" "Sprint 2. Story US-4. Only Draft or Rejected records are editable." enhancement
issue "T08 Search and status filter" "Sprint 2. Story US-5. Case-insensitive partial match on name, number, course." enhancement
issue "T09 Role-based status workflow" "Sprint 2. Stories US-6, US-7. Submit, approve, reject, issue with role checks." enhancement
issue "T10 Health endpoint and environment configuration" "Sprint 2. Story US-9. /health returns 200 and JSON." devops
issue "T11 Unit tests (JUnit)" "Sprint 2. Workflow, validator and DAO tests." devops
issue "T12 Selenium tests (5 journeys)" "Sprint 3. Login, CRUD, search, workflow, dashboard with failure screenshots." devops
issue "T13 Jenkins pipeline: build, test, package, deploy" "Sprint 3. Story US-10. Jenkinsfile with parameterised environment." devops
issue "T14 Docker image and Jenkins-Docker delivery" "Sprint 3. Story US-11. Versioned image pushed to a registry and run as a container." devops
issue "T15 Ansible provisioning, idempotency, rollback" "Sprint 3. Story US-12. Playbook, second run without changes, rollback to previous release." devops
echo "Done. Add the issues to a GitHub Project board (Board layout) and move cards as you finish them."
