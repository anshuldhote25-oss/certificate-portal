# Certificate Generation Portal

A small Servlet/JSP web application used as the subject of a DevOps course project (Jenkins, Maven, Git, Selenium, Docker, Ansible).

Staff create certificate records, send them through a role-based approval workflow, and issue them. A dashboard summarises progress.

| | |
|---|---|
| Language / build | Java 17, Maven (WAR) |
| Web | Jakarta Servlet 6 and JSP/JSTL, deployed on Tomcat 10.1 |
| Data | H2 embedded database (file stored in `DATA_DIR`) |
| Tests | JUnit 5 unit tests, Selenium WebDriver (Chrome) browser tests |
| CI/CD | Jenkins pipeline (`Jenkinsfile`), Docker image, Ansible playbook |

## Features (MVP)
* Create, view, update and search certificate records
* Role-based status workflow: **Draft → Submitted → Approved → Issued** (or Rejected, then edit and resubmit)
* Summary dashboard with counts per status
* `/health` endpoint for monitoring and automation

### Roles and demo accounts
| User | Password | Can do |
|------|----------|--------|
| `operator` | `operator123` | Create, edit, submit |
| `approver` | `approver123` | Approve or reject submitted records |
| `admin` | `admin123` | Everything, and issue approved certificates |

These accounts are for coursework only. Change them before any real use.

## Run locally
Prerequisites: JDK 17+, Maven 3.9+, Chrome (only for the Selenium tests).

```bash
mvn clean package            # compile, unit tests, build target/certificate-portal.war
mvn verify                   # also runs the Selenium tests against the packaged WAR
```

Run the WAR on a local Tomcat 10.1: copy `target/certificate-portal.war` into Tomcat's `webapps` folder, start Tomcat, then open
<http://localhost:8080/certificate-portal/>.

Quick option without installing Tomcat: `docker build -t certificate-portal . && docker run -p 8080:8080 certificate-portal`
(build the WAR first), then open the same URL.

### Configuration
| Setting | JVM property | Environment variable | Default |
|---------|--------------|----------------------|---------|
| Environment label shown in the footer | `-Dapp.env` | `APP_ENV` | value passed at build time (`mvn package -Dapp.env=staging`), else `dev` |
| Database folder | `-Ddata.dir` | `DATA_DIR` | `./data` |
| Full JDBC URL (optional) | `-Ddb.url` | `DB_URL` | H2 file in the data folder |

### Test options
```bash
mvn test                                  # unit tests only
mvn verify -Dheadless=false               # watch Chrome run the Selenium tests
mvn verify -DbaseUrl=http://localhost:8080/certificate-portal   # test an already deployed server
mvn verify -Dit.test=FailureCaptureDemoIT -Dforce.fail=true      # demonstrate failure screenshots (target/screenshots)
```

## Repository layout
```
src/main/java      application code (model, dao, service, web)
src/main/webapp    JSP pages, CSS, web.xml
src/test/java      unit tests and Selenium tests (*IT.java)
Jenkinsfile        full CI/CD pipeline
jenkins/           staged pipelines used for individual course tasks
Dockerfile         Tomcat-based image
ansible/           inventory, playbooks (site.yml, rollback.yml) and role
docs/              project documents and the task-by-task run guide
scripts/           helper scripts (health check, docker lifecycle, defect demo)
```

## Workflow
See [docs/BRANCHING.md](docs/BRANCHING.md) for branch names and pull request rules and
[docs/RUN-GUIDE.md](docs/RUN-GUIDE.md) for the step-by-step instructions for every course task.

