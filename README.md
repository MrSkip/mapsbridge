# MapsBridge – Map Link Converter API

**MapsBridge** is a Spring Boot backend service that converts single-location links from various map providers (Google Maps, Apple Maps, Bing Maps, Waze, OpenStreetMap, etc.) into equivalent links for all supported platforms.

This is the backend component of the MapsBridge web service, designed to help users quickly switch between map apps using a single, universal location link.

---

## 🔧 Features

- Accepts a location URL or coordinate input
- Parses location data from Google, Apple, Bing, OSM, and Waze
- Uses Google Geocoding/Place Details API as a fallback
- Returns provider-specific map links
- Built with Java 21 + Spring Boot
- RESTful API with OpenAPI docs
- Supports logging and rate limiting

---

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/yourname/mapsbridge-backend.git
cd mapsbridge-backend
```

### 2. Prerequisites

- Java 21+
- Gradle
- Google Cloud API Key (for Places API / Geocoding API)

### 3. Configuration

Create a `.env` file or export these environment variables:

```env
GOOGLE_API_KEY=your_google_api_key
RATE_LIMIT=50  # optional
```

> Optionally, use an `application.yml` or `application.properties` to manage config more formally.

### 4. Run the Application

```bash
./mvnw spring-boot:run
```

### 5. Access Swagger Documentation

Open [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 🛠️ API Usage

### `POST /convert`

**Request:**

```json
{
  "input": "https://maps.google.com/?q=Statue+of+Liberty"
}
```

**Response:**

```json
{
  "coordinates": {
    "lat": 40.6892,
    "lon": -74.0445
  },
  "links": {
    "google": "https://www.google.com/maps?q=40.6892,-74.0445",
    "apple": "https://maps.apple.com/?ll=40.6892,-74.0445",
    "osm": "https://www.openstreetmap.org/?mlat=40.6892&mlon=-74.0445",
    "bing": "https://www.bing.com/maps?q=40.6892,-74.0445",
    "waze": "https://waze.com/ul?ll=40.6892,-74.0445&navigate=yes"
  }
}
```

---

## 🧪 Testing

Run all tests with:

```bash
./mvnw test
```

Includes:
- Unit tests for URL parsing logic
- Integration tests for `/convert` endpoint

---

## 📊 Analytics (Optional)

- Integrate with PostHog or Plausible for usage tracking
- Use AWS CloudWatch for backend logs and performance insights

---

## 🚀 Deployment

### Docker Deployment

The project includes a Dockerfile for containerized deployment:

```bash
# Build the Docker image
docker build -t mapsbridge:latest .

# Run the container
docker run -p 8080:8080 \
  -e GOOGLE_API_KEY=your_google_api_key \
  -e API_SECURITY_TOKEN=your_secure_token \
  -e SPRING_PROFILES_ACTIVE=prod \
  mapsbridge:latest
```

#### Using Docker Compose

For easier deployment, you can use Docker Compose:

```bash
# Start the application
docker-compose up -d

# View logs
docker-compose logs -f

# Stop the application
docker-compose down
```

The docker-compose.yml file includes:
- Automatic building of the Docker image
- Environment variable configuration via docker-secrets/.env
- Health checks for the application
- Volume mounting for persistent logs

#### Environment Variables for Docker

| Variable | Description | Default |
|----------|-------------|---------|
| `GOOGLE_API_KEY` | Google API key for geocoding | (empty) |
| `GOOGLE_API_ENABLED` | Enable/disable Google API | false |
| `API_SECURITY_TOKEN` | Security token for API authentication | default-secure-token |
| `SPRING_PROFILES_ACTIVE` | Spring active profile | prod |
| `JAVA_OPTS` | JVM options | -Xms256m -Xmx512m |

#### Docker Secrets Management

For better security, the project includes a structured approach to manage secrets:

1. Use the provided template in the `docker-secrets` directory:
   ```bash
   cp docker-secrets/.env.template docker-secrets/.env
   ```

2. Edit the `.env` file with your actual secrets and configuration:
   ```bash
   nano docker-secrets/.env
   ```

3. Run the container with the environment file:
   ```bash
   docker run -p 8080:8080 --env-file ./docker-secrets/.env mapsbridge:latest
   ```

   Or when using Docker Compose (the env_file is already configured in docker-compose.yml):
   ```bash
   docker-compose up -d
   ```

This approach keeps your secrets separate from the Docker image and prevents them from being committed to version control. For more advanced secret management options (Docker Swarm secrets, Kubernetes secrets), see the README in the `docker-secrets` directory.

### Cloud Deployment

MapsBridge can be deployed to AWS via:

- **Elastic Beanstalk** for managed backend deployment
- **EC2** for full manual control
- **ECS/EKS** for containerized deployment using the provided Dockerfile

Environment variables must be set via EB environment settings, EC2 config scripts, or container environment variables.

### GitHub Actions CI/CD Pipeline

The project includes a GitHub Actions workflow for automated CI/CD deployment to AWS EC2:

```yaml
# .github/workflows/ci-cd.yml
name: CI/CD to AWS EC2

on:
  push:
    branches:
      - main
```

The pipeline automatically:
1. Builds the application with Gradle
2. Creates a Docker image
3. Deploys the image to an EC2 instance

To use this pipeline, you need to configure the following GitHub secrets:
- `EC2_HOST`: Your EC2 instance hostname or IP
- `EC2_USER`: SSH username for your EC2 instance
- `EC2_SSH_KEY`: Private SSH key for EC2 access
- `BOT_TOKEN`: Telegram bot token

For more details, see the [GitHub Actions documentation](.github/README.md).

---

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## 🤝 Contributing

Pull requests are welcome! Please open an issue first to discuss proposed changes.

---

## 🔗 Related Projects

- [MapsBridge Frontend](https://github.com/yourname/mapsbridge-frontend) – Angular frontend that interacts with this API
