# Task 1 - Problem definition and scope

## Problem statement
Training institutes and departments issue completion certificates to hundreds of participants. Today a clerk types each certificate into a word-processor template or spreadsheet, a supervisor checks it by email, and someone signs and sends it. The same data is typed more than once, there is no single place to see which certificates are waiting, and errors (wrong name spelling, wrong course) are found only after the certificate has gone out.

The **Certificate Generation Portal** replaces this with one web application where records are created once, reviewed by an approver, and issued only after approval. A dashboard shows how many are in each stage.

## Target users
| User | Need |
|------|------|
| Operator (data-entry clerk) | Enter recipients quickly, fix mistakes, send for approval |
| Approver (supervisor / course coordinator) | See what is waiting, approve or reject with confidence |
| Administrator (registrar) | Issue approved certificates, see overall status |

## Existing pain points
1. Same details typed in several places (spreadsheet, template, email).
2. No audit trail: nobody knows who changed or approved a record.
3. Approval happens in email threads and gets lost.
4. Finding an old certificate means searching folders by hand.
5. No overview of how many certificates are pending.

## Stakeholders
| Stakeholder | Interest |
|-------------|----------|
| Participants / certificate recipients | Correct, timely certificates |
| Operators | Fast, simple data entry |
| Approvers | Clear queue, fewer errors |
| Administrators / registrar | Control over what is issued, reporting |
| Course instructors | Confidence that completions are recorded |
| IT / DevOps team | Easy build, test, deployment and recovery |
| Project guide / evaluator | Evidence of a complete DevOps lifecycle |

## Objectives
1. Deliver a working MVP that covers create, view, update and search of certificate records, a role-based status workflow and a summary dashboard.
2. Automate the path from commit to running system with Jenkins (build, test, package, deploy, container).
3. Make the environment reproducible with configuration management (Ansible) and recoverable with rollback.

## Measurable success criteria
| # | Criterion | Target |
|---|-----------|--------|
| 1 | Operator can create and submit a certificate | under 1 minute, 5 form fields |
| 2 | Search returns matching records | under 1 second for 1,000 records |
| 3 | Automated tests | all unit tests and 5 Selenium journeys pass on every build |
| 4 | Commit to running container | fully automatic, under 10 minutes |
| 5 | Failed tests | never reach deployment (pipeline stops) |
| 6 | Recovery | rollback to the previous release in under 5 minutes |
| 7 | Rerunning configuration automation | zero changes on the second run (idempotent) |

## Constraints
* Open-source tools only: Java, Maven, Tomcat, H2, Jenkins, Selenium, Docker, Ansible, Git/GitHub.
* Single developer, short time-box (course project).
* Must run on a laptop or a single small virtual machine.
* No external services (email, payment, SSO) in the MVP.
* Demo accounts only; production-grade identity management is out of scope.

## Approved MVP scope (frozen)
**In scope (15 backlog tasks, see [02-agile-plan.md](02-agile-plan.md)):** login with three roles, create / view / update / search certificates, status workflow (Draft, Submitted, Approved, Rejected, Issued), dashboard, health endpoint, unit and Selenium tests, Jenkins pipeline, Docker image, Ansible provisioning with rollback.

**Out of scope:** PDF generation and e-mailing of certificates, bulk import, user management screens, password reset, multi-language support, reporting beyond the dashboard.
