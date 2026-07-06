# Trips Bounded Context: Architecture Diagrams

This document contains Mermaid diagrams visualizing the architecture, context map, and state machines for the Trips bounded context (Sprint 05).

---

## 1. Context Map & Monolith Communication Boundary

As defined in **ADR-002**, there are no direct database dependencies or repository injections between the `Groups` and `Trips` contexts. Cross-context validation is performed via services.

```mermaid
graph TD
    subgraph "Groups Bounded Context"
        G_Controller[GroupController] --> G_Service[GroupQueryService]
        G_Entity[GroupEntity]
    end

    subgraph "Trips Bounded Context"
        T_Controller[TripController] --> T_CmdService[TripCommandService]
        T_Controller --> T_QueryService[TripQueryService]
        
        T_CmdService --> T_Rules[TripBusinessRuleService]
        T_CmdService --> T_Validator[TripValidator]
        T_CmdService --> T_Repo[TripRepository]
        T_CmdService --> TA_Repo[TripActivityRepository]
        
        T_Repo --> T_Entity[TripEntity]
        TA_Repo --> TA_Entity[TripActivityEntity]
        
        T_Entity --> TP_Entity[TripParticipantEntity]
    end

    %% Monolith Cross-Context Boundaries
    T_Rules -.->|invokes| G_Service
```

---

## 2. Trip Lifecycle State Machine

A trip progresses through a series of mutually exclusive status states. Once a trip is `COMPLETED` or `CANCELLED`, it becomes read-only and no further state transitions or metadata modifications are permitted.

```mermaid
stateDiagram-v2
    [*] --> PLANNING : Create Trip
    
    PLANNING --> ACTIVE : Start Trip (organizer only)
    PLANNING --> CANCELLED : Cancel Trip (organizer only)
    
    ACTIVE --> COMPLETED : Complete Trip (organizer only)
    
    COMPLETED --> [*] : Read-only Workspace
    CANCELLED --> [*] : Read-only Workspace
```

---

## 3. Participant Lifecycle State Machine

Participant states are scoped inside the Trip aggregate. Status transitions are managed via `ParticipantCommandService` mutations.

```mermaid
stateDiagram-v2
    [*] --> INVITED : Invite member
    
    INVITED --> ACCEPTED : Accept invitation (participant)
    INVITED --> DECLINED : Decline invitation (participant)
    
    ACCEPTED --> LEFT : Leave trip (non-organizer participant)
    ACCEPTED --> REMOVED : Remove participant (organizer only)
    
    DECLINED --> [*]
    LEFT --> [*]
    REMOVED --> [*]
```
