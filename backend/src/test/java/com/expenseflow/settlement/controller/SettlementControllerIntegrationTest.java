package com.expenseflow.settlement.controller;

import com.expenseflow.BaseIntegrationTest;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.entity.UserStatus;
import com.expenseflow.expense.domain.entity.ExpenseEntity;
import com.expenseflow.expense.domain.entity.ExpenseParticipantEntity;
import com.expenseflow.expense.domain.entity.ExpenseSplitEntity;
import com.expenseflow.expense.domain.repository.ExpenseRepository;
import com.expenseflow.expense.domain.valueobject.*;
import com.expenseflow.group.entity.GroupCode;
import com.expenseflow.group.entity.GroupEntity;
import com.expenseflow.group.entity.GroupMemberEntity;
import com.expenseflow.group.entity.GroupMemberStatus;
import com.expenseflow.group.entity.GroupRole;
import com.expenseflow.group.repository.GroupMemberRepository;
import com.expenseflow.group.repository.GroupRepository;
import com.expenseflow.repository.UserRepository;
import com.expenseflow.security.UserPrincipal;
import com.expenseflow.settlement.domain.entity.SettlementEntity;
import com.expenseflow.settlement.domain.valueobject.SettlementStatus;
import com.expenseflow.settlement.dto.ConfirmSettlementRequest;
import com.expenseflow.settlement.dto.DisputeSettlementRequest;
import com.expenseflow.settlement.repository.SettlementRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SettlementControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private SettlementRepository settlementRepository;

    private UserEntity ownerUser;
    private UserEntity memberUser;
    private UserEntity strangerUser;

    private UserPrincipal ownerPrincipal;
    private UserPrincipal memberPrincipal;
    private UserPrincipal strangerPrincipal;

    private GroupEntity testGroup;

    @BeforeEach
    void setUp() {
        settlementRepository.deleteAllInBatch();
        expenseRepository.deleteAllInBatch();
        groupMemberRepository.deleteAllInBatch();
        groupRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        // 1. Setup Users
        ownerUser = UserEntity.builder()
                .fullName("Alice Owner")
                .email("alice@example.com")
                .password("Password123!")
                .status(UserStatus.ACTIVE)
                .loginProvider("LOCAL")
                .roles(Collections.emptySet())
                .build();
        userRepository.save(ownerUser);
        ownerPrincipal = new UserPrincipal(ownerUser);

        memberUser = UserEntity.builder()
                .fullName("Bob Member")
                .email("bob@example.com")
                .password("Password123!")
                .status(UserStatus.ACTIVE)
                .loginProvider("LOCAL")
                .roles(Collections.emptySet())
                .build();
        userRepository.save(memberUser);
        memberPrincipal = new UserPrincipal(memberUser);

        strangerUser = UserEntity.builder()
                .fullName("Stranger")
                .email("stranger@example.com")
                .password("Password123!")
                .status(UserStatus.ACTIVE)
                .loginProvider("LOCAL")
                .roles(Collections.emptySet())
                .build();
        userRepository.save(strangerUser);
        strangerPrincipal = new UserPrincipal(strangerUser);

        // 2. Setup Group
        testGroup = GroupEntity.builder()
                .id("group-1")
                .name("Holiday Trip")
                .currency("INR")
                .groupCode(new GroupCode("CODE9999"))
                .owner(ownerUser)
                .build();
        groupRepository.save(testGroup);

        // Memberships
        groupMemberRepository.save(GroupMemberEntity.builder()
                .id("m-owner")
                .group(testGroup)
                .user(ownerUser)
                .role(GroupRole.OWNER)
                .status(GroupMemberStatus.ACTIVE)
                .build());

        groupMemberRepository.save(GroupMemberEntity.builder()
                .id("m-member")
                .group(testGroup)
                .user(memberUser)
                .role(GroupRole.MEMBER)
                .status(GroupMemberStatus.ACTIVE)
                .build());
    }

    @Test
    void testFullSettlementLifecycle_Success() throws Exception {
        // Step 1: Create a POSTED expense where Alice paid 100 INR, and Bob owes 40 INR, Alice owes 60 INR
        ExpenseEntity expense = ExpenseEntity.builder()
                .id(UUID.randomUUID().toString())
                .groupId(testGroup.getId())
                .description("Dinner bills")
                .category(ExpenseCategory.FOOD)
                .categoryType(ExpenseCategoryType.SYSTEM)
                .money(new Money(new BigDecimal("100.00"), CurrencyCode.INR))
                .paidByUserId(ownerUser.getId())
                .createdByUserId(ownerUser.getId())
                .status(ExpenseStatus.POSTED)
                .splitType(SplitType.EXACT)
                .expenseDate(LocalDate.now())
                .participants(new ArrayList<>())
                .splits(new ArrayList<>())
                .createdBy(ownerUser.getId())
                .updatedBy(ownerUser.getId())
                .build();

        expense.getParticipants().add(ExpenseParticipantEntity.builder()
                .id("p-1").expense(expense).userId(ownerUser.getId()).build());
        expense.getParticipants().add(ExpenseParticipantEntity.builder()
                .id("p-2").expense(expense).userId(memberUser.getId()).build());

        expense.getSplits().add(ExpenseSplitEntity.builder()
                .id("s-1").expense(expense).userId(ownerUser.getId())
                .owedAmount(new BigDecimal("60.00")).version(0L).build());
        expense.getSplits().add(ExpenseSplitEntity.builder()
                .id("s-2").expense(expense).userId(memberUser.getId())
                .owedAmount(new BigDecimal("40.00")).version(0L).build());

        expenseRepository.save(expense);

        // Step 2: Generate Settlements (Must be called by owner Alice)
        mockMvc.perform(post("/api/v1/groups/" + testGroup.getId() + "/settlements/generate")
                        .with(user(ownerPrincipal))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pendingCount").value(1))
                .andExpect(jsonPath("$.data.totalOutstanding").value(40.00))
                .andExpect(jsonPath("$.data.settlements[0].fromUserId").value(memberUser.getId()))
                .andExpect(jsonPath("$.data.settlements[0].toUserId").value(ownerUser.getId()));

        List<SettlementEntity> settlements = settlementRepository.findByGroupIdAndStatusAndIsDeletedFalse(testGroup.getId(), SettlementStatus.PENDING);
        assertThat(settlements).hasSize(1);
        SettlementEntity settlement = settlements.get(0);
        assertThat(settlement.getMoney().getAmount()).isEqualByComparingTo(new BigDecimal("40.00"));

        // Step 3: Get Settlement Summary
        mockMvc.perform(get("/api/v1/groups/" + testGroup.getId() + "/settlements")
                        .with(user(memberPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalOutstanding").value(40.00));

        // Step 4: Dispute Settlement (Must be called by creditor Alice)
        DisputeSettlementRequest disputeReq = new DisputeSettlementRequest("Actually you owe me 50");
        mockMvc.perform(post("/api/v1/settlements/" + settlement.getId() + "/dispute")
                        .with(user(ownerPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(disputeReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("DISPUTED"));

        // Step 5: Resolve Dispute (Must be called by owner Alice)
        mockMvc.perform(post("/api/v1/settlements/" + settlement.getId() + "/resolve")
                        .with(user(ownerPrincipal))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));

        // Check in DB
        SettlementEntity confirmedSettlement = settlementRepository.findById(settlement.getId()).get();
        assertThat(confirmedSettlement.getStatus()).isEqualTo(SettlementStatus.CONFIRMED);
    }
}
