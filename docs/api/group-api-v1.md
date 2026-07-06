# API Contract: Group Management v1

This document freezes the REST API payload structures, response models, query parameters, validation rules, and error codes for Sprint 04.

All endpoints are prefix-guarded: `/api/v1`

---

## 1. Unified Response Envelope
All API responses must wrap their results inside the standardized `ApiResponse` structure.

### 1.1 Success Envelope
```json
{
  "success": true,
  "message": "Operation completed successfully.",
  "code": null,
  "data": { ... },
  "timestamp": "2026-07-03T19:30:00.123Z"
}
```

### 1.2 Error Envelope
```json
{
  "success": false,
  "message": "Owner cannot leave the group. You must transfer ownership first.",
  "code": "GROUP_202",
  "data": null,
  "timestamp": "2026-07-03T19:36:00.123Z"
}
```

---

## 2. API Endpoints

### 2.1 Create Group
*   **Method & Path:** `POST /api/v1/groups`
*   **Request Payload (`application/json`):**
    ```json
    {
      "name": "Goa Trip 2026",
      "description": "Shared expenses for Goa vacation",
      "currency": "INR"
    }
    ```
*   **Response Payload (`201 Created`):**
    ```json
    {
      "success": true,
      "message": "Group created successfully.",
      "code": null,
      "data": {
        "id": "e8c105a0-21d1-48ed-856f-ce0a1e0a73a3",
        "name": "Goa Trip 2026",
        "description": "Shared expenses for Goa vacation",
        "currency": "INR",
        "groupCode": "X7KD4Q2M",
        "isOwner": true,
        "role": "OWNER",
        "settings": {
          "allowJoinByCode": true,
          "allowJoinByLink": true,
          "archived": false
        },
        "createdAt": "2026-07-03T19:30:00Z"
      },
      "timestamp": "2026-07-03T19:30:00.123Z"
    }
    ```

---

### 2.2 List Active Groups (Paginated)
*   **Method & Path:** `GET /api/v1/groups?page=0&size=10&sort=createdAt&direction=desc&search=Goa`
*   **Response Payload (`200 OK`):**
    ```json
    {
      "success": true,
      "message": "Groups listed successfully.",
      "code": null,
      "data": {
        "content": [
          {
            "id": "e8c105a0-21d1-48ed-856f-ce0a1e0a73a3",
            "name": "Goa Trip 2026",
            "description": "Shared expenses for Goa vacation",
            "currency": "INR",
            "groupCode": "X7KD4Q2M",
            "isOwner": true,
            "role": "OWNER",
            "settings": {
              "allowJoinByCode": true,
              "allowJoinByLink": true,
              "archived": false
            },
            "createdAt": "2026-07-03T19:30:00Z"
          }
        ],
        "pageable": {
          "pageNumber": 0,
          "pageSize": 10
        },
        "totalPages": 1,
        "totalElements": 1,
        "last": true
      },
      "timestamp": "2026-07-03T19:30:05.123Z"
    }
    ```

---

### 2.3 Get Group Details
*   **Method & Path:** `GET /api/v1/groups/{id}`
*   **Response Payload (`200 OK`):**
    ```json
    {
      "success": true,
      "message": "Group details retrieved successfully.",
      "code": null,
      "data": {
        "id": "e8c105a0-21d1-48ed-856f-ce0a1e0a73a3",
        "name": "Goa Trip 2026",
        "description": "Shared expenses for Goa vacation",
        "currency": "INR",
        "groupCode": "X7KD4Q2M",
        "isOwner": true,
        "role": "OWNER",
        "settings": {
          "allowJoinByCode": true,
          "allowJoinByLink": true,
          "archived": false
        },
        "createdAt": "2026-07-03T19:30:00Z"
      },
      "timestamp": "2026-07-03T19:30:06.123Z"
    }
    ```

---

### 2.4 Partial Update Group (PATCH)
*   **Method & Path:** `PATCH /api/v1/groups/{id}`
*   **Request Payload (`application/json`):**
    ```json
    {
      "name": "Goa Trip 2026 (Updated)",
      "description": "Updated group description",
      "settings": {
        "allowJoinByCode": false,
        "allowJoinByLink": true
      }
    }
    ```
*   **Response Payload (`200 OK`):**
    ```json
    {
      "success": true,
      "message": "Group settings updated successfully.",
      "code": null,
      "data": {
        "id": "e8c105a0-21d1-48ed-856f-ce0a1e0a73a3",
        "name": "Goa Trip 2026 (Updated)",
        "description": "Updated group description",
        "currency": "INR",
        "groupCode": "X7KD4Q2M",
        "isOwner": true,
        "role": "OWNER",
        "settings": {
          "allowJoinByCode": false,
          "allowJoinByLink": true,
          "archived": false
        },
        "createdAt": "2026-07-03T19:30:00Z"
      },
      "timestamp": "2026-07-03T19:30:10.123Z"
    }
    ```

---

### 2.5 Archive Group (PATCH)
*   **Method & Path:** `PATCH /api/v1/groups/{id}/archive`
*   **Response Payload (`200 OK`):**
    ```json
    {
      "success": true,
      "message": "Group archived successfully.",
      "code": null,
      "data": {
        "id": "e8c105a0-21d1-48ed-856f-ce0a1e0a73a3",
        "settings": {
          "allowJoinByCode": false,
          "allowJoinByLink": false,
          "archived": true
        }
      },
      "timestamp": "2026-07-03T19:30:12.123Z"
    }
    ```

---

### 2.6 Restore Group (PATCH)
*   **Method & Path:** `PATCH /api/v1/groups/{id}/restore`
*   **Response Payload (`200 OK`):**
    ```json
    {
      "success": true,
      "message": "Group restored successfully.",
      "code": null,
      "data": {
        "id": "e8c105a0-21d1-48ed-856f-ce0a1e0a73a3",
        "settings": {
          "allowJoinByCode": true,
          "allowJoinByLink": true,
          "archived": false
        }
      },
      "timestamp": "2026-07-03T19:30:14.123Z"
    }
    ```

---

### 2.7 Delete Group (Soft Delete)
*   **Method & Path:** `DELETE /api/v1/groups/{id}`
*   **Response Payload (`200 OK`):**
    ```json
    {
      "success": true,
      "message": "Group deleted successfully.",
      "code": null,
      "data": null,
      "timestamp": "2026-07-03T19:30:15.123Z"
    }
    ```

---

### 2.8 Join Group via Room Code
*   **Method & Path:** `POST /api/v1/groups/join`
*   **Request Payload (`application/json`):**
    ```json
    {
      "roomCode": "X7KD4Q2M"
    }
    ```
*   **Response Payload (`200 OK`):**
    ```json
    {
      "success": true,
      "message": "Joined group successfully.",
      "code": null,
      "data": {
        "groupId": "e8c105a0-21d1-48ed-856f-ce0a1e0a73a3",
        "role": "MEMBER",
        "joinedAt": "2026-07-03T19:35:00Z"
      },
      "timestamp": "2026-07-03T19:35:00.123Z"
    }
    ```

---

### 2.9 Leave Group
*   **Method & Path:** `POST /api/v1/groups/{id}/leave`
*   **Response Payload (`200 OK`):**
    ```json
    {
      "success": true,
      "message": "Left group successfully.",
      "code": null,
      "data": null,
      "timestamp": "2026-07-03T19:35:10.123Z"
    }
    ```

---

### 2.10 List Group Members (Paginated)
*   **Method & Path:** `GET /api/v1/groups/{id}/members?page=0&size=10&sort=role&direction=asc`
*   **Response Payload (`200 OK`):**
    ```json
    {
      "success": true,
      "message": "Members retrieved successfully.",
      "code": null,
      "data": {
        "content": [
          {
            "userId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
            "name": "Jane Owner",
            "email": "owner@example.com",
            "role": "OWNER",
            "joinedAt": "2026-07-03T19:30:00Z"
          }
        ],
        "pageable": {
          "pageNumber": 0,
          "pageSize": 10
        },
        "totalPages": 1,
        "totalElements": 1,
        "last": true
      },
      "timestamp": "2026-07-03T19:35:12.123Z"
    }
    ```

---

### 2.11 Update Member Role
*   **Method & Path:** `PATCH /api/v1/groups/{id}/members/{userId}/role`
*   **Request Payload (`application/json`):**
    ```json
    {
      "role": "ADMIN"
    }
    ```
*   **Response Payload (`200 OK`):**
    ```json
    {
      "success": true,
      "message": "Member role updated successfully.",
      "code": null,
      "data": {
        "groupId": "e8c105a0-21d1-48ed-856f-ce0a1e0a73a3",
        "userId": "d9b23b3a-5c21-4f11-9e8c-529a67a84012",
        "role": "ADMIN"
      },
      "timestamp": "2026-07-03T19:35:15.123Z"
    }
    ```

---

### 2.12 Kick Member
*   **Method & Path:** `DELETE /api/v1/groups/{id}/members/{userId}`
*   **Response Payload (`200 OK`):**
    ```json
    {
      "success": true,
      "message": "Member removed from group successfully.",
      "code": null,
      "data": null,
      "timestamp": "2026-07-03T19:35:20.123Z"
    }
    ```

---

### 2.13 Transfer Group Ownership
*   **Method & Path:** `POST /api/v1/groups/{id}/transfer-ownership`
*   **Request Payload (`application/json`):**
    ```json
    {
      "newOwnerId": "d9b23b3a-5c21-4f11-9e8c-529a67a84012"
    }
    ```
*   **Response Payload (`200 OK`):**
    ```json
    {
      "success": true,
      "message": "Ownership transferred successfully.",
      "code": null,
      "data": {
        "groupId": "e8c105a0-21d1-48ed-856f-ce0a1e0a73a3",
        "newOwnerRole": "ADMIN"
      },
      "timestamp": "2026-07-03T19:35:25.123Z"
    }
    ```

---

### 2.14 Fetch Group Timeline Activity (Paginated)
*   **Method & Path:** `GET /api/v1/groups/{id}/activity?page=0&size=10&sort=createdAt&direction=desc`
*   **Response Payload (`200 OK`):**
    ```json
    {
      "success": true,
      "message": "Activity history retrieved successfully.",
      "code": null,
      "data": {
        "content": [
          {
            "id": "c7a105a0-21d1-48ed-856f-ce0a1e0a73b4",
            "actionType": "MEMBER_JOINED",
            "description": "John Doe joined the group via room code.",
            "createdAt": "2026-07-03T19:35:00Z"
          }
        ],
        "pageable": {
          "pageNumber": 0,
          "pageSize": 10
        },
        "totalPages": 1,
        "totalElements": 1,
        "last": true
      },
      "timestamp": "2026-07-03T19:35:30.123Z"
    }
    ```

---

## 3. API Idempotency Specification

| Endpoint | HTTP Method | Idempotent | Expected Repeated Request Result |
| :--- | :--- | :--- | :--- |
| `GET /api/v1/groups` | GET | ✅ Yes | Returns cached/current active groups list. |
| `GET /api/v1/groups/{id}` | GET | ✅ Yes | Returns cached/current group details. |
| `PATCH /api/v1/groups/{id}` | PATCH | ✅ Yes | Returns current updated settings state. |
| `PATCH /api/v1/groups/{id}/archive` | PATCH | ✅ Yes | Returns current archived settings state (archived = true). |
| `PATCH /api/v1/groups/{id}/restore` | PATCH | ✅ Yes | Returns current restored settings state (archived = false). |
| `POST /api/v1/groups` | POST | ❌ No | Creates a new duplicate group with new ID/Room Code. |
| `POST /api/v1/groups/join` | POST | ✅ Yes | Returns membership data (fails silently or returns cached membership details if already a member). |
| `POST /api/v1/groups/{id}/leave` | POST | ✅ Yes | Returns success even if called repeatedly (user remains in NOT_MEMBER/LEFT state). |
| `POST /api/v1/groups/{id}/transfer-ownership` | POST | ❌ No | Transfers ownership (returns 400/403 on retry as user is no longer the Owner). |
| `DELETE /api/v1/groups/{id}` | DELETE | ✅ Yes | Returns 200 OK / 204 No Content (soft delete filter active, resource deleted). |
| `DELETE /api/v1/groups/{id}/members/{userId}` | DELETE | ✅ Yes | Returns success (user is removed from group on first call, subsequent calls verify user is not a member). |

---

## 4. Standard Error Mappings

*   **GROUP_001 – GROUP_099 (Validation)**
    *   `GROUP_001`: Invalid Group Name
    *   `GROUP_002`: Invalid Currency Code
*   **GROUP_100 – GROUP_199 (Resource)**
    *   `GROUP_101`: Group Not Found
*   **GROUP_200 – GROUP_299 (Permission)**
    *   `GROUP_201`: Permission Denied
    *   `GROUP_202`: Owner Cannot Leave (must transfer ownership first)
*   **GROUP_300 – GROUP_399 (Access)**
    *   `GROUP_301`: Invalid or Disabled Room Code
    *   `GROUP_302`: Join By Link Disabled
*   **GROUP_900 – GROUP_999 (Internal Errors)**
    *   `GROUP_901`: Room Code Generation Collision Exhausted
