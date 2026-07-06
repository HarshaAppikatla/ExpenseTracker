package com.expenseflow.recurring.service;

import com.expenseflow.category.entity.CategoryEntity;
import com.expenseflow.category.repository.CategoryRepository;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.exception.SecurityHardeningException;
import com.expenseflow.expense.service.ExpenseCommandService;
import com.expenseflow.group.repository.GroupRepository;
import com.expenseflow.income.dto.IncomeRequest;
import com.expenseflow.income.dto.IncomeResponse;
import com.expenseflow.income.service.IncomeService;
import com.expenseflow.recurring.dto.RecurringDto;
import com.expenseflow.recurring.dto.RecurringRequest;
import com.expenseflow.recurring.entity.RecurringExecutionHistoryEntity;
import com.expenseflow.recurring.entity.RecurringStatus;
import com.expenseflow.recurring.entity.RecurringTransactionEntity;
import com.expenseflow.recurring.mapper.RecurringMapper;
import com.expenseflow.recurring.repository.RecurringExecutionHistoryRepository;
import com.expenseflow.recurring.repository.RecurringTransactionRepository;
import com.expenseflow.repository.UserRepository;
import com.expenseflow.core.calendar.FinancialCalendar;
import com.expenseflow.core.event.RecurringExecutionEvent;
import com.expenseflow.core.event.RecurringExecutionFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RecurringTransactionService {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final RecurringExecutionHistoryRepository executionHistoryRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseCommandService expenseCommandService;
    private final GroupRepository groupRepository;
    private final IncomeService incomeService;
    private final RecurringMapper recurringMapper;
    private final FinancialCalendar financialCalendar;
    private final ApplicationEventPublisher eventPublisher;

    public RecurringDto createRecurring(String userId, RecurringRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new SecurityHardeningException("User not found.", "USR_001"));

        CategoryEntity category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new SecurityHardeningException("Category not found.", "CAT_001"));
        }

        RecurringTransactionEntity entity = RecurringTransactionEntity.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .transactionType(request.transactionType().toUpperCase())
                .category(category)
                .amount(request.amount())
                .currencyCode(request.currencyCode() != null ? request.currencyCode() : "USD")
                .merchant(request.merchant())
                .description(request.description())
                .recurrenceType(request.recurrenceType().toUpperCase())
                .recurrenceInterval(request.recurrenceInterval())
                .startDate(request.startDate())
                .nextExecution(request.startDate())
                .endDate(request.endDate())
                .status(RecurringStatus.ACTIVE)
                .build();

        RecurringTransactionEntity saved = recurringTransactionRepository.save(entity);
        log.info("Recurring template created: {} for user {}", saved.getId(), userId);
        return recurringMapper.toDto(saved);
    }

    public RecurringDto updateRecurring(String userId, String recurringId, RecurringRequest request) {
        RecurringTransactionEntity entity = recurringTransactionRepository.findById(recurringId)
                .orElseThrow(() -> new SecurityHardeningException("Recurring template not found.", "REC_001"));

        if (!entity.getUser().getId().equals(userId)) {
            throw new SecurityHardeningException("Access denied. You do not own this recurring template.", "REC_002");
        }

        entity.setAmount(request.amount());
        if (request.currencyCode() != null) {
            entity.setCurrencyCode(request.currencyCode());
        }
        entity.setMerchant(request.merchant());
        entity.setDescription(request.description());
        entity.setRecurrenceType(request.recurrenceType().toUpperCase());
        entity.setRecurrenceInterval(request.recurrenceInterval());
        entity.setStartDate(request.startDate());
        entity.setEndDate(request.endDate());

        RecurringTransactionEntity saved = recurringTransactionRepository.save(entity);
        log.info("Recurring template updated: {}", recurringId);
        return recurringMapper.toDto(saved);
    }

    public void pauseRecurring(String userId, String recurringId) {
        setRecurringStatus(userId, recurringId, RecurringStatus.PAUSED);
    }

    public void resumeRecurring(String userId, String recurringId) {
        setRecurringStatus(userId, recurringId, RecurringStatus.ACTIVE);
    }

    public void deleteRecurring(String userId, String recurringId) {
        RecurringTransactionEntity entity = recurringTransactionRepository.findById(recurringId)
                .orElseThrow(() -> new SecurityHardeningException("Recurring template not found.", "REC_001"));

        if (!entity.getUser().getId().equals(userId)) {
            throw new SecurityHardeningException("Access denied. You do not own this recurring template.", "REC_002");
        }

        entity.setDeleted(true);
        entity.setDeletedAt(LocalDateTime.now());
        recurringTransactionRepository.save(entity);
        log.info("Recurring template soft deleted: {}", recurringId);
    }

    @Transactional(readOnly = true)
    public List<RecurringDto> getRecurrings(String userId) {
        return recurringTransactionRepository.findByUserIdAndIsDeletedFalse(userId).stream()
                .map(recurringMapper::toDto)
                .collect(Collectors.toList());
    }

    private void setRecurringStatus(String userId, String recurringId, RecurringStatus status) {
        RecurringTransactionEntity entity = recurringTransactionRepository.findById(recurringId)
                .orElseThrow(() -> new SecurityHardeningException("Recurring template not found.", "REC_001"));

        if (!entity.getUser().getId().equals(userId)) {
            throw new SecurityHardeningException("Access denied. You do not own this recurring template.", "REC_002");
        }

        entity.setStatus(status);
        recurringTransactionRepository.save(entity);
        log.info("Recurring template {} status set to {}", recurringId, status);
    }

    public void executeDueTransactions() {
        LocalDateTime now = financialCalendar.currentLocalDate().atStartOfDay();
        List<RecurringTransactionEntity> dueTemplates = recurringTransactionRepository
                .findByStatusAndNextExecutionBeforeAndIsDeletedFalse(RecurringStatus.ACTIVE, now.plusDays(1));

        log.info("ShedScheduler: found {} due templates for execution", dueTemplates.size());

        for (RecurringTransactionEntity template : dueTemplates) {
            LocalDateTime nextExec = template.getNextExecution();
            while (nextExec.isBefore(now.plusDays(1)) && template.getStatus() == RecurringStatus.ACTIVE) {
                try {
                    executeTemplateSingleTransaction(template.getId(), nextExec);
                    template = recurringTransactionRepository.findById(template.getId()).orElse(template);
                    nextExec = template.getNextExecution();
                } catch (Exception e) {
                    log.error("Failed executing template step for {}: {}", template.getId(), e.getMessage());
                    break;
                }
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeTemplateSingleTransaction(String templateId, LocalDateTime executionDate) {
        RecurringTransactionEntity template = recurringTransactionRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));

        if (template.getStatus() != RecurringStatus.ACTIVE) {
            return;
        }

        Optional<RecurringExecutionHistoryEntity> history = executionHistoryRepository
                .findByRecurringTransactionIdAndExecutionDate(templateId, executionDate);
        if (history.isPresent()) {
            log.warn("Recurring execution for template {} at {} already completed. Skipping.", templateId, executionDate);
            return;
        }

        String generatedTransactionId = UUID.randomUUID().toString();
        try {
            if ("EXPENSE".equalsIgnoreCase(template.getTransactionType())) {
                String categoryId = template.getCategory() != null ? template.getCategory().getId() : null;
                if (categoryId == null) {
                    throw new IllegalStateException("Recurring expense template must have a category configured.");
                }
                com.expenseflow.group.entity.GroupEntity group = groupRepository.findActiveGroupsByUserId(template.getUser().getId(), org.springframework.data.domain.PageRequest.of(0, 1))
                        .getContent().stream().findFirst()
                        .orElse(null);
                
                if (group == null) {
                    log.warn("Cannot execute recurring expense for template {} and user {} because they do not belong to any active groups.", templateId, template.getUser().getId());
                    return;
                }

                com.expenseflow.expense.domain.valueobject.ExpenseCategory category = com.expenseflow.expense.domain.valueobject.ExpenseCategory.OTHER;
                if (template.getCategory() != null) {
                    try {
                        category = com.expenseflow.expense.domain.valueobject.ExpenseCategory.valueOf(template.getCategory().getName().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        category = com.expenseflow.expense.domain.valueobject.ExpenseCategory.OTHER;
                    }
                }

                java.util.Map<String, BigDecimal> allocationValues = new java.util.HashMap<>();
                allocationValues.put(template.getUser().getId(), BigDecimal.ZERO);

                com.expenseflow.expense.dto.CreateExpenseRequest expenseRequest = new com.expenseflow.expense.dto.CreateExpenseRequest(
                        null, // tripId
                        template.getDescription() != null ? template.getDescription() : "Recurring Expense",
                        category,
                        template.getAmount(),
                        template.getCurrencyCode(),
                        template.getUser().getId(),
                        executionDate.toLocalDate(),
                        com.expenseflow.expense.domain.valueobject.SplitType.EQUAL,
                        allocationValues
                );

                com.expenseflow.expense.dto.ExpenseDto res = expenseCommandService.createExpense(group.getId(), expenseRequest, template.getUser().getId());
                generatedTransactionId = res.id();
            } else {
                String incomeSource = template.getMerchant() != null && !template.getMerchant().isBlank()
                        ? template.getMerchant()
                        : (template.getDescription() != null && !template.getDescription().isBlank()
                                ? template.getDescription()
                                : "Recurring Income");
                IncomeRequest incomeRequest = new IncomeRequest(
                        template.getAmount(),
                        template.getCurrencyCode(),
                        incomeSource,
                        executionDate,
                        template.getDescription(),
                        null
                );
                IncomeResponse res = incomeService.createIncome(template.getUser().getId(), incomeRequest);
                generatedTransactionId = res.id();
            }

            RecurringExecutionHistoryEntity runLog = RecurringExecutionHistoryEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .recurringTransaction(template)
                    .generatedTransactionId(generatedTransactionId)
                    .executionDate(executionDate)
                    .executionStatus("SUCCESS")
                    .createdAt(LocalDateTime.now())
                    .build();
            executionHistoryRepository.save(runLog);

            LocalDateTime nextRun = calculateNextExecution(
                    executionDate, template.getRecurrenceType(), template.getRecurrenceInterval()
            );
            template.setNextExecution(nextRun);

            if (template.getEndDate() != null && nextRun.isAfter(template.getEndDate())) {
                template.setStatus(RecurringStatus.EXPIRED);
            }
            recurringTransactionRepository.save(template);

            eventPublisher.publishEvent(new RecurringExecutionEvent(
                    this, template.getId(), template.getUser().getId(), template.getTransactionType(), template.getAmount(), template.getDescription()
            ));

        } catch (Exception e) {
            log.error("Failed template execution run {} at {}: {}", templateId, executionDate, e.getMessage());
            
            recordExecutionFailure(templateId, executionDate, e.getMessage());

            eventPublisher.publishEvent(new RecurringExecutionFailedEvent(
                    this, templateId, template.getUser().getId(), e.getMessage()
            ));

            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordExecutionFailure(String templateId, LocalDateTime executionDate, String errorMsg) {
        RecurringTransactionEntity template = recurringTransactionRepository.findById(templateId).orElse(null);
        if (template != null) {
            RecurringExecutionHistoryEntity runLog = RecurringExecutionHistoryEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .recurringTransaction(template)
                    .generatedTransactionId("FAILED")
                    .executionDate(executionDate)
                    .executionStatus("FAILED")
                    .errorMessage(errorMsg != null && errorMsg.length() > 490 ? errorMsg.substring(0, 490) : errorMsg)
                    .createdAt(LocalDateTime.now())
                    .build();
            executionHistoryRepository.save(runLog);
        }
    }

    private LocalDateTime calculateNextExecution(LocalDateTime current, String type, int interval) {
        return switch (type.toUpperCase()) {
            case "DAILY" -> current.plusDays(interval);
            case "WEEKLY" -> current.plusWeeks(interval);
            case "MONTHLY" -> current.plusMonths(interval);
            case "YEARLY" -> current.plusYears(interval);
            default -> throw new IllegalArgumentException("Unknown recurrence type: " + type);
        };
    }
}
