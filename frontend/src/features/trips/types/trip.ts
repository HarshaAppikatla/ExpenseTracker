export interface DestinationDto {
  city: string;
  country: string;
  displayName: string;
}

export interface TripScheduleDto {
  startDate: string;
  endDate: string;
}

export interface TripSettingsDto {
  visibility: string;
  allowInvites: boolean;
  archived: boolean;
}

export interface TripParticipantDto {
  id: string;
  userId: string;
  userName: string;
  userEmail: string;
  status: 'INVITED' | 'ACCEPTED' | 'DECLINED' | 'LEFT' | 'REMOVED';
  joinedAt?: string;
  leftAt?: string;
}

export interface TripDto {
  id: string;
  groupId: string;
  title: string;
  description?: string;
  destination: DestinationDto;
  coverType: 'PRESET' | 'CUSTOM';
  coverImage?: string;
  schedule: TripScheduleDto;
  organizerId: string;
  organizerName: string;
  status: 'PLANNING' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED';
  settings: TripSettingsDto;
  version: number;
  participants: TripParticipantDto[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateTripRequest {
  groupId: string;
  title: string;
  description?: string;
  destination: DestinationDto;
  schedule: TripScheduleDto;
  settings?: TripSettingsDto;
  coverType?: string;
  coverImage?: string;
}

export interface UpdateTripRequest {
  title: string;
  description?: string;
  destination: DestinationDto;
  schedule: TripScheduleDto;
  settings?: TripSettingsDto;
  coverType?: string;
  coverImage?: string;
}

export interface TripActivityDto {
  id: string;
  tripId: string;
  activityType: string;
  actorUserId: string;
  actorName: string;
  targetUserId?: string;
  targetName?: string;
  message: string;
  metadataJson?: any;
  occurredAt: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}
