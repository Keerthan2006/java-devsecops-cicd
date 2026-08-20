# CI/CD Pipeline

This workflow (`java-ci`) implements a DevSecOps pipeline for the Java application. It runs automatically on pushes and pull requests to `main` (excluding changes under `java-app/` and `kubernetes/`, since those are updated by the pipeline itself), and can also be triggered manually via `workflow_dispatch`.

The pipeline is organized into the following stages, run in sequence:

## 1. Secrets Scanning (`gitleaks`)

Scans the repository for accidentally committed secrets (API keys, credentials, tokens, etc.) using **Gitleaks**. This runs first as a security gate before any build activity.

## 2. Lint (`lint`)

Runs **Super-Linter** against the Java source code (`src/`) to enforce code style and formatting standards, including Google Java Format validation. Results are reported as pull request comments and commit status checks.

## 3. Build, Test & Analyze (`build-test-analyse`)

Depends on: `lint`, `gitleaks`

- Sets up JDK 17 (Temurin) with Maven dependency caching.
- Builds the application with `mvn clean package`.
- Runs the test suite with `mvn test`.
- Performs static code analysis via **SonarQube**, using a quality gate to fail the build if code quality thresholds aren't met.

## 4. Docker Build & Push (`docker`)

Depends on: `build-test-analyse`

- Logs in to Docker Hub.
- Builds a Docker image tagged with the commit SHA.
- Scans the image for **CRITICAL** and **HIGH** severity vulnerabilities using **Trivy**, failing the pipeline if any unfixed vulnerabilities are found.
- Pushes the image to Docker Hub once it passes the scan.

## 5. Update Helm Tag (`update-tag`)

Depends on: `docker`

Updates the image tag in `java-app/values.yaml` to the new commit SHA and commits/pushes the change back to the repository. This is what triggers ArgoCD to sync and deploy the new image (GitOps pattern), since ArgoCD watches this file.

## 6. Dynamic Application Security Testing (`dast`)

Depends on: `update-tag`

- Polls the deployed application's `/health` endpoint until it responds (up to 5 minutes), confirming the new deployment is live.
- Runs an **OWASP ZAP Baseline Scan** against the running application to catch runtime security issues (e.g. missing headers, common web vulnerabilities).
- Uploads the ZAP scan report as a workflow artifact for review.

---

## Pipeline Flow

```
gitleaks ─┐
          ├─► build-test-analyse ─► docker ─► update-tag ─► dast.
lint ─────┘
```

## Security Practices Used

| Tool | Purpose | Stage |
|---|---|---|
| Gitleaks | Secret detection | Pre-build |
| Super-Linter | Code style/quality | Pre-build |
| SonarQube | Static code analysis (SAST) | Build |
| Trivy | Container image vulnerability scanning | Post-build |
| OWASP ZAP | Dynamic application security testing (DAST) | Post-deploy |

## Required Secrets & Variables

| Name | Type | Purpose |
|---|---|---|
| `SONAR_TOKEN` | Secret | Authenticates with SonarQube/SonarCloud |
| `DOCKERHUB_USERNAME` | Variable | Docker Hub account used for image push |
| `DOCKERHUB_TOKEN` | Secret | Docker Hub authentication token |
| `email` | Secret | Git commit author email for the tag-update step |
| `username` | Variable | Git commit author name for the tag-update step |
| `GITHUB_TOKEN` | Built-in | Used by Gitleaks and Super-Linter for repo access and PR comments |

> This pipeline follows a "shift-left" security approach — secrets scanning and linting happen before the build, static analysis and image scanning happen during the build, and DAST validates the live deployment — catching issues as early as possible in the delivery process.