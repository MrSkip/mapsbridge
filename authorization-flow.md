# Maps Bridge Authorization Flow

## Overview
Maps Bridge uses a secure API key-based authorization system. The application implements a multi-step process for user registration, API key generation, and request authorization with rate limiting to prevent abuse.

## Authorization Flow

### 1. User Registration and API Key Generation

#### Step 1: Request an API Key
- **Endpoint**: `POST /auth/request-api-key`
- **Authentication**: None (public endpoint)
- **Request Body**: 
  ```json
  {
    "email": "user@example.com"
  }
  ```
- **Process**:
  1. The system checks IP-based rate limits to prevent abuse
  2. The system checks email-based rate limits to prevent abuse
  3. A unique confirmation token is generated and stored in the database
  4. An email with a confirmation link is sent to the provided email address
  5. The token expires after a configurable time period (default: 15 minutes)

#### Step 2: Email Confirmation and API Key Generation
- **Endpoint**: `GET /auth/api/confirm?token={token}`
- **Authentication**: None (public endpoint)
- **Process**:
  1. The system validates the token (checks if it exists, is not expired, and not used)
  2. If valid, the token is marked as used
  3. A new API key is generated with format `maps_live_xxxxxxxx`
  4. The API key is stored in the database and associated with the user's email
  5. The API key is returned to the user

### 2. API Authentication Mechanism

#### API Key Validation
- **Header**: `X-API-Key`
- **Process**:
  1. For protected endpoints, the `ApiKeyAuthFilter` extracts the API key from the request header
  2. The `ApiKeyAuthManager` validates the API key:
     - Checks if it's the master token (has full access)
     - Checks if it's a valid user API key (has limited access)
  3. If valid, the request is allowed to proceed with appropriate authorities
  4. If invalid, a 401 Unauthorized response is returned

#### Authorization Roles
1. **Anonymous** - No authentication
   - Can access public endpoints only
   
2. **API User** (`ROLE_API_USER`)
   - Has access to authenticated endpoints
   - Created when a user confirms their email and receives an API key
   
3. **Master** (`ROLE_MASTER`)
   - Has access to all endpoints including admin and metrics
   - Uses a special master token configured in the application

### 3. Endpoint Security Configuration

The application uses a hierarchical security model defined in `endpoint-security.properties`:

1. **Public Endpoints** (no authentication required)
   - `/auth/request-api-key` - Request an API key
   - `/auth/confirm` - Confirm email and get API key

2. **Authenticated Endpoints** (requires valid API key)
   - `/api/**` - All API endpoints, including:
     - `POST /api/convert` - Convert map coordinates between providers

3. **Master-Only Endpoints** (requires master token)
   - `/admin/**` - Administrative endpoints
   - `/metrics/**` - Application metrics
   - `/actuator/**` - Spring Boot Actuator endpoints

### 4. Rate Limiting

The application implements rate limiting to prevent abuse:

1. **IP-based Rate Limiting**
   - Limits the number of requests from a single IP address
   - Prevents brute force attacks and abuse
   - Throws `IpRateLimitExceededException` when exceeded

2. **Email-based Rate Limiting**
   - Limits the number of API key requests for a single email
   - Prevents email spam and abuse
   - Throws `EmailRateLimitExceededException` when exceeded

Rate limiters are automatically cleaned up after a period of inactivity to prevent memory leaks.

## Security Implementation Details

### API Key Authentication Filter
The `ApiKeyAuthFilter` intercepts all requests to protected endpoints and:
1. Extracts the API key from the `X-API-Key` header
2. Passes it to the authentication manager for validation
3. Sets the authentication in the security context if valid
4. Returns 401 Unauthorized if invalid

### API Key Storage and Validation
- API keys are stored in the database with the following information:
  - Unique ID
  - Email address
  - API key string
  - Creation timestamp
  - Last used timestamp
  - Active status
- When a request is made, the system:
  1. Looks up the API key in the database
  2. Verifies it's active
  3. Updates the "last used" timestamp
  4. Grants appropriate authorities

### Email Confirmation Security
- Confirmation tokens are one-time use only
- Tokens expire after a configurable time period
- Rate limiting prevents abuse of the confirmation system

## Usage Example

1. **Request an API Key**:
   ```
   POST /auth/request-api-key
   Content-Type: application/json
   
   {
     "email": "user@example.com"
   }
   ```

2. **Confirm Email and Get API Key**:
   User clicks the link in their email or manually visits:
   ```
   GET /auth/confirm?token=abc123
   ```

3. **Use the API Key**:
   ```
   POST /api/convert
   Content-Type: application/json
   X-API-Key: maps_live_xxxxxxxx
   
   {
     "input": "https://maps.google.com/?q=40.7128,-74.0060"
   }
   ```

This completes the authorization flow from registration to API usage.