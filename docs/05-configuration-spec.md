# Task 13 - Configuration specification

Target node: Ubuntu 24.04 (or Debian 12+) with systemd. Managed by the Ansible role `ansible/roles/certportal`.

| Category | Requirement | Value | Ansible task |
|----------|-------------|-------|--------------|
| Packages | Java runtime | `openjdk-17-jdk-headless` | `provision.yml` - apt |
| Packages | Servlet container | `tomcat10` | `provision.yml` - apt |
| Packages | Health-check client | `curl` | `provision.yml` - apt |
| Users | Release owner | system user `certportal`, no login shell | `provision.yml` - user |
| Users | Tomcat runtime | `tomcat` (created by the package) | - |
| Folders | Release archive | `/opt/certportal/releases/<version>/` owner `certportal`, 0755 | `provision.yml` - file |
| Folders | Database | `/var/lib/certportal` owner `tomcat`, 0750 | `provision.yml` - file |
| Files | systemd drop-in | `/etc/systemd/system/tomcat10.service.d/certportal.conf` (APP_ENV, DATA_DIR, ReadWritePaths) | `provision.yml` - template |
| Files | Application WAR | `/var/lib/tomcat10/webapps/certificate-portal.war` | `deploy.yml` - copy |
| Files | Release markers | `/opt/certportal/current_version`, `/opt/certportal/previous_version` | `deploy.yml` - copy |
| Ports | HTTP | TCP 8080 (Tomcat) | `provision.yml` - wait_for |
| Services | Tomcat | `tomcat10` enabled and running | `provision.yml` - service |
| Verification | Health | `GET http://localhost:8080/certificate-portal/health` returns 200 and `"status":"UP"` | `health.yml` - uri |

## Variables
All values above are variables in `ansible/group_vars/all.yml` (package names, ports, folders, `app_version`, `app_env`).

## Playbooks
| File | Purpose |
|------|---------|
| `ansible/site.yml` | Provision the node and deploy the WAR (idempotent) |
| `ansible/rollback.yml` | Restore the previous release (or `-e rollback_to=<version>`) |
