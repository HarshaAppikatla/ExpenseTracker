package com.expenseflow.controller;

import com.expenseflow.entity.UserEntity;
import com.expenseflow.entity.UserStatus;
import com.expenseflow.entity.RoleEntity;
import com.expenseflow.repository.UserRepository;
import com.expenseflow.repository.RoleRepository;
import com.expenseflow.profile.entity.UserProfileEntity;
import com.expenseflow.profile.repository.UserProfileRepository;
import com.expenseflow.category.entity.CategoryEntity;
import com.expenseflow.category.repository.CategoryRepository;
import com.expenseflow.income.entity.IncomeEntity;
import com.expenseflow.income.repository.IncomeRepository;

import com.expenseflow.group.entity.*;
import com.expenseflow.trip.domain.entity.*;
import com.expenseflow.trip.domain.valueobject.*;
import com.expenseflow.expense.domain.entity.*;
import com.expenseflow.expense.domain.valueobject.*;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/debug")
@RequiredArgsConstructor
public class DebugController {

    private final EntityManager entityManager;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserProfileRepository userProfileRepository;
    private final CategoryRepository categoryRepository;
    private final IncomeRepository incomeRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @RequestMapping(value = "/seed", method = {org.springframework.web.bind.annotation.RequestMethod.GET, org.springframework.web.bind.annotation.RequestMethod.POST})
    @Transactional
    public ResponseEntity<?> seedDemoData() {
        try {
            // Enforce safety check - only allow in dev profile
            List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
            if (!activeProfiles.contains("dev")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Seeding is only allowed in 'dev' environment"));
            }

            String email = "demo@expenseflow.com";
            
            // 1. Delete existing demo user data completely using native SQL (bypassing soft-delete filters)
            entityManager.createNativeQuery("DELETE FROM user_roles WHERE user_id IN (SELECT id FROM users WHERE email = :email)").setParameter("email", email).executeUpdate();
            entityManager.createNativeQuery("DELETE FROM user_profiles WHERE user_id IN (SELECT id FROM users WHERE email = :email)").setParameter("email", email).executeUpdate();
            entityManager.createNativeQuery("DELETE FROM expenses WHERE paid_by_user_id IN (SELECT id FROM users WHERE email = :email)").setParameter("email", email).executeUpdate();
            entityManager.createNativeQuery("DELETE FROM income WHERE user_id IN (SELECT id FROM users WHERE email = :email)").setParameter("email", email).executeUpdate();
            entityManager.createNativeQuery("DELETE FROM users WHERE email = :email").setParameter("email", email).executeUpdate();

            // 2. Create User
            RoleEntity userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new IllegalStateException("ROLE_USER not found"));

            String userId = UUID.randomUUID().toString();
            UserEntity user = UserEntity.builder()
                    .id(userId)
                    .fullName("Demo User")
                    .email(email)
                    .password(passwordEncoder.encode("Password123!"))
                    .status(UserStatus.ACTIVE)
                    .loginProvider("LOCAL")
                    .roles(Set.of(userRole))
                    .build();
            user = userRepository.save(user);

            // 3. Create User Profile (Onboarding completed)
            UserProfileEntity profile = UserProfileEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .user(user)
                    .preferredCurrency("USD")
                    .openingBalance(new BigDecimal("5000.00"))
                    .onboardingCompleted(true)
                    .build();
            userProfileRepository.save(profile);

            // 4. Retrieve seeded categories or fallback to creating them
            List<CategoryEntity> systemCategories = categoryRepository.findAll().stream()
                    .filter(CategoryEntity::isSystemCategory)
                    .toList();

            CategoryEntity foodCat = systemCategories.stream().filter(c -> "Food".equalsIgnoreCase(c.getName())).findFirst()
                    .orElseGet(() -> categoryRepository.save(CategoryEntity.builder().id(UUID.randomUUID().toString()).name("Food").icon("restaurant").color("#FF5733").systemCategory(true).build()));
            
            CategoryEntity shoppingCat = systemCategories.stream().filter(c -> "Shopping".equalsIgnoreCase(c.getName())).findFirst()
                    .orElseGet(() -> categoryRepository.save(CategoryEntity.builder().id(UUID.randomUUID().toString()).name("Shopping").icon("shopping_bag").color("#E0115F").systemCategory(true).build()));

            CategoryEntity transportCat = systemCategories.stream().filter(c -> "Transport".equalsIgnoreCase(c.getName())).findFirst()
                    .orElseGet(() -> categoryRepository.save(CategoryEntity.builder().id(UUID.randomUUID().toString()).name("Transport").icon("directions_car").color("#1F75FE").systemCategory(true).build()));

            CategoryEntity billsCat = systemCategories.stream().filter(c -> "Bills".equalsIgnoreCase(c.getName())).findFirst()
                    .orElseGet(() -> categoryRepository.save(CategoryEntity.builder().id(UUID.randomUUID().toString()).name("Bills").icon("receipt_long").color("#F4D03F").systemCategory(true).build()));

            // 5. Seed Income
            IncomeEntity salary = IncomeEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .user(user)
                    .amount(new BigDecimal("3500.00"))
                    .currencyCode("USD")
                    .source("Monthly Salary")
                    .incomeDate(LocalDateTime.now().minusDays(10))
                    .description("June Salary Deposit")
                    .build();
            incomeRepository.save(salary);

            IncomeEntity freelance = IncomeEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .user(user)
                    .amount(new BigDecimal("650.00"))
                    .currencyCode("USD")
                    .source("Freelance Project")
                    .incomeDate(LocalDateTime.now().minusDays(5))
                    .description("Website landing page design")
                    .build();
            incomeRepository.save(freelance);
            entityManager.flush();

            return ResponseEntity.ok(Map.of(
                    "message", "Demo data seeded successfully!",
                    "user", Map.of(
                            "email", email,
                            "password", "Password123!",
                            "fullName", "Demo User"
                    ),
                    "seedingDetails", Map.of(
                            "openingBalance", "$5000.05",
                            "incomeRecords", 2,
                            "expenseRecords", 0
                    )
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", e.getMessage(),
                            "type", e.getClass().getName(),
                            "stackTrace", Arrays.stream(e.getStackTrace()).limit(15).map(StackTraceElement::toString).toList()
                    ));
        }
    }

    @RequestMapping(value = "/seed-coorg", method = {org.springframework.web.bind.annotation.RequestMethod.GET, org.springframework.web.bind.annotation.RequestMethod.POST})
    @Transactional
    public ResponseEntity<?> seedCoorgTrip() {
        try {
            // Enforce safety check - only allow in dev profile
            List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
            if (!activeProfiles.contains("dev")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Seeding is only allowed in 'dev' environment"));
            }

            // 1. Get or create the 5 friends
            UserEntity demoUser = getOrCreateUser("demo@expenseflow.com", "Demo User");
            UserEntity alice = getOrCreateUser("alice@expenseflow.com", "Alice");
            UserEntity bob = getOrCreateUser("bob@expenseflow.com", "Bob");
            UserEntity charlie = getOrCreateUser("charlie@expenseflow.com", "Charlie");
            UserEntity david = getOrCreateUser("david@expenseflow.com", "David");

            // 2. Clean up any existing group with name "Weekend Trip to Coorg"
            List<?> groups = entityManager.createQuery("SELECT g FROM GroupEntity g WHERE g.name = :name")
                    .setParameter("name", "Weekend Trip to Coorg")
                    .getResultList();

            for (Object obj : groups) {
                GroupEntity group = (GroupEntity) obj;
                String groupId = group.getId();
                
                // Clean up dependent tables via native SQL to avoid cascade constraints issues
                entityManager.createNativeQuery("DELETE FROM expense_splits WHERE expense_id IN (SELECT id FROM expenses WHERE group_id = :groupId)").setParameter("groupId", groupId).executeUpdate();
                entityManager.createNativeQuery("DELETE FROM expense_participants WHERE expense_id IN (SELECT id FROM expenses WHERE group_id = :groupId)").setParameter("groupId", groupId).executeUpdate();
                entityManager.createNativeQuery("DELETE FROM expense_attachments WHERE expense_id IN (SELECT id FROM expenses WHERE group_id = :groupId)").setParameter("groupId", groupId).executeUpdate();
                entityManager.createNativeQuery("DELETE FROM expenses WHERE group_id = :groupId").setParameter("groupId", groupId).executeUpdate();
                entityManager.createNativeQuery("DELETE FROM trip_participants WHERE trip_id IN (SELECT id FROM trips WHERE group_id = :groupId)").setParameter("groupId", groupId).executeUpdate();
                entityManager.createNativeQuery("DELETE FROM trip_activities WHERE trip_id IN (SELECT id FROM trips WHERE group_id = :groupId)").setParameter("groupId", groupId).executeUpdate();
                entityManager.createNativeQuery("DELETE FROM trips WHERE group_id = :groupId").setParameter("groupId", groupId).executeUpdate();
                entityManager.createNativeQuery("DELETE FROM group_activity WHERE group_id = :groupId").setParameter("groupId", groupId).executeUpdate();
                entityManager.createNativeQuery("DELETE FROM group_members WHERE group_id = :groupId").setParameter("groupId", groupId).executeUpdate();
                entityManager.createNativeQuery("DELETE FROM `groups` WHERE id = :groupId").setParameter("groupId", groupId).executeUpdate();
            }
            entityManager.flush();

            // 3. Create the Group
            GroupEntity group = new GroupEntity();
            String groupId = UUID.randomUUID().toString();
            group.setId(groupId);
            group.setName("Weekend Trip to Coorg");
            group.setDescription("Shared expenses and bill splits for our weekend trip to Coorg!");
            group.setCurrency("USD");
            group.setGroupCode(new GroupCode("COORG026"));
            group.setOwner(demoUser);
            group.setSettings(new GroupSettings(true, true, false));
            group.setCreatedAt(LocalDateTime.now());
            group.setUpdatedAt(LocalDateTime.now());
            group = entityManager.merge(group);

            // 4. Create Group Members
            List<UserEntity> users = List.of(demoUser, alice, bob, charlie, david);
            List<GroupRole> roles = List.of(GroupRole.OWNER, GroupRole.ADMIN, GroupRole.MEMBER, GroupRole.MEMBER, GroupRole.MEMBER);
            
            for (int i = 0; i < users.size(); i++) {
                GroupMemberEntity member = new GroupMemberEntity();
                member.setId(UUID.randomUUID().toString());
                member.setGroup(group);
                member.setUser(users.get(i));
                member.setRole(roles.get(i));
                member.setStatus(GroupMemberStatus.ACTIVE);
                member.setJoinedAt(LocalDateTime.now().minusDays(2));
                entityManager.persist(member);
            }

            // 5. Create Group Activity
            GroupActivityEntity activity = new GroupActivityEntity();
            activity.setId(UUID.randomUUID().toString());
            activity.setGroup(group);
            activity.setUser(demoUser);
            activity.setActionType(ActivityType.GROUP_CREATED);
            activity.setMetadata(Map.of("groupName", "Weekend Trip to Coorg"));
            activity.setCreatedAt(LocalDateTime.now().minusDays(2));
            entityManager.persist(activity);

            // 6. Create Trip
            TripEntity trip = new TripEntity();
            String tripId = UUID.randomUUID().toString();
            trip.setId(tripId);
            trip.setGroupId(groupId);
            trip.setTitle("Coorg Trip 2026");
            trip.setDescription("A fun weekend trip to the hills of Coorg with 5 friends.");
            trip.setOrganizerId(demoUser.getId());
            trip.setDestination(new Destination("Madikeri", "India", "Coorg, Karnataka, India"));
            trip.setSchedule(new TripSchedule(java.time.LocalDate.now(), java.time.LocalDate.now().plusDays(3)));
            trip.setStatus(TripStatus.ACTIVE);
            trip.setCoverType("PRESET");
            trip.setCoverImage("linear-gradient(135deg, #134e5e 0%, #71b280 100%)");
            trip.setCreatedAt(LocalDateTime.now());
            trip.setUpdatedAt(LocalDateTime.now());
            entityManager.persist(trip);

            // 6b. Add all 5 members as ACCEPTED trip participants (required before invitations can be issued)
            for (UserEntity u : users) {
                TripParticipantEntity tp = TripParticipantEntity.builder()
                        .id(UUID.randomUUID().toString())
                        .trip(trip)
                        .userId(u.getId())
                        .status(com.expenseflow.trip.domain.valueobject.TripParticipantStatus.ACCEPTED)
                        .joinedAt(LocalDateTime.now().minusDays(2))
                        .build();
                entityManager.persist(tp);
            }

            // 7. Seed 5 different shared expenses
            // Expense 1: Villa Booking - $500, paid by Demo User
            createMockExpense(groupId, tripId, "Coorg Villa Booking", ExpenseCategory.LODGING, 
                    new BigDecimal("500.00"), demoUser.getId(), users);

            // Expense 2: Dinner at Taj Coorg - $150, paid by Alice
            createMockExpense(groupId, tripId, "Dinner at Taj", ExpenseCategory.FOOD, 
                    new BigDecimal("150.00"), alice.getId(), users);

            // Expense 3: Car Rental - $240, paid by Bob
            createMockExpense(groupId, tripId, "Car Rental & Fuel", ExpenseCategory.TRANSPORT, 
                    new BigDecimal("240.00"), bob.getId(), users);

            // Expense 4: River Rafting - $200, paid by Charlie
            createMockExpense(groupId, tripId, "Adventure Rafting", ExpenseCategory.ENTERTAINMENT, 
                    new BigDecimal("200.00"), charlie.getId(), users);

            // Expense 5: Snacks & Drinks - $50, paid by David
            createMockExpense(groupId, tripId, "Snacks & Drinks", ExpenseCategory.SHOPPING, 
                    new BigDecimal("50.00"), david.getId(), users);

            entityManager.flush();

            return ResponseEntity.ok(Map.of(
                    "message", "Coorg Group seeded successfully with 5 friends, 1 trip, and 5 shared expenses!",
                    "groupId", groupId,
                    "tripId", tripId,
                    "groupCode", "COORG026"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage(), "type", e.getClass().getName()));
        }
    }

    private UserEntity getOrCreateUser(String email, String fullName) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            RoleEntity userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new IllegalStateException("ROLE_USER not found"));
            String id = UUID.randomUUID().toString();
            UserEntity user = UserEntity.builder()
                    .id(id)
                    .fullName(fullName)
                    .email(email)
                    .password(passwordEncoder.encode("Password123!"))
                    .status(UserStatus.ACTIVE)
                    .loginProvider("LOCAL")
                    .roles(Set.of(userRole))
                    .build();
            user = userRepository.save(user);
            
            UserProfileEntity profile = UserProfileEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .user(user)
                    .preferredCurrency("USD")
                    .openingBalance(new BigDecimal("1000.00"))
                    .onboardingCompleted(true)
                    .build();
            userProfileRepository.save(profile);
            
            return user;
        });
    }

    private void createMockExpense(String groupId, String tripId, String description, ExpenseCategory category, 
                                   BigDecimal totalAmount, String payerId, List<UserEntity> participants) {
        ExpenseEntity expense = ExpenseEntity.builder()
                .id(UUID.randomUUID().toString())
                .groupId(groupId)
                .tripId(tripId)
                .description(description)
                .category(category)
                .categoryType(ExpenseCategoryType.SYSTEM)
                .money(new Money(totalAmount, CurrencyCode.USD))
                .paidByUserId(payerId)
                .createdByUserId(payerId)
                .status(ExpenseStatus.DRAFT)
                .expenseDate(java.time.LocalDate.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(payerId)
                .updatedBy(payerId)
                .participants(new ArrayList<>())
                .splits(new ArrayList<>())
                .attachments(new ArrayList<>())
                .build();

        // Calculate split amounts
        BigDecimal count = new BigDecimal(participants.size());
        BigDecimal owedAmount = totalAmount.divide(count, 4, java.math.RoundingMode.HALF_UP);
        BigDecimal remainder = totalAmount.subtract(owedAmount.multiply(count.subtract(BigDecimal.ONE)));
        
        Map<String, BigDecimal> calculatedOwed = new HashMap<>();
        Map<String, BigDecimal> allocationValues = new HashMap<>();
        
        for (int i = 0; i < participants.size(); i++) {
            String uId = participants.get(i).getId();
            BigDecimal amount = (i == 0) ? remainder : owedAmount;
            calculatedOwed.put(uId, amount.setScale(2, java.math.RoundingMode.HALF_UP));
            allocationValues.put(uId, new BigDecimal("1.0")); // Equal weighting
        }
        
        expense.updateSplitsAndParticipants(calculatedOwed, allocationValues);
        expense.transitionTo(ExpenseStatus.POSTED); // Now transition to POSTED so it counts in settlements
        entityManager.persist(expense);
    }
}
