# API Contract: Trip Management v1

This document freezes the REST API payload structures, response models, query parameters, validation rules, and error codes for Sprint 05.

All endpoints are prefix-guarded: `/api/v1`

---

## 1. Unified Response Envelope
All API responses wrap their results inside the standardized `ApiResponse` structure.

---

## 2. API Endpoints

### 2.1 Create Trip Workspace
*   **Method & Path:** `POST /api/v1/trips`
*   **Request Payload (`application/json`):**
    ```json
    {
      "groupId": "group-12345",
      "title": "Summer Holiday in Rome",
      "description": "Exploration of Rome history, food, and culture",
      "destination": {
        "city": "Rome",
        "country": "Italy",
        "displayName": "Rome, Italy"
      },
      "schedule": {
        "startDate": "2026-08-10",
        "endDate": "2026-08-18"
      },
      "settings": {
        "visibility": "GROUP",
        "allowInvites": true
      },
      "coverType": "PRESET",
      "coverImage": "linear-gradient(135deg, #f6d365 0%, #fda085 100%)"
    }
    ```
*   **Response Payload (`201 Created`):**
    ```json
    {
      "success": true,
      "message": "Trip created successfully.",
      "code": null,
      "data": {
        "id": "trip-56789",
        "groupId": "group-12345",
        "title": "Summer Holiday in Rome",
        "description": "Exploration of Rome history, food, and culture",
        "destination": {
          "city": "Rome",
          "country": "Italy",
          "displayName": "Rome, Italy"
        },
        "coverType": "PRESET",
        "coverImage": "linear-gradient(135deg, #f6d365 0%, #fda085 100%)",
        "schedule": {
          "startDate": "2026-08-10",
          "endDate": "2026-08-18"
        },
        "organizerId": "user-111",
        "organizerName": "John Doe",
        "status": "PLANNING",
        "settings": {
          "visibility": "GROUP",
          "allowInvites": true,
          "archived": false
        },
        "version": 0,
        "participants": [
          {
            "id": "part-111",
            "userId": "user-111",
            "userName": "John Doe",
            "userEmail": "john@example.com",
            "status": "ACCEPTED",
            "joinedAt": "2026-07-04T12:00:00Z"
          }
        ],
        "createdAt": "2026-07-04T12:00:00Z",
        "updatedAt": "2026-07-04T12:00:00Z"
      },
      "timestamp": "2026-07-04T12:00:00.123Z"
    }
    ```

---

### 2.2 Update Trip Details (Metadata)
*   **Method & Path:** `PATCH /api/v1/trips/{id}`
*   **Request Payload (`application/json`):**
    ```json
    {
      "title": "Extended Rome Vacation",
      "description": "Extended exploration of Rome history, food, and culture",
      "destination": {
        "city": "Rome",
        "country": "Italy",
        "displayName": "Rome, Italy"
      },
      "schedule": {
        "startDate": "2026-08-10",
        "endDate": "2026-08-20"
      },
      "settings": {
        "visibility": "GROUP",
        "allowInvites": true
      },
      "coverType": "CUSTOM",
      "coverImage": "https://images.unsplash.com/photo-1552832230-c0197dd311b5"
    }
    ```
*   **Response Payload (`200 OK`):**
    ```json
    {
      "success": true,
      "message": "Trip updated successfully.",
      "code": null,
      "data": { ... }
    }
    ```

---

### 2.3 Transition Trip Status
*   **Method & Path:** `PATCH /api/v1/trips/{id}/status`
*   **Request Payload (`application/json`):**
    ```json
    {
      "status": "ACTIVE"
    }
    ```
*   **Response Payload (`200 OK`):**
    ```json
    {
      "success": true,
      "message": "Trip status updated successfully.",
      "code": null,
      "data": {
        ...
        "status": "ACTIVE"
      }
    }
    ```

---

### 2.4 Soft-Delete Trip
*   **Method & Path:** `DELETE /api/v1/trips/{id}`
*   **Response Payload (`200 OK`):**
    ```json
    {
      "success": true,
      "message": "Trip soft deleted successfully.",
      "code": null,
      "data": null
    }
    ```

---

### 2.5 Fetch Single Trip Details
*   **Method & Path:** `GET /api/v1/trips/{id}`
*   **Response Payload (`200 OK`):**
    ```json
    {
      "success": true,
      "message": "Trip details retrieved successfully.",
      "code": null,
      "data": { ... }
    }
    ```

---

### 2.6 List Group Trips (Paginated)
*   **Method & Path:** `GET /api/v1/groups/{groupId}/trips?page=0&size=10`
*   **Response Payload (`200 OK`):**
    ```json
    {
      "success": true,
      "message": "Group trips listed successfully.",
      "code": null,
      "data": {
        "content": [
          { ... }
        ],
        "totalPages": 1,
        "totalElements": 1,
        "size": 10,
        "number": 0
      }
    }
    ```

---

### 2.7 Invite Participant
*   **Method & Path:** `POST /api/v1/trips/{id}/participants/invite`
*   **Request Payload (`application/json`):**
    ```json
    {
      "userId": "user-222"
    }
    ```
*   **Response Payload (`200 OK`):**
    ```json
    {
      "success": true,
      "message": "Participant invited successfully.",
      "code": null,
      "data": { ... }
    }
    ```

---

### 2.8 Accept Invitation
*   **Method & Path:** `POST /api/v1/trips/{id}/participants/accept`
*   **Response Payload (`200 OK`):**
    ```json
    {
      "success": true,
      "message": "Invitation accepted successfully.",
      "code": null,
      "data": { ... }
    }
    ```

---

### 2.9 Decline Invitation
*   **Method & Path:** `POST /api/v1/trips/{id}/participants/decline`
*   **Response Payload (`200 OK`):**
    ```json
    {
      "success": true,
      "message": "Invitation declined successfully.",
      "code": null,
      "data": { ... }
    }
    ```

---

### 2.10 Leave Trip
*   **Method & Path:** `POST /api/v1/trips/{id}/participants/leave`
*   **Response Payload (`200 OK`):**
    ```json
    {
      "success": true,
      "message": "Left the trip successfully.",
      "code": null,
      "data": { ... }
    }
    ```

---

### 2.11 Kick/Remove Participant
*   **Method & Path:** `DELETE /api/v1/trips/{id}/participants/{userId}`
*   **Response Payload (`200 OK`):**
    ```json
    {
      "success": true,
      "message": "Participant removed successfully.",
      "code": null,
      "data": { ... }
    }
    ```

---

### 2.12 Fetch Trip Timeline (Append-Only Logs)
*   **Method & Path:** `GET /api/v1/trips/{id}/timeline?page=0&size=20`
*   **Response Payload (`200 OK`):**
    ```json
    {
      "success": true,
      "message": "Trip timeline retrieved successfully.",
      "code": null,
      "data": {
        "content": [
          {
            "id": "log-abcde",
            "tripId": "trip-56789",
            "activityType": "PARTICIPANT_JOINED",
            "actorUserId": "user-222",
            "actorName": "Jane Smith",
            "targetUserId": null,
            "targetName": null,
            "message": "Jane Smith joined the trip",
            "occurredAt": "2026-07-04T12:05:00Z"
          }
        ],
        "totalPages": 1,
        "totalElements": 1,
        "size": 20,
        "number": 0
      }
    }
    ```

---

## 3. Error Codes

| Code | HTTP Status | Description |
|---|---|---|
| `TRIP_NOT_FOUND` | `404 Not Found` | The specified trip does not exist or has been deleted. |
| `INVALID_TRIP_STATE` | `400 Bad Request` | Transition requested or update action is invalid for the current status. |
| `PARTICIPANT_CONFLICT` | `409 Conflict` | Participant is already invited/active, or invalid duplicate action. |
| `PERMISSION_DENIED` | `403 Forbidden` | The requesting user does not have permission to execute this action. |
