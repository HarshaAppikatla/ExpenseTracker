package com.expenseflow.expense.domain.entity;

import com.expenseflow.expense.domain.valueobject.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "expenses")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "group_id", length = 36, nullable = false)
    private String groupId;

    @Column(name = "trip_id", length = 36)
    private String tripId;

    @Column(name = "description", length = 255, nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 50, nullable = false)
    private ExpenseCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", length = 20, nullable = false)
    @Builder.Default
    private ExpenseCategoryType categoryType = ExpenseCategoryType.SYSTEM;

    @Embedded
    private Money money;

    @Column(name = "paid_by_user_id", length = 36, nullable = false)
    private String paidByUserId;

    @Column(name = "created_by_user_id", length = 36, nullable = false)
    private String createdByUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private ExpenseStatus status = ExpenseStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "split_type", length = 20, nullable = false)
    @Builder.Default
    private SplitType splitType = SplitType.EQUAL;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 36)
    private String deletedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ExpenseParticipantEntity> participants = new ArrayList<>();

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ExpenseSplitEntity> splits = new ArrayList<>();

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ExpenseAttachmentEntity> attachments = new ArrayList<>();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void assertEditable() {
        if (this.status != ExpenseStatus.DRAFT) {
            throw new IllegalStateException("Expense in status " + this.status + " cannot be modified");
        }
    }

    public void updateMetadata(String description, ExpenseCategory category, ExpenseCategoryType categoryType, LocalDate expenseDate, String paidByUserId, Money money, SplitType splitType) {
        assertEditable();
        if (this.status != ExpenseStatus.DRAFT && this.splitType != splitType) {
            throw new IllegalStateException("Split type cannot be changed after posting.");
        }
        this.description = description;
        this.category = category;
        this.categoryType = categoryType;
        this.expenseDate = expenseDate;
        this.paidByUserId = paidByUserId;
        this.money = money;
        this.splitType = splitType;
    }

    public void markDeleted(String userId) {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = userId;
    }

    public void touch(String userId) {
        this.updatedBy = userId;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateSplitsAndParticipants(Map<String, BigDecimal> calculatedOwedAmounts, Map<String, BigDecimal> allocationValues) {
        assertEditable();
        this.participants.clear();
        this.splits.clear();

        for (Map.Entry<String, BigDecimal> entry : calculatedOwedAmounts.entrySet()) {
            String userId = entry.getKey();
            BigDecimal owedAmount = entry.getValue();
            BigDecimal allocVal = allocationValues.get(userId);

            this.participants.add(ExpenseParticipantEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .expense(this)
                    .userId(userId)
                    .build());

            this.splits.add(ExpenseSplitEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .expense(this)
                    .userId(userId)
                    .owedAmount(owedAmount)
                    .allocationValue(allocVal)
                    .build());
        }

        // Ensure payer is always a participant
        boolean payerIsParticipant = this.participants.stream().anyMatch(p -> p.getUserId().equals(this.paidByUserId));
        if (!payerIsParticipant) {
            this.participants.add(ExpenseParticipantEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .expense(this)
                    .userId(this.paidByUserId)
                    .build());
        }
    }

    public void transitionTo(ExpenseStatus newStatus) {
        if (this.status == ExpenseStatus.VOID) {
            throw new IllegalStateException("Expense is VOID and cannot transition to any other status");
        }
        if (this.status == ExpenseStatus.POSTED && newStatus != ExpenseStatus.VOID) {
            throw new IllegalStateException("POSTED expense can only transition to VOID");
        }
        this.status = newStatus;
    }

    public void addAttachment(String id, String url, String fileName, long fileSize, String fileType, StorageProvider provider, String createdBy) {
        assertEditable();
        this.attachments.add(ExpenseAttachmentEntity.builder()
                .id(id)
                .expense(this)
                .url(url)
                .fileName(fileName)
                .fileSize(fileSize)
                .fileType(fileType)
                .storageProvider(provider)
                .createdBy(createdBy)
                .build());
    }

    public void removeAttachment(String attachmentId) {
        assertEditable();
        this.attachments.removeIf(a -> a.getId().equals(attachmentId));
    }
}
