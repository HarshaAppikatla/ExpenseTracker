package com.expenseflow.expense.dto;

import com.expenseflow.expense.domain.valueobject.StorageProvider;
import java.time.LocalDateTime;

public record ExpenseAttachmentDto(
    String id,
    String url,
    String fileName,
    Long fileSize,
    String fileType,
    StorageProvider storageProvider,
    LocalDateTime createdAt,
    String createdBy
) {}
