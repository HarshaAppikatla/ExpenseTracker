package com.expenseflow.notification.service;

import com.expenseflow.notification.domain.valueobject.NotificationPriority;

/**
 * @deprecated This monolithic service has been split into
 * {@link NotificationCommandService} and {@link NotificationQueryService}
 * per ADR-006 §3.3. This class will be removed in a future sprint.
 *
 * <p>All callers should migrate:
 * <ul>
 *   <li>Mutations → {@link NotificationCommandService}</li>
 *   <li>Queries    → {@link NotificationQueryService}</li>
 * </ul>
 */
@Deprecated(forRemoval = true)
public class NotificationService {

    private NotificationService() {
        // Non-instantiable — use NotificationCommandService or NotificationQueryService
    }
}
