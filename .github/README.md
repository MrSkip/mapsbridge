# GitHub Actions CI/CD Pipeline

This directory contains the GitHub Actions workflow configuration for CI/CD deployment to AWS EC2.

## Required GitHub Secrets

The following secrets need to be configured in your GitHub repository settings (Settings > Secrets and variables > Actions > New repository secret):

1. `EC2_HOST` - The public IP address or hostname of your AWS EC2 instance
2. `EC2_USER` - The username to use when connecting to the EC2 instance (e.g., `ec2-user`, `ubuntu`)
3. `EC2_SSH_KEY` - The private SSH key content for connecting to the EC2 instance
4. `BOT_TOKEN` - The Telegram bot token for the application

## EC2 Instance Prerequisites

Your EC2 instance should have:

1. Docker installed and running
2. The specified user (`EC2_USER`) should have permissions to run Docker commands
3. Port 8080 should be open in the security group

## Workflow Overview

The CI/CD pipeline consists of two main jobs:

1. **Build**: Compiles the application, builds a Docker image, and saves it as an artifact
2. **Deploy**: Transfers the Docker image to the EC2 instance and runs it as a container

The workflow is triggered on pushes to the `main` branch.