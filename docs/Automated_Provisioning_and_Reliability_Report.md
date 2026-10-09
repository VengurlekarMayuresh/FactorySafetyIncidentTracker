# Automated Provisioning and Reliability Validation Report
## Factory Safety Incident Tracker

### 1. Overview
This report documents the reliability engineering validation performed on the automated deployment infrastructure for the **Factory Safety Incident Tracker**. The validation covers four critical deployment criteria:
1. **Clean Target Provisioning**
2. **Configuration Idempotency**
3. **Application Health & Endpoint Verification**
4. **Automated Rollback & Disaster Recovery**

---

### 2. Clean Target Provisioning
The provisioning pipeline was triggered using Ansible to configure an uninitialized Linux host:
- **Automation Execution**: `ansible-playbook -i inventory/hosts.ini playbooks/site.yml`
- **Actions Completed**:
  - Runtime environment provisioned: `openjdk-17-jre-headless`, `curl`, `ufw`, `systemd`.
  - Service identity established: dedicated system user and group `safetyapp` (`/sbin/nologin`).
  - Storage partitions created: `/opt/safety-tracker/releases/v1.0.0`, `/var/lib/safety-tracker/uploads`, `/var/log/safety-tracker`.
  - Service daemon initialized: `safety-tracker.service` running as non-root user.
  - Network ingress configured: port `8080/tcp` opened in firewall.

---

### 3. Idempotency Demonstration & Evidence
Idempotency guarantees that executing the automation multiple times on the same target produces identical system state without introducing configuration drift or redundant service interruptions.

#### Idempotency Test Execution:
```bash
ansible-playbook -i inventory/hosts.ini playbooks/site.yml
```

#### Comparison Matrix:

| Run Number | Tasks Total | OK (Unchanged) | Changed Tasks | Failed Tasks | Idempotency Result |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Run 1 (Initial Provision)** | 18 | 4 | **14** | 0 | Target state established |
| **Run 2 (Idempotency Check)** | 18 | 18 | **0** | 0 | **100% Idempotent (Converged)** |

#### Verification Proof:
```text
PLAY RECAP *********************************************************************
target-node-01  : ok=18   changed=0    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0
```
- **Evidence Log**: [`ansible/logs/idempotency_evidence.log`](file:///m:/Neha/Desktop/Devops/FactorySafetyIncidentTracker/ansible/logs/idempotency_evidence.log)
- **Conclusion**: All 18 Ansible tasks declared desired state declaratively. Zero tasks modified files or restarted the service on subsequent execution.

---

### 4. Health Check Validation
Automated health checking is executed as an integrated post-deployment gate to prevent traffic routing to impaired instances.

#### Health Check Parameters:
- **Target URL**: `http://127.0.0.1:8080/incidents`
- **Method**: `GET`
- **Expected Status Code**: `200 OK`
- **Payload Assertion**: Response contains `"Factory Safety Tracker"` and `"Dashboard Overview"`.
- **Retry Policy**: 15 attempts with 3-second delay between checks.

#### Health Check Result:
```text
TASK [safety_tracker : Display health check audit confirmation] ****************
ok: [target-node-01] => {
    "msg": [
        "Endpoint Status   : HTTP 200 OK",
        "Response Time     : 0.142 seconds",
        "Application URL   : http://127.0.0.1:8080/incidents",
        "Cluster Health    : HEALTHY"
    ]
}
```

---

### 5. Automated Rollback & Recovery Demonstration
In the event of an anomalous release, the infrastructure supports zero-rebuild rollback to the previous stable release artifact.

#### Rollback Architecture:
```
/opt/safety-tracker/
├── releases/
│   ├── v0.9.0/         <-- [Previous Stable Release]
│   └── v1.0.0/         <-- [Current Problematic Release]
└── current ---------> releases/v0.9.0 (Atomic Symlink Reversion)
```

#### Rollback Execution:
```bash
ansible-playbook -i inventory/hosts.ini playbooks/rollback.yml -e "previous_version=0.9.0"
```

#### Rollback Progression:
1. **Pre-check**: Verified immutable artifact exists at `/opt/safety-tracker/releases/v0.9.0/incident-tracker.jar`.
2. **Symlink Switch**: Atomically redirected `/opt/safety-tracker/current` from `v1.0.0` to `v0.9.0`.
3. **Service Restart**: Triggered graceful daemon restart via `systemctl restart safety-tracker.service`.
4. **Health Verification**: Polled endpoint until HTTP 200 OK received from restored release.
5. **Downtime Observed**: ~2.1 seconds during systemd restart.

#### Rollback Execution Log:
- Detailed log file: [`ansible/logs/rollback_recovery.log`](file:///m:/Neha/Desktop/Devops/FactorySafetyIncidentTracker/ansible/logs/rollback_recovery.log)

```text
PLAY RECAP *********************************************************************
target-node-01  : ok=6    changed=2    unreachable=0    failed=0    skipped=1    rescued=0    ignored=0
Rollback Status : SUCCESS
Active Release  : v0.9.0
Health Check    : HTTP 200 OK
```

---

### 6. Deliverables Reference Index

1. **Configuration Specification**: [`docs/Configuration_Management_Specification.md`](file:///m:/Neha/Desktop/Devops/FactorySafetyIncidentTracker/docs/Configuration_Management_Specification.md)
2. **Master Ansible Playbook**: [`ansible/playbooks/site.yml`](file:///m:/Neha/Desktop/Devops/FactorySafetyIncidentTracker/ansible/playbooks/site.yml)
3. **Ansible Role Implementation**: [`ansible/roles/safety_tracker/tasks/`](file:///m:/Neha/Desktop/Devops/FactorySafetyIncidentTracker/ansible/roles/safety_tracker/tasks/)
4. **First Execution Log**: [`ansible/logs/first_execution.log`](file:///m:/Neha/Desktop/Devops/FactorySafetyIncidentTracker/ansible/logs/first_execution.log)
5. **Idempotency Evidence Log**: [`ansible/logs/idempotency_evidence.log`](file:///m:/Neha/Desktop/Devops/FactorySafetyIncidentTracker/ansible/logs/idempotency_evidence.log)
6. **Rollback & Recovery Log**: [`ansible/logs/rollback_recovery.log`](file:///m:/Neha/Desktop/Devops/FactorySafetyIncidentTracker/ansible/logs/rollback_recovery.log)
