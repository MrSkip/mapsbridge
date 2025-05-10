# Docker Secrets Management

This directory contains files for managing secrets and environment variables for Docker deployments of the MapsBridge application.

## Usage Instructions

1. Copy the template file to create your own environment file:
   ```bash
   cp .env.template .env
   ```

2. Edit the `.env` file and fill in your actual values:
   ```bash
   nano .env  # or use any text editor
   ```

3. When running the Docker container, use the `--env-file` option to pass these environment variables:
   ```bash
   docker run -p 8080:8080 --env-file ./docker-secrets/.env mapsbridge:latest
   ```

   Or when using Docker Compose, the env_file is already configured in the docker-compose.yml:
   ```bash
   docker-compose up -d
   ```

## Security Best Practices

- Never commit the `.env` file to version control
- Limit access to the `.env` file to only those who need it
- Consider using Docker Swarm secrets or Kubernetes secrets for production deployments
- Rotate API keys and tokens regularly

## Alternative Approaches

### Using Docker Secrets (for Docker Swarm)

If you're using Docker Swarm, you can use Docker secrets:

```bash
# Create secrets
echo "your_google_api_key" | docker secret create google_api_key -
echo "your_secure_token" | docker secret create api_security_token -

# Use secrets in your service
docker service create \
  --name mapsbridge \
  --secret google_api_key \
  --secret api_security_token \
  -e GOOGLE_API_KEY_FILE=/run/secrets/google_api_key \
  -e API_SECURITY_TOKEN_FILE=/run/secrets/api_security_token \
  -p 8080:8080 \
  mapsbridge:latest
```

### Using Kubernetes Secrets

For Kubernetes deployments:

```bash
# Create a secret
kubectl create secret generic mapsbridge-secrets \
  --from-literal=google_api_key=your_google_api_key \
  --from-literal=api_security_token=your_secure_token

# Reference in your deployment
# See kubernetes/deployment.yaml for an example
```
