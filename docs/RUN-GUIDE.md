# Run guide - all 14 tasks

Everything that can be written for you is in this repository. The steps below are the parts only you can do (your GitHub, Jenkins, Docker Hub and server accounts) and the evidence to capture. Save screenshots and logs in `docs/evidence/` using the file names suggested, so the report is easy to assemble.

> **First, prove the build on your own machine.** The code, tests and pipeline files were written without being able to run Maven, Chrome, Jenkins, Docker or Ansible, so run `mvn clean verify` (Task 9) before anything else. If something fails, fix it or send me the log.

## 0. One-time machine setup (Ubuntu 24.04, WSL2 with systemd, or a VM)

```bash
sudo apt update
sudo apt install -y openjdk-17-jdk maven git curl docker.io ansible tomcat10 tomcat10-admin
sudo usermod -aG docker $USER            # log out and in again afterwards
# Google Chrome (needed by Selenium)
wget -q https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
sudo apt install -y ./google-chrome-stable_current_amd64.deb
# GitHub CLI (optional, used by scripts/create-issues.sh)
sudo apt install -y gh
```

Java 17 and Maven must show up with `java -version` and `mvn -version`.

**Tomcat manager user** (so Jenkins can deploy). Edit `/etc/tomcat10/tomcat-users.xml` and add before `</tomcat-users>`:

```xml
<role rolename="manager-script"/>
<user username="deployer" password="CHOOSE-A-PASSWORD" roles="manager-script"/>
```

Then `sudo systemctl restart tomcat10`. Tomcat listens on port 8080.

**Jenkins** (install from the official Jenkins apt repository, see <https://www.jenkins.io/doc/book/installing/linux/>). Jenkins also wants port 8080, so move it:

```bash
sudo systemctl edit jenkins      # add the two lines below, save, exit
#   [Service]
#   Environment="JENKINS_PORT=8090"
sudo systemctl restart jenkins
sudo usermod -aG docker jenkins && sudo systemctl restart jenkins
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

Open <http://localhost:8090>, finish the setup wizard with the suggested plugins, then make sure these plugins are installed (Manage Jenkins -> Plugins): Git, GitHub, Pipeline, JUnit, Credentials Binding.

---

## Task 1 - Problem definition and scope
**Do:** Read and adapt `docs/01-problem-definition.md` (change names, numbers or constraints to match your situation). 
**Evidence:** The document itself (problem statement, stakeholders, objectives, constraints, MVP scope).

## Task 2 - Agile planning and DevOps workflow
**Do:**
1. Read `docs/02-agile-plan.md` (stories, backlog, sprint plan, Definition of Done, DevOps lifecycle diagram).
2. After the GitHub repository exists (Task 4): `scripts/create-issues.sh`, then on GitHub create a **Project** (Board layout), add the 15 issues and move cards to Done as you finish tasks.
3. Open the Markdown file on GitHub so the Mermaid lifecycle diagram renders, and screenshot it.

**Evidence:** `docs/evidence/task2-board.png`, `task2-lifecycle-diagram.png`.

## Task 3 - Requirements, architecture and technology setup
**Do:** Read `docs/03-requirements-architecture.md` (SRS summary, use-case, architecture, data model, URL list). Prove the local setup:

```bash
mvn clean package
sudo cp target/certificate-portal.war /var/lib/tomcat10/webapps/
# wait ~10 seconds, then open http://localhost:8080/certificate-portal/  (sign in: operator / operator123)
curl http://localhost:8080/certificate-portal/health
```

**Evidence:** screenshots of the diagrams on GitHub, of `mvn -version` / `java -version`, and of the running application (`task3-local-app.png`).

---

## Tasks 4, 5, 6 - Git and GitHub

The repository in this folder already has a Git history prepared as a chain of branches. You push them and use GitHub for the pull requests, review comments and merges, so that evidence is genuinely yours.

| Branch | Contents | Used for |
|--------|----------|----------|
| `main`, `develop` | Skeleton: README, .gitignore, templates, branch policy, Maven file, domain model, workflow and validator code with unit tests, docs | Task 4 |
| `feature/create-view-certificate` | Login, create, view, list, dashboard, database, health check | Task 5 |
| `feature/search-workflow-update` | Edit, search and filter, role-based status workflow | Task 6 |
| `feature/devops-automation` | Selenium tests, Jenkinsfiles, Dockerfile, Ansible, scripts, run guide | Tasks 7-14 |

Each branch is built on the one above it.

### Task 4 - Initialise the repository
1. On GitHub create an empty repository named `certificate-portal` (no README, no .gitignore). 
2. Set your identity if needed and push:

```bash
cd certificate-portal
git config user.name "Your Name" && git config user.email "you@example.com"
git remote add origin https://github.com/<your-user>/certificate-portal.git
git log --oneline --decorate --graph --all       # look at the prepared history
git push -u origin main develop
```
3. On GitHub: Settings -> Branches -> add a rule for `main` (require a pull request before merging). Run `scripts/create-issues.sh` to create the issues.

**Evidence:** repository URL; screenshot of the file list with README; `git log --oneline` output (`task4-git-log.txt`); Issues page; `docs/BRANCHING.md` as the branch policy.

### Task 5 - First feature on a feature branch, pull request, review, merge
```bash
git push -u origin feature/create-view-certificate feature/search-workflow-update feature/devops-automation
git checkout feature/create-view-certificate
git log --oneline                                  # your commits for this feature
git status
```
On GitHub open a **pull request**: base `develop`, compare `feature/create-view-certificate`, fill in the template. Add at least two review comments on specific lines of the diff ("Files changed" tab, click the + beside a line), then **Approve / Comment**, and **Merge pull request** (merge commit). Then locally:

```bash
git checkout develop
git pull origin develop
git log --oneline --graph --decorate -10
```
To show `add`, `commit`, `push` yourself, make one small real change on the feature branch before merging, for example improve a validation message in `Validator.java`: `git add -p`, `git commit -m "Clarify name validation message"`, `git push`.

**Evidence:** PR page (title, description, review comments, "Merged" banner), `git log --graph` output, screenshot of the working feature (create a certificate and view it).

### Task 6 - Complete the MVP, second branch, conflict, tag
1. Open the second pull request: base `develop`, compare `feature/search-workflow-update`. Review and merge (after the first is merged it only shows the new changes). 
2. **Create and resolve a merge conflict** (two people editing the same line):

```bash
git checkout develop && git pull origin develop
git checkout -b bugfix/footer-wording-a
sed -i 's/<span>Version /<span>Release /' src/main/webapp/WEB-INF/jsp/footer.jspf
git commit -am "Footer: say Release instead of Version" && git push -u origin bugfix/footer-wording-a

git checkout develop
git checkout -b bugfix/footer-wording-b
sed -i 's/<span>Version /<span>Build /' src/main/webapp/WEB-INF/jsp/footer.jspf
git commit -am "Footer: say Build instead of Version" && git push -u origin bugfix/footer-wording-b

git checkout develop
git merge bugfix/footer-wording-a                  # merges cleanly
git merge bugfix/footer-wording-b                  # CONFLICT
git status                                         # screenshot this
```
Open `src/main/webapp/WEB-INF/jsp/footer.jspf`, keep one version, delete the `<<<<<<<`, `=======`, `>>>>>>>` lines, then:

```bash
git add src/main/webapp/WEB-INF/jsp/footer.jspf
git commit -m "Resolve footer wording conflict"
git push origin develop
```
3. Verify the MVP: `mvn clean verify`. 
4. Release: open a pull request `develop` -> `main`, merge, then tag:

```bash
git checkout main && git pull origin main
git tag -a v1.0.0 -m "MVP release"
git push origin v1.0.0
git tag -n
```
5. Update the board: move T01-T11 to Done.

**Evidence:** second PR, conflict `git status` screenshot with markers, resolved file and merge commit in `git log --graph`, GitHub Releases/Tags page showing `v1.0.0`, updated board.

---

## Task 7 - Jenkins installation and CI job
**Do:**
1. Make sure `develop` (or `main`) contains everything (all three feature branches merged) and is pushed.
2. Jenkins -> **New Item** -> name `certificate-portal-ci` -> **Freestyle project**.
3. Source Code Management: Git, repository URL, branch `*/develop`. (Public repo: no credentials needed.)
4. Build Triggers: **Poll SCM**, schedule `H/2 * * * *`.
5. Build Steps: **Execute shell**: `mvn -B clean package -DskipTests`.
6. Post-build Actions: **Archive the artifacts**: `target/*.war`.
7. **Build Now**, then push a tiny commit (for example a README edit) and wait two minutes to see the automatic build.

**Evidence:** job configuration page, green build **Console Output**, build history showing a build "Started by an SCM change" (`task7-trigger.png`), the archived WAR on the build page.

## Task 8 - Pipeline as code and server deployment
**Do:**
1. Jenkins -> Manage Jenkins -> Credentials -> Global -> **Add Credentials**: Username with password, ID `tomcat-manager`, username `deployer`, the password you set in `tomcat-users.xml`.
2. New Item `certificate-portal-pipeline` -> **Pipeline** -> Pipeline script from SCM -> Git -> your repo, branch `*/develop`, **Script Path** `jenkins/Jenkinsfile.task8-deploy`.
3. **Build with Parameters**: `DEPLOY_ENV=staging`, `TOMCAT_URL=http://localhost:8080`. Open <http://localhost:8080/certificate-portal/> and check the footer says **Environment: staging**. Run again with `dev` and show the footer change. That is your parameterised environment setting.

**Evidence:** `jenkins/Jenkinsfile.task8-deploy`, stage view of a green run, the deployed URL in a browser showing the environment label, the parameter form.

## Task 9 - Selenium tests, local execution
**Do:** read `docs/04-test-plan.md`, then:

```bash
mvn clean verify                                   # everything, headless Chrome
mvn verify -Dheadless=false                        # watch it run (screen recording is good evidence)
mvn verify -Dit.test=FailureCaptureDemoIT -Dforce.fail=true    # failing on purpose
ls target/screenshots                              # the captured PNG and HTML
```

**Evidence:** the test plan, the console summary (about `Tests run: 18, Failures: 0, Errors: 0, Skipped: 1`; the skipped one is the on-purpose failure demo), files from `target/failsafe-reports`, the screenshot PNG from the deliberate failure.

## Task 10 - Continuous testing in Jenkins
**Do:**
1. Create the job `certificate-portal-tests` like Task 8 but with Script Path `jenkins/Jenkinsfile.task10-tests`. Run it once (green). In the build page, open **Test Result** (JUnit report).
2. Introduce the defect, commit, push, and run the job again:

```bash
scripts/inject-defect.sh
git checkout -b bugfix/search-case-fix develop
git commit -am "Introduce search defect (demo)" && git push -u origin bugfix/search-case-fix
```
(Point the job at that branch, or merge into `develop`.) The **Selenium tests** stage turns red, the **Deploy** stage is skipped, and the report names `SearchIT.searchFindsARecordByPartialLowercaseName`. Screenshots are archived under the build's artifacts.
3. Correct the defect:

```bash
scripts/fix-defect.sh
git commit -am "Fix case-insensitive name search" && git push
```
Run the job again: green, deployed.

**Evidence:** green run, red run (stage view with Deploy skipped), failed test in the JUnit report, archived failure screenshot, the fix commit, green rerun.

## Task 11 - Docker image and container lifecycle
**Do:**
```bash
mkdir -p docs/evidence
scripts/docker-lifecycle.sh 2>&1 | tee docs/evidence/docker-command-log.txt
```
It builds and tags the image, runs the container with a port mapping and a volume, reads logs, stops, starts, restarts and removes it. Take a screenshot of the application at the mapped port (`http://localhost:8082/certificate-portal/`) while the container runs.

**Evidence:** `Dockerfile`, `docker image ls` / `docker image inspect` output, `docker-command-log.txt`, screenshot of the running container (`docker ps`) and the app.

## Task 12 - Jenkins-Docker continuous deployment
**Do:**
1. Create a Docker Hub account and an **access token** (Account Settings -> Security). In Jenkins add Username with password credentials, ID `dockerhub`, username = your Docker Hub user, password = the token. (For a local registry instead: `docker run -d -p 5000:5000 --name registry registry:2` and use `DOCKER_REPO=localhost:5000/certificate-portal`, `REGISTRY_LOGIN=false`.)
2. Create the job `certificate-portal-cd`: Pipeline from SCM, Script Path `Jenkinsfile` (the full pipeline at the repository root).
3. Build with Parameters: `DOCKER_REPO=<your-dockerhub-user>/certificate-portal`, `DEPLOY_ENV=staging`, keep the other defaults. The pipeline builds, tests, deploys to Tomcat, builds image `1.0.<build number>`, pushes it, and starts a fresh container on port 8081.
4. Check <http://localhost:8081/certificate-portal/health> and Docker Hub for the tag.
5. Make a small commit and push; the SCM poll runs the pipeline again, giving image `1.0.<next number>`.

**Evidence:** `Jenkinsfile`, stage view (all green), console log lines for `docker push`, Docker Hub tags page showing two versions, `docker ps` showing the new container, the commit -> container timeline.

## Task 13 - Configuration management script
**Do:** read `docs/05-configuration-spec.md` and `ansible/`. Prepare the node (a machine with systemd; a VM is best, see Task 14). On the machine that will run Ansible:

```bash
cd ansible
ansible --version
ansible portal -m ping                              # uses inventory/hosts.ini (default: this machine)
mvn -f .. clean package -DskipTests                 # the WAR to deploy
ansible-playbook site.yml --syntax-check
ansible-playbook site.yml --check --diff            # dry run
ansible-playbook site.yml 2>&1 | tee ../docs/evidence/ansible-first-run.log
```

**Evidence:** configuration specification table, `ansible/site.yml` and the role, `ansible-first-run.log` showing the changed tasks.

## Task 14 - Provisioning, idempotency, health check, rollback
**Do:** use a **clean** Ubuntu 24.04 machine. Easiest: a VM with Multipass (`sudo snap install multipass`):

```bash
multipass launch 24.04 --name portal-node --cpus 2 --memory 2G --disk 10G
multipass info portal-node | grep IPv4              # note the IP
```
Give Ansible SSH access: copy your public key to the VM (`ssh-copy-id ubuntu@<ip>` or append it with `multipass exec portal-node -- bash -c "echo '<your public key>' >> ~/.ssh/authorized_keys"`), then edit `ansible/inventory/hosts.ini`:

```
[portal]
portal-node ansible_host=<ip> ansible_user=ubuntu
```

1. **Provision and deploy (release 1.0.0)**
   ```bash
   cd ansible && ansible portal -m ping
   ansible-playbook site.yml 2>&1 | tee ../docs/evidence/provision-run1.log
   ```
2. **Idempotency:** run it again; the recap must show `changed=0`:
   ```bash
   ansible-playbook site.yml 2>&1 | tee ../docs/evidence/provision-run2-idempotent.log
   ```
3. **Health check:** `../scripts/health-check.sh http://<ip>:8080/certificate-portal` (JSON with `"status":"UP"`); open the site in a browser and note "Version 1.0.0" in the footer.
4. **Second release:** edit `pom.xml` (`<version>1.0.1</version>`), then
   ```bash
   mvn -f .. clean package -DskipTests
   ansible-playbook site.yml -e app_version=1.0.1 2>&1 | tee ../docs/evidence/release-1.0.1.log
   ```
   The footer now says Version 1.0.1.
5. **Rollback:**
   ```bash
   ansible-playbook rollback.yml 2>&1 | tee ../docs/evidence/rollback.log
   ../scripts/health-check.sh http://<ip>:8080/certificate-portal
   ssh ubuntu@<ip> 'cat /opt/certportal/current_version; ls /opt/certportal/releases'
   ```
   The footer says Version 1.0.0 again and the health check still passes.

If you have no VM, you can run against your own Ubuntu machine with the default `localhost ansible_connection=local` inventory line, but "clean target" evidence is stronger on a fresh VM (delete it afterwards with `multipass delete portal-node --purge`).

**Evidence:** provisioning logs, the `changed=0` recap, health-check output, the two footer screenshots (1.0.1 then rolled back to 1.0.0), `current_version` and release listing.

---

## Final checklist
| Task | Evidence file(s) in `docs/evidence/` |
|------|---------------------------------------|
| 1 | problem definition document |
| 2 | board and lifecycle screenshots |
| 3 | local setup screenshots |
| 4 | repo URL, git log, issues |
| 5 | PR with review comments, merge evidence |
| 6 | second PR, conflict screenshots, tag v1.0.0 |
| 7 | Jenkins job, build log, trigger, archived WAR |
| 8 | Jenkinsfile, pipeline run, deployed URL |
| 9 | test plan, local report, failure screenshot |
| 10 | red and green runs, defect fix commit |
| 11 | Dockerfile, command log, container screenshots |
| 12 | registry tags, pipeline run, running container |
| 13 | specification, playbook, first execution log |
| 14 | idempotency, health check, rollback proof |
