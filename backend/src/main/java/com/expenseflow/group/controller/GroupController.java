package com.expenseflow.group.controller;

import com.expenseflow.dto.ApiResponse;
import com.expenseflow.security.UserPrincipal;
import com.expenseflow.group.dto.*;
import com.expenseflow.group.facade.GroupFacade;
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
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@Tag(name = "Group Management", description = "Endpoints resolving group workflows, collaborations, member directories, and timeline activity feeds")
public class GroupController {

    private final GroupFacade groupFacade;

    @PostMapping
    @Operation(summary = "Create a new group")
    public ResponseEntity<ApiResponse<GroupDto>> createGroup(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateGroupRequest request) {
        GroupDto response = groupFacade.createGroup(request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Group created successfully", response));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update group profile settings")
    public ResponseEntity<ApiResponse<GroupDto>> updateGroup(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateGroupRequest request) {
        GroupDto response = groupFacade.updateGroup(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Group updated successfully", response));
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "Archive an active group")
    public ResponseEntity<ApiResponse<GroupDto>> archiveGroup(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id) {
        GroupDto response = groupFacade.archiveGroup(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Group archived successfully", response));
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Restore an archived group")
    public ResponseEntity<ApiResponse<GroupDto>> restoreGroup(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id) {
        GroupDto response = groupFacade.restoreGroup(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Group restored successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete a group")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id) {
        groupFacade.deleteGroup(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Group deleted successfully"));
    }

    @PostMapping("/join")
    @Operation(summary = "Join a group using an 8-character room code")
    public ResponseEntity<ApiResponse<GroupDto>> joinGroup(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody JoinGroupRequest request) {
        GroupDto response = groupFacade.joinGroup(request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Joined group successfully", response));
    }

    @PostMapping("/{id}/leave")
    @Operation(summary = "Leave a group membership")
    public ResponseEntity<ApiResponse<Void>> leaveGroup(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id) {
        groupFacade.leaveGroup(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Left group successfully"));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    @Operation(summary = "Remove (kick) a member from a group")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String groupId,
            @PathVariable("memberId") String memberId) {
        groupFacade.removeMember(groupId, memberId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully"));
    }

    @PatchMapping("/{id}/members/{memberId}/role")
    @Operation(summary = "Change a member's role (promote/demote)")
    public ResponseEntity<ApiResponse<Void>> updateMemberRole(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String groupId,
            @PathVariable("memberId") String memberId,
            @Valid @RequestBody UpdateMemberRoleRequest request) {
        if ("ADMIN".equalsIgnoreCase(request.role())) {
            groupFacade.promoteMember(groupId, memberId, principal.getId());
        } else if ("MEMBER".equalsIgnoreCase(request.role())) {
            groupFacade.demoteMember(groupId, memberId, principal.getId());
        } else {
            throw new IllegalArgumentException("Invalid role request: role must be ADMIN or MEMBER");
        }
        return ResponseEntity.ok(ApiResponse.success("Member role updated successfully"));
    }

    @PostMapping("/{id}/transfer-ownership")
    @Operation(summary = "Transfer group ownership to another active member")
    public ResponseEntity<ApiResponse<Void>> transferOwnership(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id,
            @Valid @RequestBody TransferOwnershipRequest request) {
        groupFacade.transferOwnership(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Ownership transferred successfully"));
    }

    @GetMapping
    @Operation(summary = "Get paginated active groups for the authenticated user")
    public ResponseEntity<ApiResponse<Page<GroupDto>>> getMyGroups(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(value = "search", required = false) String search,
            Pageable pageable) {
        Page<GroupDto> response = groupFacade.getMyGroups(principal.getId(), search, pageable);
        return ResponseEntity.ok(ApiResponse.success("Active groups retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get active group details profile")
    public ResponseEntity<ApiResponse<GroupDto>> getGroupDetails(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id) {
        GroupDto response = groupFacade.getGroupDetails(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Group details retrieved successfully", response));
    }

    @GetMapping("/{id}/dashboard")
    @Operation(summary = "Get group dashboard preview page details")
    public ResponseEntity<ApiResponse<GroupDashboardDto>> getGroupDashboard(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id) {
        GroupDashboardDto response = groupFacade.getGroupDashboard(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Group dashboard resolved successfully", response));
    }
}
