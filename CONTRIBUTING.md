# Contributing Guidelines

## Branch Naming Policy
We follow a strict branching model to keep our Git history organized:
- `main`: Production-ready, stable releases (e.g., tags `vX.Y.Z`).
- `develop`: Integration branch where features are merged.
- `feature/<feature-name>`: Active feature development (e.g., `feature/incident-entry`).
- `bugfix/<issue-name>`: Fixing issues found on develop/main.
- `release/<version>`: Preparing for a release.

## Commit Message Policy
Use clear and concise conventional commit messages:
- `feat: <description>` for new features
- `fix: <description>` for bug fixes
- `docs: <description>` for documentation changes
- `chore: <description>` for maintenance or configuration updates
