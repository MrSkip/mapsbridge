# Maps Bridge AWS Infrastructure

This document describes the AWS infrastructure setup for the Maps Bridge application using CloudFormation.

## Infrastructure Overview

The CloudFormation template (`cloudformation-template-updated.yaml`) creates the following resources:

1. **Networking**:
    - VPC with DNS support
    - Public and private subnets across two availability zones
    - Internet Gateway for public internet access
    - Route tables for public and private subnets

2. **Compute**:
    - EC2 instance (t2.micro by default) for running the application
    - Docker pre-installed for container deployment

3. **Database**:
    - PostgreSQL RDS instance (db.t3.micro by default)
    - Deployed in private subnets for security
    - Accessible only from the EC2 instance

4. **Monitoring**:
    - CloudWatch Log Group for application logs
    - CloudWatch Dashboard with application metrics

5. **Security**:
    - Security groups for EC2 and RDS
    - IAM role and instance profile for EC2 to access CloudWatch
    - SSH access to EC2 for deployment

## Deployment Instructions

### Prerequisites

1. AWS CLI installed and configured with appropriate credentials
2. An existing EC2 Key Pair for SSH access
3. Sufficient permissions to create all the resources in the template

### Deployment Steps

1. **Prepare parameters**:

   Create a parameters file named `parameters.json` with the following content:

   ```json
   [
     {
       "ParameterKey": "ApplicationName",
       "ParameterValue": "mapsbridge"
     },
     {
       "ParameterKey": "Environment",
       "ParameterValue": "prod"
     },
     {
       "ParameterKey": "NotificationEmail",
       "ParameterValue": "your-email@example.com"
     },
     {
       "ParameterKey": "EC2KeyName",
       "ParameterValue": "your-key-pair-name"
     },
     {
       "ParameterKey": "DBUsername",
       "ParameterValue": "dbadmin"
     },
     {
       "ParameterKey": "DBPassword",
       "ParameterValue": "your-secure-password"
     },
     {
       "ParameterKey": "DomainName",
       "ParameterValue": "mapsbridge.com"
     },
     {
       "ParameterKey": "CreateHostedZone",
       "ParameterValue": "false"
     },
     {
       "ParameterKey": "ExistingHostedZoneId",
       "ParameterValue": "YOUR_HOSTED_ZONE_ID"
     }
   ]
   ```

   > **Note about domain configuration:**
   > - Set `DomainName` to "mapsbridge.com" to ensure the application is accessible via this domain
   > - Set `CreateHostedZone` to "true" if you need to create a new Route53 hosted zone for the domain
   > - Set `CreateHostedZone` to "false" and provide `ExistingHostedZoneId` if you already have a hosted zone for the
       domain

   Replace the values with your own settings.

2. **Deploy the CloudFormation stack**:

   ```bash
   aws cloudformation create-stack \
     --stack-name mapsbridge-infrastructure \
     --template-body file://cloudformation-template.yaml \
     --parameters file://parameters.json \
     --capabilities CAPABILITY_IAM
   ```

3. **Monitor the stack creation**:

   ```bash
   aws cloudformation describe-stacks --stack-name mapsbridge-infrastructure
   ```

4. **Get the outputs**:

   Once the stack is created, retrieve the outputs to get the EC2 and RDS endpoints:

   ```bash
   aws cloudformation describe-stacks \
     --stack-name mapsbridge-infrastructure \
     --query "Stacks[0].Outputs"
   ```

### GitHub CI/CD Integration

The existing GitHub Actions workflow (`ci-cd.yml`) deploys the application to the EC2 instance. To integrate with the
new infrastructure:

1. Add the following secrets to your GitHub repository:
    - `EC2_HOST`: The public DNS or IP of your EC2 instance (from stack outputs)
    - `EC2_USER`: The username for SSH access (typically `ec2-user` for Amazon Linux)
    - `EC2_SSH_KEY`: The private SSH key corresponding to the EC2 key pair
    - `SPRING_DATASOURCE_URL`: The JDBC URL for the RDS instance (e.g.,
      `jdbc:postgresql://<RDS_ENDPOINT>:5432/mapsbridge`)
    - `SPRING_DATASOURCE_USERNAME`: The database username
    - `SPRING_DATASOURCE_PASSWORD`: The database password

2. The workflow will automatically deploy the application to the EC2 instance when changes are pushed to the main
   branch.

## Monitoring

The CloudWatch dashboard provides visibility into:

1. **Application Metrics**:
    - Map extraction success/failure rates
    - HTTP request statistics
    - JVM memory usage
    - Rate limiter metrics

2. **Infrastructure Metrics**:
    - EC2 CPU utilization
    - RDS CPU utilization and connections
    - RDS storage space

3. **Logs**:
    - Application error logs
    - System logs
    - Docker container logs

Access the dashboard at the URL provided in the stack outputs.

## Alarms

The following CloudWatch alarms are configured:

1. **Application Alarms**:
    - High error rate
    - High memory usage
    - Map extraction failures
    - Rate limiter exhaustion

2. **Infrastructure Alarms**:
    - EC2 high CPU utilization
    - RDS high CPU utilization
    - RDS low storage space

Alarms will send notifications to the email address specified in the parameters.

## Security Considerations

1. **Network Security**:
    - RDS is in private subnets, not accessible from the internet
    - EC2 is in a public subnet with only necessary ports open (SSH and application port)

2. **Access Control**:
    - IAM roles follow the principle of least privilege
    - SSH access is restricted to authorized keys

3. **Data Security**:
    - Database credentials are stored securely
    - All sensitive parameters use the `NoEcho` property

## Cost Optimization

The infrastructure uses free tier eligible resources:

- t2.micro EC2 instance
- db.t3.micro RDS instance
- Minimal storage allocations

Monitor your AWS billing to ensure you stay within free tier limits.

## Maintenance

### Updating the Stack

To update the infrastructure:

```bash
aws cloudformation update-stack \
  --stack-name mapsbridge-infrastructure \
  --template-body file://cloudformation-template.yaml \
  --parameters file://parameters.json \
  --capabilities CAPABILITY_IAM
```

### Deleting the Stack

To delete all resources:

```bash
aws cloudformation delete-stack --stack-name mapsbridge-infrastructure
```

Note: This will delete all resources including the database. Make sure to back up any important data first.