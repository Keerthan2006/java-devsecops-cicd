# Java DevSecOps CI/CD Pipeline

## Introduction

This project demonstrates an end-to-end **DevSecOps CI/CD pipeline** for a Java application, built to showcase how security can be embedded at every stage of the software delivery lifecycle rather than bolted on at the end.

Starting from a simple Java application, the project walks through containerizing it, automating build/test/security checks through GitHub Actions, and deploying it to a production-grade environment on **Amazon EKS** using the **GitOps** methodology with **ArgoCD**.

The core idea behind this project is "shift-left security" — catching vulnerabilities and misconfigurations as early as possible:

- **Secret scanning** and **linting** before any code is built
- **Static code analysis** and **dependency/image vulnerability scanning** during the build
- **Dynamic security testing** against the live, deployed application

### What This Project Covers

1. **Local Development & Testing** — Building and running the Java application locally to validate it before containerization.
2. **Containerization** — A multi-stage, distroless Docker build for a minimal, secure runtime image.
3. **CI/CD Automation** — A GitHub Actions pipeline that lints, builds, tests, scans, and pushes the application automatically.
4. **GitOps Deployment** — ArgoCD continuously syncs the desired state from this repository to the Kubernetes cluster.
5. **Cloud Infrastructure** — An Amazon EKS cluster (Fargate) hosting the application, exposed via the AWS Load Balancer Controller.

### Tech Stack

| Category | Tools |
|---|---|
| Language / Build | Java 17, Maven |
| Containerization | Docker (multi-stage, distroless) |
| CI/CD | GitHub Actions |
| Code Quality & Security | Gitleaks, Super-Linter, SonarQube, Trivy, OWASP ZAP |
| Container Registry | Docker Hub |
| Orchestration | Amazon EKS (Fargate) |
| GitOps / Deployment | ArgoCD |
| Networking | AWS Load Balancer Controller (ALB) |

### Project Journey

The application was first built and verified locally to ensure it worked before any automation was introduced:

```bash
mvn dependency:go-offline
mvn clean package -DskipTests
java -jar target/app.jar
mvn test
```

Once local testing confirmed the app was healthy, it was containerized using a multi-stage Dockerfile — a Maven-based builder stage to compile the application, and a lightweight **distroless** runtime image to minimize the attack surface and image size:

```dockerfile
# Stage 1
FROM maven:3.9.16-eclipse-temurin-17-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY . .
RUN mvn clean package -DskipTests

# Stage 2
FROM gcr.io/distroless/java17-debian13
WORKDIR /app
COPY --from=builder /app/target/*.jar ./app.jar
EXPOSE 9090
ENTRYPOINT [ "java","-jar","app.jar" ]
```

With the application containerized, the project moved on to building the CI/CD pipeline and the supporting Kubernetes/GitOps infrastructure documented in this repository.

### Repository Structure

```
.
├── .github/workflows/   # CI/CD pipeline definitions (see README in this folder)
├── java-app/            # Helm chart / K8s manifests + EKS & ArgoCD setup (see README in this folder)
├── kubernetes/          # Kubernetes-related resources
├── src/                 # Java application source code
├── Dockerfile           # Multi-stage, distroless Docker build
├── application.yaml     # ArgoCD Application manifest
├── iam_policy.json       # IAM policy for AWS Load Balancer Controller
└── pom.xml              # Maven project configuration
```

### Documentation

This project's documentation is split across three READMEs:

| README | Location | Covers |
|---|---|---|
| Project Introduction (this file) | `/README.md` | Project overview, tech stack, and journey |
| CI/CD Pipeline | [`.github/workflows/README.md`](.github/workflows/README.md) | Full breakdown of the GitHub Actions pipeline stages |
| EKS & ArgoCD Setup | [`java-app/README.md`](java-app/README.md) | Provisioning EKS, installing ArgoCD, and exposing the app via ALB |

### Resources Followed

- [Amazon EKS Documentation](https://docs.aws.amazon.com/eks/latest/userguide/what-is-eks.html)
- [GitHub Actions Marketplace](https://github.com/marketplace?type=actions)
- [ArgoCD Getting Started Guide](https://argo-cd.readthedocs.io/en/stable/getting_started/)

## Conclusion

This project brings together application development, containerization, automated security tooling, and cloud-native deployment into a single, cohesive pipeline. By integrating secret scanning, static analysis, container image scanning, and dynamic application security testing directly into the CI/CD workflow, security becomes a continuous, automated part of delivery rather than a manual, after-the-fact process.

Pairing this with a GitOps deployment model via ArgoCD ensures that the state of the Kubernetes cluster always reflects what's committed to the repository, making deployments predictable, auditable, and easy to roll back if needed.

Overall, this project serves as a practical reference for building a secure, automated, and cloud-native delivery pipeline for a Java application — from a developer's first `mvn test` all the way to a running, internet-facing service on Amazon EKS.