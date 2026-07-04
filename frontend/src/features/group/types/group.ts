export type GroupRole = 'OWNER' | 'ADMIN' | 'MEMBER';
export type GroupMemberStatus = 'ACTIVE' | 'LEFT' | 'KICKED';
export type ActivityType =
  | 'GROUP_CREATED'
  | 'GROUP_UPDATED'
  | 'GROUP_ARCHIVED'
  | 'GROUP_RESTORED'
  | 'GROUP_DELETED'
  | 'MEMBER_JOINED'
  | 'MEMBER_LEFT'
  | 'MEMBER_REMOVED'
  | 'ROLE_CHANGED'
  | 'OWNER_TRANSFERRED';

export interface GroupSettingsDto {
  allowJoinByCode: boolean;
  allowJoinByLink: boolean;
  archived: boolean;
}

export interface GroupDto {
  id: string;
  name: string;
  description: string;
  currency: string;
  groupCode: string;
  joinLink: string;
  settings: GroupSettingsDto;
  ownerId: string;
  ownerName: string;
  role: GroupRole;
  version: number;
}

export interface GroupMemberDto {
  id: string;
  userId: string;
  userName: string;
  userEmail: string;
  role: GroupRole;
  joinedAt: string;
}

export interface GroupActivityDto {
  id: string;
  actionType: ActivityType;
  metadata: Record<string, any>;
  createdAt: string;
  createdBy: string;
}

export interface GroupDashboardDto {
  group: GroupDto;
  members: GroupMemberDto[];
  recentActivities: GroupActivityDto[];
  activeMemberCount: number;
}

export interface CreateGroupRequest {
  name: string;
  description: string;
  currency: string;
}

export interface UpdateGroupRequest {
  name: string;
  description: string;
  settings: GroupSettingsDto;
}

export interface JoinGroupRequest {
  roomCode: string;
}

export interface TransferOwnershipRequest {
  newOwnerId: string;
}

export interface UpdateMemberRoleRequest {
  role: 'ADMIN' | 'MEMBER';
}

export interface PaginatedResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

