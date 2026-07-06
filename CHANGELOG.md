# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.6.0-trip-module] - 2026-07-04

### Added
- **Trips Bounded Context**: Fully implemented backend architecture for Trip workspaces.
- **Database Schema**: V7 Flyway migration with tables for `trips`, `trip_participants`, and `trip_activities`.
- **Aggregate Root Invariants**: Comprehensive business rules in `TripEntity` (e.g., date ordering, organizer rules, immutable state states).
- **Concurrency Protection**: Optimistic locking concurrency strategy on the aggregate root using `@Version`.
- **Append-only Timeline**: Immutable logging of all trip status and participant modifications in `TripActivityEntity`.
- **REST Endpoints**: Resource-oriented controller with PATCH and POST actions.
- **Frontend Workspace**: React components (`TripList`, `TripCard`, `CreateTripDialog`, `TripParticipantList`, `TripTimeline`) integrated inside the `GroupDetailPage` tab layout.

### Fixed
- Unmapped target warning in MapStruct `TripMapper`.
- Participant status compilation errors (replaced `JOINED` status with `ACCEPTED`).

## [0.5.0-group-management] - 2026-07-03

### Added
- Collaborative group workspaces.
- Member lists, role badges, and room invitation codes.
- Group activity logger.
