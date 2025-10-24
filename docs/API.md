# API Documentation

## Table of Contents

- [Overview](#overview)
- [Base URL](#base-url)
- [Authentication](#authentication)
- [Common Response Codes](#common-response-codes)
- [Error Response Format](#error-response-format)
- [Data Models](#data-models)
- [Endpoints](#endpoints)
  - [Create Device](#create-device)
  - [Get All Devices](#get-all-devices)
  - [Get Device by ID](#get-device-by-id)
  - [Get Devices by Brand](#get-devices-by-brand)
  - [Get Devices by State](#get-devices-by-state)
  - [Full Update Device](#full-update-device)
  - [Partial Update Device](#partial-update-device)
  - [Delete Device](#delete-device)

## Overview

The Device Management API provides RESTful endpoints for managing device records. All endpoints return JSON responses and follow REST conventions for HTTP methods and status codes.

**Version**: 1.0.0
**Content-Type**: `application/json`
**API Documentation**: http://localhost:8080/device-domain/swagger-ui.html

## Base URL

```
http://localhost:8080/device-domain/api/v1/devices
```

For production deployments, replace `localhost:8080` with your domain.

## Authentication

Currently, the API does not require authentication. Future versions will implement:
- Spring Security with JWT tokens
- OAuth2 integration
- API key authentication

See [FUTURE_IMPROVEMENTS.md](FUTURE_IMPROVEMENTS.md) for security roadmap.

## Common Response Codes

| Status Code | Description |
|-------------|-------------|
| 200 OK | Request succeeded, response body contains data |
| 201 Created | Resource created successfully |
| 204 No Content | Request succeeded, no response body |
| 400 Bad Request | Invalid request format or business rule violation |
| 404 Not Found | Resource not found |
| 409 Conflict | Resource state conflict (e.g., deleting device in use) |
| 500 Internal Server Error | Unexpected server error |

## Error Response Format

All error responses follow a consistent format:

```json
{
  "timestamp": "2025-10-24T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Descriptive error message",
  "path": "/api/v1/devices/123e4567-e89b-12d3-a456-426614174000",
  "details": [
    "Field 'name' must not be blank",
    "Field 'brand' must not be blank"
  ]
}
```

**Fields**:
- `timestamp`: ISO-8601 formatted timestamp of when error occurred
- `status`: HTTP status code
- `error`: HTTP status text
- `message`: Human-readable error description
- `path`: Request path that caused the error
- `details`: (Optional) List of validation errors or additional context

## Data Models

### DeviceState Enum

Represents the current state of a device.

**Values**:
- `AVAILABLE` - Device is ready for use
- `IN_USE` - Device is currently being used
- `INACTIVE` - Device is not active but not deleted

**State Transitions**:
```
AVAILABLE ↔ IN_USE
AVAILABLE ↔ INACTIVE
IN_USE ↔ INACTIVE
```

### Device Response

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "iPhone 14 Pro",
  "brand": "Apple",
  "state": "AVAILABLE",
  "creationTime": "2025-10-24T10:15:30"
}
```

**Fields**:
- `id` (UUID): Unique identifier, generated automatically
- `name` (string): Device name, required, non-blank
- `brand` (string): Manufacturer/brand, required, non-blank
- `state` (DeviceState): Current device state, required
- `creationTime` (datetime): ISO-8601 timestamp, set automatically, immutable

### Device Request

```json
{
  "name": "iPhone 14 Pro",
  "brand": "Apple",
  "state": "AVAILABLE"
}
```

**Fields**:
- `name` (string): Required, must not be blank
- `brand` (string): Required, must not be blank
- `state` (DeviceState): Required, must be valid enum value

### Device Update Request (Partial)

```json
{
  "name": "iPhone 14 Pro Max",
  "brand": "Apple",
  "state": "IN_USE"
}
```

**Fields**: All fields are optional
- `name` (string): Optional, if provided must not be blank
- `brand` (string): Optional, if provided must not be blank
- `state` (DeviceState): Optional, if provided must be valid enum value

**Note**: Null fields are ignored (field not updated).

---

## Endpoints

### Create Device

Creates a new device record.

**Endpoint**: `POST /api/v1/devices`

**Request Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "name": "MacBook Pro 16",
  "brand": "Apple",
  "state": "AVAILABLE"
}
```

**Success Response**:

**Status**: `201 Created`

```json
{
  "id": "7f3d5e9c-4a1b-4c8d-9e2f-1a3b5c7d9e0f",
  "name": "MacBook Pro 16",
  "brand": "Apple",
  "state": "AVAILABLE",
  "creationTime": "2025-10-24T14:30:45"
}
```

**Error Responses**:

**Status**: `400 Bad Request` - Invalid input
```json
{
  "timestamp": "2025-10-24T14:30:45",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/devices",
  "details": [
    "name: must not be blank",
    "state: must not be null"
  ]
}
```

**Example cURL**:
```bash
curl -X POST http://localhost:8080/device-domain/api/v1/devices \
  -H "Content-Type: application/json" \
  -d '{
    "name": "MacBook Pro 16",
    "brand": "Apple",
    "state": "AVAILABLE"
  }'
```

**Business Rules**:
- UUID is generated automatically
- Creation time is set to current timestamp
- Name and brand must not be blank
- State must be a valid DeviceState value

---

### Get All Devices

Retrieves all device records.

**Endpoint**: `GET /api/v1/devices`

**Request Headers**: None required

**Query Parameters**: None

**Success Response**:

**Status**: `200 OK`

```json
[
  {
    "id": "7f3d5e9c-4a1b-4c8d-9e2f-1a3b5c7d9e0f",
    "name": "MacBook Pro 16",
    "brand": "Apple",
    "state": "AVAILABLE",
    "creationTime": "2025-10-24T14:30:45"
  },
  {
    "id": "8a4e6f0d-5b2c-5d9e-0f3g-2b4c6d8e0a1g",
    "name": "ThinkPad X1",
    "brand": "Lenovo",
    "state": "IN_USE",
    "creationTime": "2025-10-23T09:15:20"
  }
]
```

**Empty Result**:
```json
[]
```

**Example cURL**:
```bash
curl -X GET http://localhost:8080/device-domain/api/v1/devices
```

**Notes**:
- Returns empty array if no devices exist
- Results are not paginated (see [FUTURE_IMPROVEMENTS.md](FUTURE_IMPROVEMENTS.md))
- No specific ordering guaranteed

---

### Get Device by ID

Retrieves a single device by its unique identifier.

**Endpoint**: `GET /api/v1/devices/{id}`

**Path Parameters**:
- `id` (UUID): Device unique identifier

**Success Response**:

**Status**: `200 OK`

```json
{
  "id": "7f3d5e9c-4a1b-4c8d-9e2f-1a3b5c7d9e0f",
  "name": "MacBook Pro 16",
  "brand": "Apple",
  "state": "AVAILABLE",
  "creationTime": "2025-10-24T14:30:45"
}
```

**Error Responses**:

**Status**: `404 Not Found`
```json
{
  "timestamp": "2025-10-24T14:35:00",
  "status": 404,
  "error": "Not Found",
  "message": "Device not found with id: 7f3d5e9c-4a1b-4c8d-9e2f-1a3b5c7d9e0f",
  "path": "/api/v1/devices/7f3d5e9c-4a1b-4c8d-9e2f-1a3b5c7d9e0f",
  "details": null
}
```

**Status**: `400 Bad Request` - Invalid UUID format
```json
{
  "timestamp": "2025-10-24T14:35:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid UUID string: not-a-uuid",
  "path": "/api/v1/devices/not-a-uuid",
  "details": null
}
```

**Example cURL**:
```bash
curl -X GET http://localhost:8080/device-domain/api/v1/devices/7f3d5e9c-4a1b-4c8d-9e2f-1a3b5c7d9e0f
```

---

### Get Devices by Brand

Retrieves all devices matching a specific brand.

**Endpoint**: `GET /api/v1/devices/brand/{brand}`

**Path Parameters**:
- `brand` (string): Brand/manufacturer name (case-sensitive)

**Success Response**:

**Status**: `200 OK`

```json
[
  {
    "id": "7f3d5e9c-4a1b-4c8d-9e2f-1a3b5c7d9e0f",
    "name": "MacBook Pro 16",
    "brand": "Apple",
    "state": "AVAILABLE",
    "creationTime": "2025-10-24T14:30:45"
  },
  {
    "id": "9b5f7g1e-6c3d-6e0f-1g4h-3c5d7e9f1b2h",
    "name": "iPhone 14 Pro",
    "brand": "Apple",
    "state": "IN_USE",
    "creationTime": "2025-10-22T11:20:30"
  }
]
```

**Empty Result**:
```json
[]
```

**Error Responses**:

**Status**: `400 Bad Request`
```json
{
  "timestamp": "2025-10-24T14:40:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Brand must not be null or empty",
  "path": "/api/v1/devices/brand/ ",
  "details": null
}
```

**Example cURL**:
```bash
curl -X GET http://localhost:8080/device-domain/api/v1/devices/brand/Apple
```

**Notes**:
- Brand matching is case-sensitive
- Returns empty array if no devices found for brand
- Indexed query for performance (see V2__Add_Brand_State_Indexes.sql)

---

### Get Devices by State

Retrieves all devices in a specific state.

**Endpoint**: `GET /api/v1/devices/state/{state}`

**Path Parameters**:
- `state` (DeviceState): One of: `AVAILABLE`, `IN_USE`, `INACTIVE`

**Success Response**:

**Status**: `200 OK`

```json
[
  {
    "id": "7f3d5e9c-4a1b-4c8d-9e2f-1a3b5c7d9e0f",
    "name": "MacBook Pro 16",
    "brand": "Apple",
    "state": "AVAILABLE",
    "creationTime": "2025-10-24T14:30:45"
  },
  {
    "id": "0c6g8h2f-7d4e-7f1g-2h5i-4d6e8f0g2c3i",
    "name": "Surface Laptop",
    "brand": "Microsoft",
    "state": "AVAILABLE",
    "creationTime": "2025-10-21T08:45:15"
  }
]
```

**Empty Result**:
```json
[]
```

**Error Responses**:

**Status**: `400 Bad Request` - Invalid state value
```json
{
  "timestamp": "2025-10-24T14:45:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid device state: UNKNOWN. Valid values are: AVAILABLE, IN_USE, INACTIVE",
  "path": "/api/v1/devices/state/UNKNOWN",
  "details": null
}
```

**Example cURL**:
```bash
curl -X GET http://localhost:8080/device-domain/api/v1/devices/state/AVAILABLE
```

**Notes**:
- State parameter is case-sensitive
- Returns empty array if no devices in specified state
- Indexed query for performance

---

### Full Update Device

Replaces all mutable fields of an existing device. All fields are required.

**Endpoint**: `PUT /api/v1/devices/{id}`

**Path Parameters**:
- `id` (UUID): Device unique identifier

**Request Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "name": "MacBook Pro 16 (Updated)",
  "brand": "Apple",
  "state": "IN_USE"
}
```

**Success Response**:

**Status**: `200 OK`

```json
{
  "id": "7f3d5e9c-4a1b-4c8d-9e2f-1a3b5c7d9e0f",
  "name": "MacBook Pro 16 (Updated)",
  "brand": "Apple",
  "state": "IN_USE",
  "creationTime": "2025-10-24T14:30:45"
}
```

**Error Responses**:

**Status**: `404 Not Found`
```json
{
  "timestamp": "2025-10-24T14:50:00",
  "status": 404,
  "error": "Not Found",
  "message": "Device not found with id: 7f3d5e9c-4a1b-4c8d-9e2f-1a3b5c7d9e0f",
  "path": "/api/v1/devices/7f3d5e9c-4a1b-4c8d-9e2f-1a3b5c7d9e0f",
  "details": null
}
```

**Status**: `400 Bad Request` - Cannot update IN_USE device
```json
{
  "timestamp": "2025-10-24T14:50:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Cannot update name or brand while device is IN_USE",
  "path": "/api/v1/devices/7f3d5e9c-4a1b-4c8d-9e2f-1a3b5c7d9e0f",
  "details": null
}
```

**Status**: `400 Bad Request` - Invalid state transition
```json
{
  "timestamp": "2025-10-24T14:50:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid state transition from AVAILABLE to IN_USE",
  "path": "/api/v1/devices/7f3d5e9c-4a1b-4c8d-9e2f-1a3b5c7d9e0f",
  "details": null
}
```

**Status**: `400 Bad Request` - Validation errors
```json
{
  "timestamp": "2025-10-24T14:50:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/devices/7f3d5e9c-4a1b-4c8d-9e2f-1a3b5c7d9e0f",
  "details": [
    "name: must not be blank"
  ]
}
```

**Example cURL**:
```bash
curl -X PUT http://localhost:8080/device-domain/api/v1/devices/7f3d5e9c-4a1b-4c8d-9e2f-1a3b5c7d9e0f \
  -H "Content-Type: application/json" \
  -d '{
    "name": "MacBook Pro 16 (Updated)",
    "brand": "Apple",
    "state": "IN_USE"
  }'
```

**Business Rules**:
1. **Device must exist** - Returns 404 if not found
2. **Cannot update name/brand if IN_USE** - Device must be AVAILABLE or INACTIVE
3. **Valid state transition required** - See state transition rules
4. **Creation time is immutable** - Preserved from original record
5. **All fields required** - Name, brand, and state must be provided

**Valid State Transitions**:
- `AVAILABLE` → `IN_USE`, `INACTIVE`
- `IN_USE` → `AVAILABLE`, `INACTIVE`
- `INACTIVE` → `AVAILABLE`

---

### Partial Update Device

Updates only the specified fields of an existing device. All fields are optional.

**Endpoint**: `PATCH /api/v1/devices/{id}`

**Path Parameters**:
- `id` (UUID): Device unique identifier

**Request Headers**:
```
Content-Type: application/json
```

**Request Body** (all fields optional):
```json
{
  "state": "INACTIVE"
}
```

**Success Response**:

**Status**: `200 OK`

```json
{
  "id": "7f3d5e9c-4a1b-4c8d-9e2f-1a3b5c7d9e0f",
  "name": "MacBook Pro 16",
  "brand": "Apple",
  "state": "INACTIVE",
  "creationTime": "2025-10-24T14:30:45"
}
```

**Error Responses**:

Same as [Full Update Device](#full-update-device), plus:

**Status**: `400 Bad Request` - Empty request body
```json
{
  "timestamp": "2025-10-24T14:55:00",
  "status": 400,
  "error": "Bad Request",
  "message": "At least one field must be provided for update",
  "path": "/api/v1/devices/7f3d5e9c-4a1b-4c8d-9e2f-1a3b5c7d9e0f",
  "details": null
}
```

**Example cURL**:

Update only state:
```bash
curl -X PATCH http://localhost:8080/device-domain/api/v1/devices/7f3d5e9c-4a1b-4c8d-9e2f-1a3b5c7d9e0f \
  -H "Content-Type: application/json" \
  -d '{
    "state": "INACTIVE"
  }'
```

Update name and brand (device must not be IN_USE):
```bash
curl -X PATCH http://localhost:8080/device-domain/api/v1/devices/7f3d5e9c-4a1b-4c8d-9e2f-1a3b5c7d9e0f \
  -H "Content-Type: application/json" \
  -d '{
    "name": "MacBook Pro 16 M3",
    "brand": "Apple"
  }'
```

**Business Rules**:
1. **Null fields are ignored** - Only provided fields are updated
2. **Same validation as full update** - Cannot update name/brand if IN_USE
3. **State transitions validated** - If state provided, must be valid transition
4. **Creation time immutable** - Always preserved
5. **At least one field required** - Cannot send empty body

**Use Cases**:
- Change device state without affecting name/brand
- Update name/brand for AVAILABLE or INACTIVE devices
- Flexible updates when not all fields need changing

---

### Delete Device

Deletes a device record permanently.

**Endpoint**: `DELETE /api/v1/devices/{id}`

**Path Parameters**:
- `id` (UUID): Device unique identifier

**Success Response**:

**Status**: `204 No Content`

No response body.

**Error Responses**:

**Status**: `404 Not Found`
```json
{
  "timestamp": "2025-10-24T15:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Device not found with id: 7f3d5e9c-4a1b-4c8d-9e2f-1a3b5c7d9e0f",
  "path": "/api/v1/devices/7f3d5e9c-4a1b-4c8d-9e2f-1a3b5c7d9e0f",
  "details": null
}
```

**Status**: `409 Conflict` - Device in use
```json
{
  "timestamp": "2025-10-24T15:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Device is currently in use and cannot be deleted: 7f3d5e9c-4a1b-4c8d-9e2f-1a3b5c7d9e0f",
  "path": "/api/v1/devices/7f3d5e9c-4a1b-4c8d-9e2f-1a3b5c7d9e0f",
  "details": null
}
```

**Example cURL**:
```bash
curl -X DELETE http://localhost:8080/device-domain/api/v1/devices/7f3d5e9c-4a1b-4c8d-9e2f-1a3b5c7d9e0f
```

**Business Rules**:
1. **Device must exist** - Returns 404 if not found
2. **Cannot delete IN_USE devices** - Returns 409 conflict
3. **Can delete AVAILABLE devices** - Allowed
4. **Can delete INACTIVE devices** - Allowed
5. **Deletion is permanent** - No soft delete (see [FUTURE_IMPROVEMENTS.md](FUTURE_IMPROVEMENTS.md))

**Recommended Workflow**:
1. Check device state: `GET /api/v1/devices/{id}`
2. If IN_USE, change to AVAILABLE or INACTIVE: `PATCH /api/v1/devices/{id}`
3. Delete: `DELETE /api/v1/devices/{id}`

---

## Testing the API

### Using Swagger UI

The easiest way to test the API is through the interactive Swagger UI:

```
http://localhost:8080/device-domain/swagger-ui.html
```

Features:
- Try all endpoints interactively
- See request/response schemas
- View example payloads
- Get generated cURL commands

### Using cURL

Complete workflow example:

```bash
# 1. Create a device
DEVICE_ID=$(curl -s -X POST http://localhost:8080/device-domain/api/v1/devices \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Device",
    "brand": "TestBrand",
    "state": "AVAILABLE"
  }' | jq -r '.id')

echo "Created device: $DEVICE_ID"

# 2. Get the device
curl -X GET http://localhost:8080/device-domain/api/v1/devices/$DEVICE_ID

# 3. Update state to IN_USE
curl -X PATCH http://localhost:8080/device-domain/api/v1/devices/$DEVICE_ID \
  -H "Content-Type: application/json" \
  -d '{"state": "IN_USE"}'

# 4. Get devices by brand
curl -X GET http://localhost:8080/device-domain/api/v1/devices/brand/TestBrand

# 5. Update state back to AVAILABLE
curl -X PATCH http://localhost:8080/device-domain/api/v1/devices/$DEVICE_ID \
  -H "Content-Type: application/json" \
  -d '{"state": "AVAILABLE"}'

# 6. Delete the device
curl -X DELETE http://localhost:8080/device-domain/api/v1/devices/$DEVICE_ID
```

### Using Postman

Import the OpenAPI specification:

1. Open Postman
2. Import → Link: `http://localhost:8080/device-domain/v3/api-docs`
3. Collection will be created with all endpoints
4. Set base URL variable to: `http://localhost:8080/device-domain`

### Using HTTPie

```bash
# Create device
http POST http://localhost:8080/device-domain/api/v1/devices \
  name="Test Device" brand="TestBrand" state="AVAILABLE"

# Get all devices
http GET http://localhost:8080/device-domain/api/v1/devices

# Update device
http PATCH http://localhost:8080/device-domain/api/v1/devices/{id} \
  state="IN_USE"

# Delete device
http DELETE http://localhost:8080/device-domain/api/v1/devices/{id}
```

---

## API Evolution & Versioning

### Current Version: v1

Base path: `/api/v1/devices`

### Future Versions

When breaking changes are introduced, a new version will be created:

- `/api/v2/devices` - New version with breaking changes
- `/api/v1/devices` - Maintained for backwards compatibility

**Versioning Strategy**:
- URL path versioning (current approach)
- Maintain previous versions for deprecation period
- Provide migration guides for breaking changes

**Non-Breaking Changes** (no version bump):
- Adding new optional fields
- Adding new endpoints
- Adding new query parameters

**Breaking Changes** (version bump required):
- Removing fields
- Renaming fields
- Changing field types
- Changing response structure
- Changing validation rules

---

## Rate Limiting

**Current**: No rate limiting implemented

**Future**: See [FUTURE_IMPROVEMENTS.md](FUTURE_IMPROVEMENTS.md) for rate limiting plans.

---

## CORS Configuration

**Current**: CORS is not configured (defaults to same-origin only)

**Future**: When frontend is added, CORS will be configured in `infrastructure/config/CorsConfig.java`

---

## Monitoring & Observability

### Health Check Endpoint

```bash
curl http://localhost:8080/device-domain/actuator/health
```

Response:
```json
{
  "status": "UP"
}
```

### Application Info

```bash
curl http://localhost:8080/device-domain/actuator/info
```

### Additional Actuator Endpoints

See Spring Boot Actuator documentation for more endpoints that can be enabled for monitoring, metrics, and tracing.

---

## API Best Practices

### 1. Always Validate Input

The API validates all inputs, but clients should also validate before sending:
- Non-blank strings for name and brand
- Valid UUID format for IDs
- Valid DeviceState enum values

### 2. Handle All Error Codes

Implement proper error handling for all possible status codes:
- 400: Show validation errors to user
- 404: Resource not found, handle gracefully
- 409: Conflict, inform user of state issue
- 500: Unexpected error, log and retry

### 3. Use Idempotent Operations

- GET, PUT, DELETE are idempotent
- POST is not idempotent (creates new resource each time)
- Use PUT for updates when you want idempotency

### 4. Respect State Transitions

Always check current state before attempting updates:
1. GET device to check state
2. Validate intended operation is allowed
3. Perform update/delete

### 5. Use PATCH for Partial Updates

When only updating one field, use PATCH instead of PUT:
- More efficient
- Clearer intent
- Avoids overwriting other fields

---

## Changelog

### Version 1.0.0 (Current)

**Features**:
- CRUD operations for devices
- Query by brand and state
- Partial updates (PATCH)
- State transition validation
- Business rule enforcement
- Comprehensive error handling

**Known Limitations**:
- No pagination
- No authentication
- No rate limiting
- No soft deletes
- No audit trail

See [FUTURE_IMPROVEMENTS.md](FUTURE_IMPROVEMENTS.md) for planned enhancements.

---

## Support

For API issues or questions:
- Check [README.md](../README.md) for setup instructions
- Review [ARCHITECTURE.md](ARCHITECTURE.md) for design details
- See [CONTRIBUTING.md](../CONTRIBUTING.md) for development guidelines
- Open an issue on GitHub

---

**Last Updated**: 2025-10-24
**API Version**: 1.0.0
**Maintained By**: Leandro Massari
