export type SettlementStatus = 'PENDING' | 'CONFIRMED' | 'DISPUTED';

export interface SettlementResponse {
  id: string;
  groupId: string;
  tripId?: string;
  fromUserId: string;
  fromUserName: string;
  toUserId: string;
  toUserName: string;
  amount: number;
  currency: string;
  status: SettlementStatus;
  settledAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface SettlementSummaryResponse {
  groupId: string;
  tripId?: string;
  currency: string;
  totalOutstanding: number;
  pendingCount: number;
  settlements: SettlementResponse[];
}

export interface ConfirmSettlementRequest {
  note?: string;
}

export interface DisputeSettlementRequest {
  reason: string;
}
