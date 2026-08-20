# EKS Cluster Setup & ArgoCD Installation

This guide walks through provisioning an Amazon EKS cluster on Fargate, installing ArgoCD for GitOps-based deployments, and setting up the AWS Load Balancer Controller to expose the application.

## Table of Contents

- [Prerequisites](#prerequisites)
- [1. Configure AWS Credentials](#1-configure-aws-credentials)
- [2. Create the EKS Cluster](#2-create-the-eks-cluster)
- [3. Configure kubectl](#3-configure-kubectl)
- [4. Create Namespaces](#4-create-namespaces)
- [5. Create Fargate Profiles](#5-create-fargate-profiles)
- [6. Install ArgoCD](#6-install-argocd)
- [7. Deploy the Application via ArgoCD](#7-deploy-the-application-via-argocd)
- [8. Set Up the AWS Load Balancer Controller](#8-set-up-the-aws-load-balancer-controller)
- [9. Access the Application](#9-access-the-application)

---

## Prerequisites

Install the following CLI tools before you begin:

- [`eksctl`](https://eksctl.io/) — CLI for creating and managing EKS clusters
- [`kubectl`](https://kubernetes.io/docs/tasks/tools/) — Kubernetes command-line tool
- [`AWS CLI`](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html) — AWS command-line interface
- [`helm`](https://helm.sh/docs/intro/install/) — Kubernetes package manager (used for the Load Balancer Controller)

## 1. Configure AWS Credentials

```bash
aws configure
```

Enter your AWS Access Key ID, Secret Access Key, default region, and output format when prompted.

## 2. Create the EKS Cluster

Create a cluster with Fargate as the compute option (no EC2 worker nodes to manage):

```bash
eksctl create cluster --name <cluster-name> --region <region> --fargate
```

> Replace `<cluster-name>` and `<region>` with your desired values, e.g. `my-eks-cluster` and `us-east-1`.

This step can take 15–20 minutes as EKS provisions the control plane and networking.

## 3. Configure kubectl

Update your local kubeconfig so `kubectl` can talk to the new cluster:

```bash
aws eks update-kubeconfig --name <cluster-name> --region <region>
```

Verify connectivity:

```bash
kubectl get svc
```

## 4. Create Namespaces

```bash
kubectl create ns application   # for the application
kubectl create ns argocd        # for ArgoCD
```

## 5. Create Fargate Profiles

Since the cluster runs on Fargate, each namespace that will schedule pods needs its own Fargate profile:

```bash
eksctl create fargateprofile \
  --namespace application \
  --cluster <cluster-name> \
  --region <region> \
  --name <fargate-profile-name>

eksctl create fargateprofile \
  --namespace argocd \
  --cluster <cluster-name> \
  --region <region> \
  --name <fargate-profile-name>
```

## 6. Install ArgoCD

```bash
kubectl apply -n argocd --server-side --force-conflicts \
  -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
```

Once the pods are running, retrieve the initial admin password from the `argocd-initial-admin-secret` in the `argocd` namespace to log in to the ArgoCD UI.

## 7. Deploy the Application via ArgoCD

Apply the ArgoCD `Application` manifest to register the app for GitOps sync:

```bash
kubectl apply -f application.yaml -n argocd
```

- Open the ArgoCD UI and confirm the application status shows **Synced** and **Healthy**.
- Verify the application pods are running:

```bash
kubectl get pods -n application
```

## 8. Set Up the AWS Load Balancer Controller

The AWS Load Balancer Controller provisions an ALB to expose the application to the internet.

### 8.1 Associate the OIDC Provider

```bash
eksctl utils associate-iam-oidc-provider --cluster <cluster-name> --approve
```

### 8.2 Create the IAM Policy

Download the IAM policy required by the controller:

```bash
curl -o iam-policy.json \
  https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/v2.1.2/docs/install/iam_policy.json
```

Create the IAM policy in AWS:

```bash
aws iam create-policy \
  --policy-name AWSLoadBalancerControllerIAMPolicy \
  --policy-document file://iam-policy.json
```

### 8.3 Create an IAM Role and Service Account

Use the ARN of the policy created above:

```bash
eksctl create iamserviceaccount \
  --cluster=<cluster-name> \
  --namespace=kube-system \
  --name=aws-load-balancer-controller \
  --attach-policy-arn=arn:aws:iam::<AWS_ACCOUNT_ID>:policy/AWSLoadBalancerControllerIAMPolicy \
  --override-existing-serviceaccounts \
  --approve
```

### 8.4 Install the Controller via Helm

```bash
# Add the EKS chart repo
helm repo add eks https://aws.github.io/eks-charts

# Update the repo
helm repo update

# Install the controller
helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=<cluster-name> \
  --set serviceAccount.create=false \
  --set serviceAccount.name=aws-load-balancer-controller \
  --set region=<region> \
  --set vpcId=<vpc-id>
```

### 8.5 Verify the Controller

```bash
kubectl get pods -n kube-system
```

Confirm the `aws-load-balancer-controller` pods are in a `Running` state.

## 9. Access the Application

Once the controller has provisioned an ALB, retrieve its DNS name using either of the following methods:

- **AWS Console:** Navigate to **EC2 → Load Balancers** and copy the ALB DNS name.
- **kubectl:**

```bash
kubectl get ing -n application
```

Use the returned ALB DNS name in your browser to access and verify the running application.

---

### Notes

- `<cluster-name>`, `<region>`, `<fargate-profile-name>`, `<AWS_ACCOUNT_ID>`, and `<vpc-id>` are placeholders — replace them with values specific to your environment.
- Ensure your IAM user/role has sufficient permissions for EKS, IAM, and EC2 operations before running the commands above.