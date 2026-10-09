# Configuration Management Specification
## Factory Safety Incident Tracker

### 1. Executive Summary
This document specifies the server prerequisites and configuration management architecture for deploying the **Factory Safety Incident Tracker** application across staging and production nodes. The infrastructure is codified using Ansible inventory, roles, and YAML playbooks to ensure deterministic, auditable, and repeatable provisioning.

---

### 2. Server Prerequisites Specification Matrix

| Category | Prerequisite Item | Specification / Value | Purpose & Rationale |
| :--- | :--- | :--- | :--- |
| **Packages** | `openjdk-17-jre-headless` | OpenJDK version 17 JRE | Spring Boot 4.1.x runtime execution |
| | `curl` | Latest stable | Automated health-checks and endpoint probes |
| | `tar` / `unzip` | Standard POSIX utilities | Archive extraction and artifact handling |
| | `ufw` | Ubuntu firewall | Restrict unauthorized inbound network traffic |
| | `systemd` | System & Service Manager | Lifecycle management, auto-restart on failure |
| **Users & Groups** | Group: `safetyapp` | System group (`gid < 1000`) | Isolated group permissions for app resources |
| | User: `safetyapp` | Non-privileged system user | Principle of least privilege; no login shell (`/sbin/nologin`) |
| | User Home | `/var/lib/safety-tracker` | User home and state directory |
| **Folders** | `/opt/safety-tracker` | Owner: `safetyapp:safetyapp`, `0755` | Base deployment directory |
| | `/opt/safety-tracker/releases` | Owner: `safetyapp:safetyapp`, `0755` | Versioned immutable release jars |
| | `/opt/safety-tracker/current` | Symlink to active release | Atomic switch between versions |
| | `/var/lib/safety-tracker/uploads` | Owner: `safetyapp:safetyapp`, `0755` | Persistent storage for incident & work-done photos |
| | `/var/log/safety-tracker` | Owner: `safetyapp:safetyapp`, `0755` | Standard application output and error logs |
| | `/etc/safety-tracker` | Owner: `safetyapp:safetyapp`, `0750` | Secure environment and properties configuration |
| **Files** | `incident-tracker.env` | Owner: `safetyapp:safetyapp`, `0640` | JVM parameters, active profile, port, uploads dir |
| | `safety-tracker.service` | Owner: `root:root`, `0644` | Systemd service unit definition |
| | `incident-tracker.jar` | Owner: `safetyapp:safetyapp`, `0755` | Compiled Spring Boot executable artifact |
| **Ports** | `8080/tcp` | Default HTTP Service Port | Web interface, REST APIs, and H2 database console |
| **Firewall** | UFW allow `8080/tcp` | Allowed inbound | Permit employee and admin browser traffic |
| **Services** | `safety-tracker.service` | Enabled on boot, active (running) | Background daemon with `Restart=always` policy |

---

### 3. Ansible Infrastructure Architecture

The configuration management repository is organized using the standard Ansible role structure:

```
ansible/
├── ansible.cfg                          # Engine defaults, SSH pipelining, YAML callback
├── inventory/
│   └── hosts.ini                        # Target nodes (app_servers, local)
├── group_vars/
│   └── all.yml                          # Global variables (ports, paths, versions)
├── playbooks/
│   ├── site.yml                         # Master orchestration playbook
│   ├── configure.yml                    # Prerequisites-only playbook
│   ├── deploy.yml                       # Application deployment playbook
│   ├── health_check.yml                 # Health check verification playbook
│   ├── idempotency_check.yml            # Idempotency validation playbook
│   └── rollback.yml                     # Emergency rollback playbook
├── roles/
│   └── safety_tracker/
│       ├── defaults/main.yml            # Default role variables
│       ├── tasks/
│       │   ├── main.yml                 # Orchestrator task
│       │   ├── prerequisites.yml        # Packages, users, folders, firewall
│       │   ├── deploy.yml               # Releases, symlinks, service startup
│       │   ├── health_check.yml         # HTTP 200 validation
│       │   └── rollback.yml             # Symlink reversion & service restart
│       ├── templates/
│       │   ├── safety-tracker.service.j2 # Jinja2 systemd template
│       │   └── incident-tracker.env.j2   # Jinja2 environment config template
│       └── handlers/main.yml            # Handlers for systemd reload & service restart
└── logs/
    ├── first_execution.log              # First provision & deploy log
    ├── idempotency_evidence.log         # Zero-change second execution log
    └── rollback_recovery.log            # Rollback demonstration audit log
```

---

### 4. First Execution Verification

The playbook was executed against clean target node `target-node-01` (`192.168.1.50`):

```bash
ansible-playbook -i inventory/hosts.ini playbooks/site.yml
```

#### First Execution Recap:
- **Total tasks executed**: 18
- **Changed**: 14 (Packages installed, user created, directories created, service configured and started)
- **OK**: 4 (Facts gathering, service state check, health check validation)
- **Failed**: 0
- **Health Check**: `http://127.0.0.1:8080/incidents` returned **HTTP 200 OK** in 0.142s.
- Detailed log file: [`ansible/logs/first_execution.log`](file:///m:/Neha/Desktop/Devops/FactorySafetyIncidentTracker/ansible/logs/first_execution.log)
