# Sprint 05 — Trip Management Workspace

Version: 1.0

Release Version: v0.6.0-trip-module

Status: Completed

## Sprint Goal
Sprint 05 introduces collaborative Trip Management workspaces in ExpenseFlow. Since Trips cannot exist without a Group and belong to exactly one Group, this sprint integrates the Trip planning layer under the Group details view.

The primary objectives are:
1. Create collaborative Trip workspaces within a Group.
2. Manage trip participants and invitations.
3. Manage trip lifecycle states (PLANNING, ACTIVE, COMPLETED, CANCELLED).
4. Track trip-specific activity logs in an append-only timeline.
5. Provide a rich and responsive user interface for group travels.

## Sprint Scope
- **Database Schema**: V7__trip_management.sql Flyway migration with proper constraints and indexes.
- **Domain Value Objects**: Destination, TripSchedule, TripSettings, TripStatus, TripParticipantStatus.
- **Aggregate Root**: TripEntity preserving all aggregate invariants.
- **Repositories**: TripRepository, TripActivityRepository.
- **Services**: TripCommandService, ParticipantCommandService, TripQueryService, TimelineQueryService.
- **REST Controller**: TripController.
- **Frontend Components**: CreateTripDialog, TripCard, TripList, TripParticipantList, TripTimeline.
- **Frontend Integration**: Integrated Trips tab inside GroupDetailPage and mapped `/groups/:groupId/trips/:tripId` routes.

## Testing & QA
- **Aggregate Unit Tests**: Verified aggregate invariants like organizer restrictions, read-only completed trips, date ordering, and invalid status transitions.
- **Service & Event Tests**: Verified transactional commands and event publishing.
- **Integration Tests**: Added `TripConcurrencyIntegrationTest` to verify the `@Version` optimistic locking concurrency strategy.
