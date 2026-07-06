package com.expenseflow.income.service;

import com.expenseflow.entity.UserEntity;
import com.expenseflow.exception.SecurityHardeningException;
import com.expenseflow.income.dto.IncomeRequest;
import com.expenseflow.income.dto.IncomeResponse;
import com.expenseflow.income.entity.IncomeEntity;
import com.expenseflow.income.mapper.IncomeMapper;
import com.expenseflow.income.repository.IncomeRepository;
import com.expenseflow.profile.event.UserProfileOnboardedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final IncomeMapper incomeMapper;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;
    private final com.expenseflow.repository.UserRepository userRepository;

    public IncomeResponse createIncome(String userId, IncomeRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        IncomeEntity income = IncomeEntity.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .amount(request.amount())
                .currencyCode(request.currencyCode() != null ? request.currencyCode() : "USD")
                .source(request.source())
                .incomeDate(request.incomeDate())
                .description(request.description())
                .notes(request.notes())
                .build();

        IncomeEntity saved = incomeRepository.save(income);
        log.info("Income recorded successfully: {} for user {}", saved.getId(), user.getEmail());
        
        eventPublisher.publishEvent(new com.expenseflow.core.event.IncomeCreatedEvent(
                this,
                saved.getId(),
                user.getId(),
                saved.getSource(),
                saved.getAmount(),
                saved.getCurrencyCode(),
                saved.getIncomeDate()
        ));

        return incomeMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<IncomeResponse> getIncomeList(String userId, Pageable pageable) {
        Page<IncomeEntity> incomePage = incomeRepository.findByUserIdAndIsDeletedFalse(userId, pageable);
        return incomePage.map(incomeMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public IncomeResponse getIncomeById(String userId, String incomeId) {
        IncomeEntity income = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new SecurityHardeningException("Income record not found.", "INC_001"));

        if (!income.getUser().getId().equals(userId)) {
            throw new SecurityHardeningException("Access denied. You do not own this resource.", "INC_002");
        }

        return incomeMapper.toResponse(income);
    }

    public IncomeResponse updateIncome(String userId, String incomeId, IncomeRequest request) {
        IncomeEntity income = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new SecurityHardeningException("Income record not found.", "INC_001"));

        if (!income.getUser().getId().equals(userId)) {
            throw new SecurityHardeningException("Access denied. You do not own this resource.", "INC_002");
        }

        income.setAmount(request.amount());
        if (request.currencyCode() != null) {
            income.setCurrencyCode(request.currencyCode());
        }
        income.setSource(request.source());
        income.setIncomeDate(request.incomeDate());
        income.setDescription(request.description());
        income.setNotes(request.notes());

        IncomeEntity saved = incomeRepository.save(income);
        log.info("Income updated successfully: {}", incomeId);
        
        eventPublisher.publishEvent(new com.expenseflow.core.event.IncomeUpdatedEvent(
                this,
                saved.getId(),
                userId,
                saved.getSource(),
                saved.getAmount(),
                saved.getCurrencyCode(),
                saved.getIncomeDate()
        ));

        return incomeMapper.toResponse(saved);
    }

    public void deleteIncome(String userId, String incomeId, String email) {
        IncomeEntity income = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new SecurityHardeningException("Income record not found.", "INC_001"));

        if (!income.getUser().getId().equals(userId)) {
            throw new SecurityHardeningException("Access denied. You do not own this resource.", "INC_002");
        }

        // Soft Delete
        income.setDeleted(true);
        income.setDeletedAt(LocalDateTime.now());
        income.setDeletedBy(email);

        incomeRepository.save(income);
        log.info("Income soft deleted successfully: {}", incomeId);
        
        eventPublisher.publishEvent(new com.expenseflow.core.event.IncomeDeletedEvent(
                this,
                incomeId,
                userId,
                income.getSource(),
                income.getAmount(),
                income.getCurrencyCode(),
                income.getIncomeDate()
        ));
    }

    /**
     * Decoupled listener creating initial salary transaction if provided during onboarding
     */
    @EventListener
    public void handleUserProfileOnboardedEvent(UserProfileOnboardedEvent event) {
        BigDecimal initialSalary = event.getInitialMonthlyIncome();
        if (initialSalary != null && initialSalary.compareTo(BigDecimal.ZERO) > 0) {
            IncomeEntity salaryTransaction = IncomeEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .user(event.getUser())
                    .amount(initialSalary)
                    .currencyCode(event.getPreferredCurrency())
                    .source("Initial Salary")
                    .incomeDate(LocalDateTime.now())
                    .description("Seeded monthly salary during onboarding setup")
                    .build();

            incomeRepository.save(salaryTransaction);
            log.info("Seeded initial monthly salary of {} {} for onboarded user {}",
                    initialSalary, event.getPreferredCurrency(), event.getUser().getEmail());
        }
    }
}
