package com.expenseflow.expense.domain.entity;

import com.expenseflow.expense.domain.valueobject.StorageProvider;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "expense_attachments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseAttachmentEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private ExpenseEntity expense;

    @Column(name = "url", length = 255, nullable = false)
    private String url;

    @Column(name = "file_name", length = 100, nullable = false)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "file_type", length = 50, nullable = false)
    private String fileType;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_provider", length = 50, nullable = false)
    private StorageProvider storageProvider;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "created_by", length = 100)
    private String createdBy;
}
