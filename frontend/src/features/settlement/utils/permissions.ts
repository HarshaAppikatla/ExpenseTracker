import { SettlementResponse } from '../types';
import { GroupRole } from '../../group/types/group';

export const settlementPermissions = {
  canGenerate: (currentUserRole: GroupRole, isGroupArchived: boolean): boolean => {
    if (isGroupArchived) return false;
    return currentUserRole === 'OWNER' || currentUserRole === 'ADMIN';
  },

  canConfirm: (
    settlement: SettlementResponse,
    currentUserId: string,
    isGroupArchived: boolean
  ): boolean => {
    if (isGroupArchived) return false;
    return settlement.status === 'PENDING' && settlement.fromUserId === currentUserId;
  },

  canDispute: (
    settlement: SettlementResponse,
    currentUserId: string,
    isGroupArchived: boolean
  ): boolean => {
    if (isGroupArchived) return false;
    return settlement.status === 'PENDING' && settlement.toUserId === currentUserId;
  },

  canResolve: (
    settlement: SettlementResponse,
    currentUserRole: GroupRole,
    isGroupArchived: boolean
  ): boolean => {
    if (isGroupArchived) return false;
    const isAdminOrOwner = currentUserRole === 'OWNER' || currentUserRole === 'ADMIN';
    return settlement.status === 'DISPUTED' && isAdminOrOwner;
  },
};
