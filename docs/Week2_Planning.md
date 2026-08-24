# Agile Planning and DevOps Workflow

## Product Backlog (Prioritized)
- **MUST** (MVP)
  1. As a Worker, I want to log an incident so that it is recorded in the system.
  2. As a Manager, I want to view a dashboard so I can see the overall safety status.
  3. As a Manager, I want to filter incidents by status, severity, and date so I can find specific reports.
  4. As a Manager, I want to see summary indicators for open and high-severity incidents so I know what needs immediate attention.
  5. As a Safety Officer, I want to drill down into an incident's details to review and update its status.
  6. As a Safety Officer, I want to view an alert list of unresolved high-severity incidents so I can prioritize them.

## Sprint Plan (15 Weeks)
- **Weeks 1-4**: Planning, Architecture, Local Setup, Git Init.
- **Weeks 5-6**: Feature Development (MVP).
- **Weeks 7-8**: CI/CD (Jenkins).
- **Weeks 9-10**: Automated Testing (Selenium).
- **Weeks 11-12**: Containerization (Docker).
- **Weeks 13-14**: Provisioning (Ansible).
- **Week 15**: Release and Demo.

## Definition of Done (DoD)
- Code is committed and pushed to the repository.
- Code is reviewed (if applicable).
- Unit tests are written and passing.
- Automated builds pass in Jenkins.
- Feature is deployed to the staging environment.
- Feature functionality meets Acceptance Criteria.

## DevOps Workflow
Plan -> Code -> Build (Maven/Jenkins) -> Test (Selenium) -> Release (Docker) -> Deploy (Ansible) -> Operate -> Monitor -> Plan
