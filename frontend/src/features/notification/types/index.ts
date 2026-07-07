export type NotificationStatus = 'UNREAD' | 'READ';

export type NotificationPriority = 'LOW' | 'NORMAL' | 'HIGH' | 'CRITICAL';

export type NotificationCategory = 'SYSTEM' | 'GROUP' | 'TRIP' | 'EXPENSE' | 'SETTLEMENT';

export type NotificationType =
  | 'BUDGET_LIMIT_EXCEEDED'
  | 'BUDGET_WARNING'
  | 'RECURRING_EXECUTION'
  | 'RECURRING_EXECUTION_FAILED'
  | 'SAVINGS_GOAL_COMPLETED'
  | 'EXPENSE_POSTED'
  | 'SETTLEMENT_GENERATED'
  | 'SETTLEMENT_CONFIRMED'
  | 'SETTLEMENT_DISPUTED'
  | 'MEMBER_JOINED';

export interface Notification {
  id: string;
  title: string;
  message: string;
  status: NotificationStatus;
  priority: NotificationPriority;
  category: NotificationCategory;
  type: NotificationType;
  expenseId?: string;
  groupId?: string;
  tripId?: string;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
