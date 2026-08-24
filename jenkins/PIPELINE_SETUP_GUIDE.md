# Jenkins Pipeline Setup Guide — Week 8 (Pipeline as Code)

## Overview
In Week 8, we convert the Freestyle Jenkins job to a **Pipeline job** that reads the `Jenkinsfile` directly from your repository root. This is **Pipeline as Code** — your CI/CD configuration lives in version control alongside your application code.

---

## Step 1: Convert to Pipeline Job in Jenkins

1. Open Jenkins at `http://localhost:8082/jenkins`.
2. Click your existing job `factory-safety-incident-tracker` → **Configure** (or create a **New Item → Pipeline**).
3. Scroll down to the **Pipeline** section.
4. Change **Definition** to: `Pipeline script from SCM`.
5. Set **SCM** to: `Git`
6. Set **Repository URL** to your local path:
   - On Windows with local repo: `file:///M:/Neha/Desktop/Devops/FactorySafetyIncidentTracker`
   - Or your GitHub URL: `https://github.com/<YOU>/factory-safety-incident-tracker.git`
7. Set **Branch**: `*/develop`
8. Set **Script Path**: `Jenkinsfile` (this is the root-level file).
9. Click **Save**.

---

## Step 2: Build with Parameters

1. On the job page, click **Build with Parameters**.
2. Select `ENV = dev` (or `staging`).
3. Optionally set `APP_PORT = 8080`.
4. Click **Build**.

---

## Step 3: Observe the Pipeline Stages

The pipeline will execute these stages:
| Stage | What Happens |
|---|---|
| **Checkout** | Logs environment info, prepares source |
| **Build** | `./mvnw.cmd clean compile` |
| **Test** | `./mvnw.cmd test` + publishes JUnit XML results |
| **Package** | `./mvnw.cmd package` → creates `incident-tracker-0.0.1-SNAPSHOT.jar` |
| **Deploy** | Copies JAR to `C:\deploy\<ENV>\`, kills old process, starts new one on configured port |

---

## Step 4: Manual Deploy (Without Jenkins)

You can also deploy directly using the deploy script:
```bat
cd M:\Neha\Desktop\Devops\FactorySafetyIncidentTracker

REM Deploy to dev on port 8080:
deploy.bat dev 8080

REM Deploy to staging on port 8081:
deploy.bat staging 8081
```

---

## Parameterization Evidence (for Viva)

The `Jenkinsfile` has two parameters:
- **`ENV`**: Chooses the Spring profile (`dev` or `staging`), which switches between:
  - `application-dev.properties` (H2 in-memory, port 8080, debug logging)
  - `application-staging.properties` (H2 file-based, port 8081, info logging)
- **`APP_PORT`**: Overrides the HTTP port the app listens on at runtime.

This demonstrates **environment-specific deployments** from a single Jenkinsfile.
