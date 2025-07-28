# CloudWatch Monitoring Setup for Maps Bridge

This guide explains how to deploy the CloudFormation template for setting up comprehensive CloudWatch monitoring for the
Maps Bridge application.

## Prerequisites

- AWS CLI installed and configured with appropriate permissions
- An AWS account with permissions to create CloudFormation stacks, IAM roles, CloudWatch dashboards and alarms

## Deployment Steps

### Option 1: Using AWS Console

1. **Go to AWS CloudFormation Console**
    - Navigate to [AWS CloudFormation Console](https://console.aws.amazon.com/cloudformation/)
    - Click "Create stack" → "With new resources (standard)"

2. **Upload the Template**
    - Choose "Upload a template file"
    - Upload the `cloudformation-template.yaml` file
    - Click "Next"

3. **Configure Parameters**
    - **Stack name**: `mapsbridge-monitoring-prod` (or your preferred name)
    - **ApplicationName**: `mapsbridge` (default)
    - **Environment**: Choose `prod`, `staging`, or `dev`
    - **NotificationEmail**: Enter your email for alarm notifications

4. **Complete Deployment**
    - Click through the remaining steps
    - Check "I acknowledge that AWS CloudFormation might create IAM resources"
    - Click "Create stack"

### Option 2: Using AWS CLI

Run the following command:

```bash
aws cloudformation create-stack \
  --stack-name mapsbridge-monitoring-prod \
  --template-body file://cloudformation-template.yaml \
  --parameters ParameterKey=NotificationEmail,ParameterValue=your-email@example.com \
               ParameterKey=Environment,ParameterValue=prod \
  --capabilities CAPABILITY_IAM \
  --region eu-central-1
```

## Configuring Application for CloudWatch

### Adding AWS Credentials to CI/CD

Update your GitHub repository secrets:

1. Go to your GitHub repository → Settings → Secrets and variables → Actions
2. Add these secrets:
    - `AWS_ACCESS_KEY_ID`: Your AWS access key
    - `AWS_SECRET_ACCESS_KEY`: Your AWS secret key

### Update CI/CD Workflow (ci-cd.yml)

Add these environment variables to your Docker run command in the CI/CD workflow:

```yaml
-e AWS_REGION=eu-central-1 \
-e AWS_ACCESS_KEY_ID="${{ secrets.AWS_ACCESS_KEY_ID }}" \
-e AWS_SECRET_ACCESS_KEY="${{ secrets.AWS_SECRET_ACCESS_KEY }}"
```

## Verify Deployment

1. **Check Email Confirmation**
    - Look for an SNS subscription confirmation email
    - Click the confirmation link to receive alarm notifications

2. **Access Dashboard**
    - Go to CloudWatch → Dashboards
    - Open the `mapsbridge-prod-monitoring` dashboard
    - Verify widgets are populated with data (may take a few minutes)

3. **Review Alarms**
    - Go to CloudWatch → Alarms
    - Verify all alarms are in the "OK" state

## Troubleshooting

- **No metrics appearing?** Ensure your application has the proper AWS credentials and permissions.
- **Missing logs?** Check that the log group name in your application matches the one in CloudFormation (
  `mapsbridge-log-group`).
- **Alarm not sending notifications?** Verify you confirmed the SNS subscription email.

## Clean Up

To remove all resources, delete the CloudFormation stack:

```bash
aws cloudformation delete-stack --stack-name mapsbridge-monitoring-prod --region eu-central-1
```

## IAM Permissions

The CloudFormation template creates the following IAM resources:

- An IAM role with permissions to publish CloudWatch metrics and logs
- An IAM instance profile for EC2 instances

If you're not using EC2 or need a custom IAM user instead, create a user with this policy:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "cloudwatch:PutMetricData",
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents",
        "logs:DescribeLogStreams"
      ],
      "Resource": "*"
    }
  ]
}
```
