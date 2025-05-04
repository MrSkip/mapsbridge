# MapsBridge – Map Link Converter API

**MapsBridge** is a Spring Boot backend service that converts single-location links from various map providers (Google Maps, Apple Maps, Bing Maps, Waze, OpenStreetMap, etc.) into equivalent links for all supported platforms.

This is the backend component of the MapsBridge web service, designed to help users quickly switch between map apps using a single, universal location link.

---

## 🔧 Features

- Accepts a location URL or coordinate input
- Parses location data from Google, Apple, Bing, OSM, and Waze
- Uses Google Geocoding/Place Details API as a fallback
- Returns provider-specific map links
- Built with Java 17 + Spring Boot
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

MapsBridge can be deployed to AWS via:

- **Elastic Beanstalk** for managed backend deployment
- **EC2** for full manual control

Environment variables must be set via EB environment settings or EC2 config scripts.

---

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## 🤝 Contributing

Pull requests are welcome! Please open an issue first to discuss proposed changes.

---

## 🔗 Related Projects

- [MapsBridge Frontend](https://github.com/yourname/mapsbridge-frontend) – Angular frontend that interacts with this API
