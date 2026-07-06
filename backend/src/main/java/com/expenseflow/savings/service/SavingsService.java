package com.expenseflow.savings.service;

import com.expenseflow.entity.UserEntity;
import com.expenseflow.exception.SecurityHardeningException;
import com.expenseflow.repository.UserRepository;
import com.expenseflow.savings.dto.SavingsDepositDto;
import com.expenseflow.savings.dto.SavingsDepositRequest;
import com.expenseflow.savings.dto.SavingsGoalDto;
import com.expenseflow.savings.dto.SavingsGoalRequest;
import com.expenseflow.savings.entity.SavingsDepositEntity;
import com.expenseflow.savings.entity.SavingsGoalEntity;
import com.expenseflow.savings.mapper.SavingsMapper;
import com.expenseflow.savings.repository.SavingsDepositRepository;
import com.expenseflow.savings.repository.SavingsGoalRepository;
import com.expenseflow.core.event.SavingsDepositCreatedEvent;
import com.expenseflow.core.event.SavingsGoalCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SavingsService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final SavingsDepositRepository savingsDepositRepository;
    private final UserRepository userRepository;
    private final SavingsMapper savingsMapper;
    private final ApplicationEventPublisher eventPublisher;

    public SavingsGoalDto createGoal(String userId, SavingsGoalRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new SecurityHardeningException("User not found.", "USR_001"));

        SavingsGoalEntity entity = SavingsGoalEntity.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .title(request.title())
                .description(request.description())
                .targetAmount(request.targetAmount())
                .targetDate(request.targetDate())
                .completed(false)
                .build();

        SavingsGoalEntity saved = savingsGoalRepository.save(entity);
        log.info("Savings goal created: {} for user {}", saved.getId(), userId);
        return toGoalDtoWithProgress(saved);
    }

    public SavingsGoalDto updateGoal(String userId, String goalId, SavingsGoalRequest request) {
        SavingsGoalEntity entity = savingsGoalRepository.findById(goalId)
                .orElseThrow(() -> new SecurityHardeningException("Goal not found.", "SAV_001"));

        if (!entity.getUser().getId().equals(userId)) {
            throw new SecurityHardeningException("Access denied. You do not own this goal.", "SAV_002");
        }

        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setTargetAmount(request.targetAmount());
        entity.setTargetDate(request.targetDate());

        BigDecimal sum = savingsDepositRepository.calculateSumByGoalId(goalId);
        if (sum.compareTo(entity.getTargetAmount()) >= 0) {
            if (!entity.isCompleted()) {
                entity.setCompleted(true);
                entity.setCompletedAt(LocalDateTime.now());
                eventPublisher.publishEvent(new SavingsGoalCompletedEvent(this, goalId, userId, entity.getTitle(), entity.getTargetAmount()));
            }
        } else {
            entity.setCompleted(false);
            entity.setCompletedAt(null);
        }

        SavingsGoalEntity saved = savingsGoalRepository.save(entity);
        log.info("Savings goal updated: {}", goalId);
        return toGoalDtoWithProgress(saved);
    }

    public void deleteGoal(String userId, String goalId) {
        SavingsGoalEntity entity = savingsGoalRepository.findById(goalId)
                .orElseThrow(() -> new SecurityHardeningException("Goal not found.", "SAV_001"));

        if (!entity.getUser().getId().equals(userId)) {
            throw new SecurityHardeningException("Access denied. You do not own this goal.", "SAV_002");
        }

        entity.setDeleted(true);
        entity.setDeletedAt(LocalDateTime.now());
        savingsGoalRepository.save(entity);
        log.info("Savings goal soft deleted: {}", goalId);
    }

    @Transactional(readOnly = true)
    public List<SavingsGoalDto> getGoals(String userId) {
        return savingsGoalRepository.findByUserIdAndIsDeletedFalse(userId).stream()
                .map(this::toGoalDtoWithProgress)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SavingsGoalDto getGoalDetails(String userId, String goalId) {
        SavingsGoalEntity entity = savingsGoalRepository.findById(goalId)
                .orElseThrow(() -> new SecurityHardeningException("Goal not found.", "SAV_001"));

        if (!entity.getUser().getId().equals(userId)) {
            throw new SecurityHardeningException("Access denied. You do not own this goal.", "SAV_002");
        }

        return toGoalDtoWithProgress(entity);
    }

    public SavingsDepositDto addDeposit(String userId, String goalId, SavingsDepositRequest request, String createdBy) {
        SavingsGoalEntity goal = savingsGoalRepository.findById(goalId)
                .orElseThrow(() -> new SecurityHardeningException("Goal not found.", "SAV_001"));

        if (!goal.getUser().getId().equals(userId)) {
            throw new SecurityHardeningException("Access denied. You do not own this goal.", "SAV_002");
        }

        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be a positive number");
        }

        SavingsDepositEntity deposit = SavingsDepositEntity.builder()
                .id(UUID.randomUUID().toString())
                .savingsGoal(goal)
                .amount(request.amount())
                .depositDate(LocalDateTime.now())
                .notes(request.notes())
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
                .build();

        SavingsDepositEntity saved = savingsDepositRepository.save(deposit);
        log.info("Deposit of {} added to goal {}", request.amount(), goalId);

        eventPublisher.publishEvent(new SavingsDepositCreatedEvent(this, saved.getId(), goalId, goal.getTitle(), userId, saved.getAmount()));

        BigDecimal sum = savingsDepositRepository.calculateSumByGoalId(goalId);
        if (sum.compareTo(goal.getTargetAmount()) >= 0 && !goal.isCompleted()) {
            goal.setCompleted(true);
            goal.setCompletedAt(LocalDateTime.now());
            savingsGoalRepository.save(goal);
            eventPublisher.publishEvent(new SavingsGoalCompletedEvent(this, goalId, userId, goal.getTitle(), goal.getTargetAmount()));
            log.info("Goal {} successfully completed!", goalId);
        }

        return savingsMapper.toDepositDto(saved);
    }

    @Transactional(readOnly = true)
    public List<SavingsDepositDto> getDeposits(String userId, String goalId) {
        SavingsGoalEntity goal = savingsGoalRepository.findById(goalId)
                .orElseThrow(() -> new SecurityHardeningException("Goal not found.", "SAV_001"));

        if (!goal.getUser().getId().equals(userId)) {
            throw new SecurityHardeningException("Access denied. You do not own this goal.", "SAV_002");
        }

        return savingsDepositRepository.findBySavingsGoalId(goalId).stream()
                .map(savingsMapper::toDepositDto)
                .collect(Collectors.toList());
    }

    private SavingsGoalDto toGoalDtoWithProgress(SavingsGoalEntity entity) {
        BigDecimal spent = savingsDepositRepository.calculateSumByGoalId(entity.getId());
        double progress = 0.0;
        if (entity.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            progress = spent.divide(entity.getTargetAmount(), 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
        }

        return new SavingsGoalDto(
                entity.getId(),
                entity.getUser().getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getTargetAmount(),
                entity.getTargetDate(),
                spent,
                progress,
                entity.isCompleted(),
                entity.getCompletedAt()
        );
    }
}
