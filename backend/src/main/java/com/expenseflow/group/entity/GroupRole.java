package com.expenseflow.group.entity;

public enum GroupRole {
    OWNER,
    ADMIN,
    MEMBER;

    /**
     * Checks if this role meets or exceeds the required target role.
     */
    public boolean hasPermissionOf(GroupRole requiredRole) {
        if (requiredRole == OWNER) {
            return this == OWNER;
        }
        if (requiredRole == ADMIN) {
            return this == OWNER || this == ADMIN;
        }
        return true; // MEMBER is the base level and meets MEMBER requirement
    }
}
