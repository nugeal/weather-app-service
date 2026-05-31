# Weather App Service

A Spring Boot application deployed on AWS ECS Fargate with an Application Load Balancer.

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Deployment Guide](#deployment-guide)
  - [ECR image update](#ecr-image-update)
  - [ECS service update](#ecs-service-update)
  - [Load balancer configuration](#load-balancer-configuration)
- [Monitoring and health checks](#monitoring-and-health-checks)

## Architecture Overview

Internet → Application Load Balancer (ports 80/443) → Target Group → ECS Fargate tasks (port 8080)

## Deployment Guide

### ECR image update

1. Build and package the Spring Boot application, then build and tag a Docker image:

```bash
# Build the Spring Boot app
./mvnw clean package

# Build Docker image
docker build -t weather-app .

# Tag for ECR (replace <account-id> and region as needed)
docker tag weather-app:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/weather-app:latest
```

2. Push the image to ECR:

```bash
# Authenticate Docker to ECR
aws ecr get-login-password --region us-east-1 \\
  | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com

# Push image
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/weather-app:latest
```

### ECS service update

1. Create a new task definition revision:

- Open the ECS Console → Task Definitions.
- Select your task definition (for example `weather-app-service-task`) and click **Create new revision**.
- Update the container image URI to the new ECR image:

  `<account-id>.dkr.ecr.us-east-1.amazonaws.com/weather-app:latest`

- Review and create the revision.

2. Update the ECS service:

- Open the ECS Console → Clusters → your service (e.g., `weather-app`).
- Click **Update** → choose the new task definition revision.
- Recommended deployment settings for rolling updates:

  - Deployment type: Rolling update
  - Maximum percent: 200
  - Minimum healthy percent: 100

- Click **Update** to start the deployment.

3. Scale service (if needed):

- If desired count is 0, update the service and set the Desired count to at least 1.

4. Monitor deployment:

- In the service details, view the **Deployments** tab and wait for the deployment to become `PRIMARY`.
- Verify tasks are running in the **Tasks** tab.

### Load balancer configuration

#### Create an Application Load Balancer

- Open the EC2 Console → Load Balancers → Create Load Balancer → Application Load Balancer.
- Basic settings:

  - Name: `weather-app-alb`
  - Scheme: Internet-facing
  - IP address type: IPv4

- Network mapping:

  - VPC: (use the VPC where your ECS service runs)
  - Subnets: select at least two availability zones

- Security groups: allow HTTP (80) and HTTPS (443) from the internet, and outbound to your ECS tasks on port 8080.

#### Configure the target group

- Create or use the existing target group (for example `weather-app-service`):

  - Protocol: HTTP
  - Port: 80 (ALB listener)
  - Target type: IP
  - VPC: same VPC as ECS
  - Health check path: `/actuator/health`
  - Health check port: 8080

- Attach the target group to the ALB via the **Load balancers** tab of the target group.

#### ALB listeners

- HTTP listener (port 80): Redirect to HTTPS (301).
- HTTPS listener (port 443): Forward to the `weather-app-service` target group.

#### Security group recommendations

- ALB security group:

  - Inbound: HTTP (80) from 0.0.0.0/0, HTTPS (443) from 0.0.0.0/0
  - Outbound: Custom TCP 8080 to ECS tasks security group

- ECS tasks security group:

  - Inbound: Custom TCP 8080 from ALB security group
  - Outbound: All traffic to 0.0.0.0/0 (for external API calls)

## Monitoring and health checks

- Use the ALB health check endpoint `/actuator/health` (port 8080) to verify task health.
- Monitor ECS service deployments and CloudWatch metrics for CPU, memory, and request/response counts.

---

For deployment commands replace `<account-id>` and region values with your AWS account and target region.
