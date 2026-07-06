package com.expenseflow.trip.controller;

import com.expenseflow.dto.ApiResponse;
import com.expenseflow.security.UserPrincipal;
import com.expenseflow.trip.dto.*;
import com.expenseflow.trip.service.command.ParticipantCommandService;
import com.expenseflow.trip.service.command.TripCommandService;
import com.expenseflow.trip.service.query.TimelineQueryService;
import com.expenseflow.trip.service.query.TripQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Trip Management", description = "Endpoints resolving collaborative travel planning workspaces and participant/timeline feeds")
public class TripController {

    private final TripCommandService tripCommandService;
    private final ParticipantCommandService participantCommandService;
    private final TripQueryService tripQueryService;
    private final TimelineQueryService timelineQueryService;

    @PostMapping("/api/v1/trips")
    @Operation(summary = "Create a new trip collaborative workspace")
    public ResponseEntity<ApiResponse<TripDto>> createTrip(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateTripRequest request) {
        TripDto response = tripCommandService.createTrip(request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Trip created successfully", response));
    }

    @PatchMapping("/api/v1/trips/{id}")
    @Operation(summary = "Update trip metadata, cover details, schedule or settings")
    public ResponseEntity<ApiResponse<TripDto>> updateTrip(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateTripRequest request) {
        TripDto response = tripCommandService.updateTrip(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Trip details updated successfully", response));
    }

    @PatchMapping("/api/v1/trips/{id}/status")
    @Operation(summary = "Transition trip status state")
    public ResponseEntity<ApiResponse<TripDto>> updateTripStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateTripStatusRequest request) {
        TripDto response = tripCommandService.updateStatus(id, request.status(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Trip status updated successfully", response));
    }

    @DeleteMapping("/api/v1/trips/{id}")
    @Operation(summary = "Soft delete a trip workspace")
    public ResponseEntity<ApiResponse<Void>> deleteTrip(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id) {
        tripCommandService.deleteTrip(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Trip deleted successfully"));
    }

    @GetMapping("/api/v1/trips/{id}")
    @Operation(summary = "Retrieve complete details of a trip")
    public ResponseEntity<ApiResponse<TripDto>> getTripDetails(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id) {
        TripDto response = tripQueryService.getTripDetails(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Trip details retrieved successfully", response));
    }

    @GetMapping("/api/v1/groups/{groupId}/trips")
    @Operation(summary = "List all active trips inside a group workspace")
    public ResponseEntity<ApiResponse<Page<TripDto>>> getGroupTrips(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("groupId") String groupId,
            Pageable pageable) {
        Page<TripDto> response = tripQueryService.getGroupTrips(groupId, principal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Group trips retrieved successfully", response));
    }

    @PostMapping("/api/v1/trips/{id}/participants/invite")
    @Operation(summary = "Invite a member of the group to join the trip")
    public ResponseEntity<ApiResponse<TripDto>> inviteParticipant(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id,
            @Valid @RequestBody InviteParticipantRequest request) {
        TripDto response = participantCommandService.inviteParticipant(id, request.userId(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Participant invited successfully", response));
    }

    @PostMapping("/api/v1/trips/{id}/participants/accept")
    @Operation(summary = "Accept an invitation to join the trip")
    public ResponseEntity<ApiResponse<TripDto>> acceptInvite(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id) {
        TripDto response = participantCommandService.acceptInvite(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Invitation accepted successfully", response));
    }

    @PostMapping("/api/v1/trips/{id}/participants/decline")
    @Operation(summary = "Decline an invitation to join the trip")
    public ResponseEntity<ApiResponse<TripDto>> declineInvite(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id) {
        TripDto response = participantCommandService.declineInvite(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Invitation declined successfully", response));
    }

    @PostMapping("/api/v1/trips/{id}/participants/leave")
    @Operation(summary = "Leave the trip participation")
    public ResponseEntity<ApiResponse<TripDto>> leaveTrip(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id) {
        TripDto response = participantCommandService.leaveTrip(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Left trip successfully", response));
    }

    @DeleteMapping("/api/v1/trips/{id}/participants/{userId}")
    @Operation(summary = "Remove a participant from the trip")
    public ResponseEntity<ApiResponse<TripDto>> removeParticipant(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id,
            @PathVariable("userId") String userId) {
        TripDto response = participantCommandService.removeParticipant(id, userId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Participant removed successfully", response));
    }

    @GetMapping("/api/v1/trips/{id}/timeline")
    @Operation(summary = "Get the append-only timeline activity feed of a trip")
    public ResponseEntity<ApiResponse<Page<TripActivityDto>>> getTripTimeline(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id,
            Pageable pageable) {
        Page<TripActivityDto> response = timelineQueryService.getTripTimeline(id, principal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Trip timeline retrieved successfully", response));
    }
}
