package com.expenseflow.settlement.service.impl;

import com.expenseflow.expense.domain.entity.ExpenseEntity;
import com.expenseflow.expense.domain.entity.ExpenseSplitEntity;
import com.expenseflow.expense.domain.repository.ExpenseRepository;
import com.expenseflow.expense.domain.valueobject.CurrencyCode;
import com.expenseflow.expense.domain.valueobject.ExpenseStatus;
import com.expenseflow.expense.domain.valueobject.Money;
import com.expenseflow.group.dto.GroupDto;
import com.expenseflow.group.service.GroupQueryService;
import com.expenseflow.settlement.domain.engine.DebtMinimizationSolver;
import com.expenseflow.settlement.domain.engine.NetBalanceCalculator;
import com.expenseflow.settlement.domain.entity.SettlementEntity;
import com.expenseflow.settlement.domain.model.SettlementPair;
import com.expenseflow.settlement.domain.model.UserBalance;
import com.expenseflow.settlement.domain.valueobject.SettlementStatus;
import com.expenseflow.settlement.dto.SettlementResponse;
import com.expenseflow.settlement.dto.SettlementSummaryResponse;
import com.expenseflow.settlement.exception.InvalidSettlementStateException;
import com.expenseflow.settlement.exception.NoPostedExpensesException;
import com.expenseflow.settlement.exception.SettlementNotFoundException;
import com.expenseflow.settlement.exception.SettlementPermissionDeniedException;
import com.expenseflow.settlement.repository.SettlementRepository;
import com.expenseflow.settlement.service.SettlementBusinessRuleService;
import com.expenseflow.settlement.service.SettlementCommandService;
import com.expenseflow.settlement.service.SettlementQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementCommandServiceImpl implements SettlementCommandService {

    private final SettlementRepository settlementRepository;
    private final ExpenseRepository expenseRepository;
    private final NetBalanceCalculator netBalanceCalculator;
    private final DebtMinimizationSolver debtMinimizationSolver;
    private final SettlementBusinessRuleService businessRuleService;
    private final GroupQueryService groupQueryService;
    private final SettlementQueryService settlementQueryService;

    @Override
    public SettlementSummaryResponse generateSettlements(String groupId, String tripId, String currentUserId) {
        businessRuleService.verifyGroupActive(groupId);
        businessRuleService.verifyUserIsAdminOrOwner(groupId, currentUserId);

        // Fetch group details to resolve currency
        GroupDto group = groupQueryService.getGroupDetails(groupId, currentUserId);
        CurrencyCode groupCurrency = CurrencyCode.valueOf(group.currency().toUpperCase());

        // Fetch POSTED expenses
        List<ExpenseEntity> expenses = (tripId == null)
                ? expenseRepository.findByGroupIdAndStatusAndIsDeletedFalse(groupId, ExpenseStatus.POSTED)
                : expenseRepository.findByGroupIdAndTripIdAndStatusAndIsDeletedFalse(groupId, tripId, ExpenseStatus.POSTED);

        if (expenses.isEmpty()) {
            throw new NoPostedExpensesException(groupId);
        }

        // Validate currencies
        for (ExpenseEntity expense : expenses) {
            if (expense.getMoney().getCurrency() != groupCurrency) {
                throw new IllegalArgumentException("Expense " + expense.getId() + " has currency "
                        + expense.getMoney().getCurrency() + " which does not match group currency " + groupCurrency);
            }
        }

        // Translate to SplitRecords
        List<NetBalanceCalculator.SplitRecord> splits = new ArrayList<>();
        for (ExpenseEntity expense : expenses) {
            String payer = expense.getPaidByUserId();
            for (ExpenseSplitEntity split : expense.getSplits()) {
                if (split.isDeleted()) continue;
                Money owed = new Money(split.getOwedAmount(), groupCurrency);
                splits.add(new NetBalanceCalculator.SplitRecord(payer, split.getUserId(), owed));
            }
        }

        // Compute balances and solve Graph
        List<UserBalance> balances = netBalanceCalculator.calculate(splits);
        List<SettlementPair> pairs = debtMinimizationSolver.solve(balances, groupCurrency);

        // Fetch existing PENDING settlements
        List<SettlementEntity> existingPending = settlementRepository.findByGroupIdAndStatusAndIsDeletedFalse(groupId, SettlementStatus.PENDING);
        Map<String, SettlementEntity> existingMap = existingPending.stream()
                .filter(s -> tripId == null && s.getTripId() == null || tripId != null && tripId.equals(s.getTripId()))
                .collect(Collectors.toMap(
                        s -> s.getFromUserId() + ":" + s.getToUserId(),
                        s -> s
                ));

        Set<String> generatedKeys = new HashSet<>();
        List<SettlementEntity> toSave = new ArrayList<>();

        for (SettlementPair pair : pairs) {
            String key = pair.fromUserId() + ":" + pair.toUserId();
            generatedKeys.add(key);

            SettlementEntity existing = existingMap.get(key);
            if (existing != null) {
                existing.updateAmount(pair.money(), currentUserId);
                toSave.add(existing);
            } else {
                SettlementEntity newSettlement = SettlementEntity.builder()
                        .id(UUID.randomUUID().toString())
                        .groupId(groupId)
                        .tripId(tripId)
                        .fromUserId(pair.fromUserId())
                        .toUserId(pair.toUserId())
                        .money(pair.money())
                        .status(SettlementStatus.PENDING)
                        .build();
                // Set audit fields inherited from BaseEntity
                newSettlement.setCreatedBy(currentUserId);
                newSettlement.setUpdatedBy(currentUserId);
                toSave.add(newSettlement);
            }
        }

        // Soft-delete PENDING settlements that are no longer present in calculated graph
        for (Map.Entry<String, SettlementEntity> entry : existingMap.entrySet()) {
            if (!generatedKeys.contains(entry.getKey())) {
                SettlementEntity entity = entry.getValue();
                entity.setDeleted(true);
                entity.setDeletedAt(LocalDateTime.now());
                entity.setDeletedBy(currentUserId);
                toSave.add(entity);
            }
        }

        settlementRepository.saveAll(toSave);

        return settlementQueryService.getSettlementSummary(groupId, currentUserId);
    }

    @Override
    public SettlementResponse markAsPaid(String settlementId, String currentUserId) {
        SettlementEntity settlement = fetchSettlement(settlementId);
        businessRuleService.verifyGroupActive(settlement.getGroupId());

        if (!settlement.getFromUserId().equals(currentUserId)) {
            throw new SettlementPermissionDeniedException("Only the debtor (" + settlement.getFromUserId()
                    + ") can confirm payment of settlement " + settlementId);
        }

        settlement.transitionTo(SettlementStatus.CONFIRMED, currentUserId, "Payment confirmed by debtor");
        SettlementEntity saved = settlementRepository.save(settlement);

        return settlementQueryService.getSettlementById(saved.getId(), currentUserId);
    }

    @Override
    public SettlementResponse disputeSettlement(String settlementId, String reason, String currentUserId) {
        SettlementEntity settlement = fetchSettlement(settlementId);
        businessRuleService.verifyGroupActive(settlement.getGroupId());

        if (!settlement.getToUserId().equals(currentUserId)) {
            throw new SettlementPermissionDeniedException("Only the creditor (" + settlement.getToUserId()
                    + ") can dispute settlement " + settlementId);
        }

        settlement.transitionTo(SettlementStatus.DISPUTED, currentUserId, reason);
        SettlementEntity saved = settlementRepository.save(settlement);

        return settlementQueryService.getSettlementById(saved.getId(), currentUserId);
    }

    @Override
    public SettlementResponse resolveSettlement(String settlementId, String currentUserId) {
        SettlementEntity settlement = fetchSettlement(settlementId);
        businessRuleService.verifyGroupActive(settlement.getGroupId());
        businessRuleService.verifyUserIsAdminOrOwner(settlement.getGroupId(), currentUserId);

        settlement.transitionTo(SettlementStatus.CONFIRMED, currentUserId, "Resolved and confirmed by admin/owner");
        SettlementEntity saved = settlementRepository.save(settlement);

        return settlementQueryService.getSettlementById(saved.getId(), currentUserId);
    }

    private SettlementEntity fetchSettlement(String id) {
        return settlementRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new SettlementNotFoundException(id));
    }
}
