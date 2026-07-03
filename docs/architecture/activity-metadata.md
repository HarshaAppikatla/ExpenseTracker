# Group Activity Metadata JSON Schema Contract

This document defines the structured JSON payload contracts for the `metadata` column in the `group_activity` table, categorized by `ActivityType`.

---

## 1. Membership Events
Applies to: `MEMBER_JOINED`, `MEMBER_LEFT`, `MEMBER_REMOVED` (Kicked).

### Schema
```json
{
  "actorId": "String (UUID)",       -- The user who triggered the action
  "actorName": "String",            -- Cache of the actor's full name at event time
  "targetUserId": "String (UUID)",  -- The user affected by the action
  "targetUserName": "String"        -- Cache of the target's full name at event time
}
```

### Examples
*   **Member Joined:**
    ```json
    {
      "actorId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
      "actorName": "John Doe",
      "targetUserId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
      "targetUserName": "John Doe"
    }
    ```
*   **Member Removed (Kick):**
    ```json
    {
      "actorId": "f47ac10b-58cc-4372-a567-0e02b2c3d479", -- Admin/Owner doing the kicking
      "actorName": "Jane Admin",
      "targetUserId": "d9b23b3a-5c21-4f11-9e8c-529a67a84012", -- Member being kicked
      "targetUserName": "Bob Member"
    }
    ```

---

## 2. Role Change Events
Applies to: `ROLE_CHANGED`.

### Schema
```json
{
  "actorId": "String (UUID)",
  "actorName": "String",
  "targetUserId": "String (UUID)",
  "targetUserName": "String",
  "oldRole": "String (GroupRole Enum)",
  "newRole": "String (GroupRole Enum)"
}
```

### Example
```json
{
  "actorId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "actorName": "Jane Owner",
  "targetUserId": "d9b23b3a-5c21-4f11-9e8c-529a67a84012",
  "targetUserName": "Bob Member",
  "oldRole": "MEMBER",
  "newRole": "ADMIN"
}
```

---

## 3. Ownership Transfer Events
Applies to: `OWNER_TRANSFERRED`.

### Schema
```json
{
  "actorId": "String (UUID)",       -- Previous owner
  "actorName": "String",
  "targetUserId": "String (UUID)",  -- New owner
  "targetUserName": "String"
}
```

### Example
```json
{
  "actorId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "actorName": "Jane FormerOwner",
  "targetUserId": "d9b23b3a-5c21-4f11-9e8c-529a67a84012",
  "targetUserName": "Bob NewOwner"
}
```

---

## 4. Group Lifecycle Events
Applies to: `GROUP_CREATED`, `GROUP_UPDATED`, `GROUP_ARCHIVED`, `GROUP_RESTORED`, `GROUP_DELETED`.

### Schema
```json
{
  "actorId": "String (UUID)",
  "actorName": "String"
}
```

### Example
```json
{
  "actorId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "actorName": "Jane Owner"
}
```
