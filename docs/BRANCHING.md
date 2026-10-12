# Branch policy

| Branch | Purpose | Rules |
|--------|---------|-------|
| `main` | Always release-ready. Tags such as `v1.0.0` are created here. | Changes arrive only by pull request from `develop` (or `hotfix/*`). |
| `develop` | Integration branch for finished features. Jenkins builds this branch. | Changes arrive only by pull request from `feature/*` or `bugfix/*`. |
| `feature/<short-name>` | One user story, for example `feature/create-certificate`. | Branch from `develop`, delete after merge. |
| `bugfix/<short-name>` | Fix for a defect found on `develop`. | Branch from `develop`. |
| `hotfix/<short-name>` | Urgent fix for `main`. | Branch from `main`, merge to `main` and `develop`. |
| `docs/<short-name>` | Documentation-only changes. | Branch from `develop`. |

## Commit messages
Start with a verb in the imperative and say what and why, for example `Add search by course name` or `Fix case-sensitive name search`.
One logical change per commit.

## Pull requests
* Use the pull request template and link the issue (`Closes #12`).
* At least one review comment from a team member (or a self-review comment for solo work) before merging.
* Merge with a merge commit so the feature branch stays visible in `git log --graph`.
