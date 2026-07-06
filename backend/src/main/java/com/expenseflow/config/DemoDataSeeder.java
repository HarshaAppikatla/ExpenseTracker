package com.expenseflow.config;

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
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DemoDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserProfileRepository userProfileRepository;
    private final CategoryRepository categoryRepository;
    private final IncomeRepository incomeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        String email = "demo@expenseflow.com";

        if (userRepository.existsByEmail(email)) {
            log.info("Demo user already exists, skipping seeding.");
            return;
        }

        log.info("Resetting and seeding demo account with mock transactions on startup...");

        try {
            // 1. Double check and delete any residual records for this email nativeside
            entityManager.createNativeQuery("DELETE FROM user_roles WHERE user_id IN (SELECT id FROM users WHERE email = :email)").setParameter("email", email).executeUpdate();
            entityManager.createNativeQuery("DELETE FROM user_profiles WHERE user_id IN (SELECT id FROM users WHERE email = :email)").setParameter("email", email).executeUpdate();
            entityManager.createNativeQuery("DELETE FROM expenses WHERE paid_by_user_id IN (SELECT id FROM users WHERE email = :email)").setParameter("email", email).executeUpdate();
            entityManager.createNativeQuery("DELETE FROM income WHERE user_id IN (SELECT id FROM users WHERE email = :email)").setParameter("email", email).executeUpdate();
            entityManager.createNativeQuery("DELETE FROM users WHERE email = :email").setParameter("email", email).executeUpdate();
            entityManager.flush();

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
            entityManager.flush();

            // 3. Create User Profile
            UserProfileEntity profile = UserProfileEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .user(user)
                    .preferredCurrency("USD")
                    .openingBalance(new BigDecimal("5000.00"))
                    .onboardingCompleted(true)
                    .build();
            userProfileRepository.save(profile);
            entityManager.flush();

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

            log.info("Demo account seeded successfully with 2 incomes!");
        } catch (Exception e) {
            log.error("Failed to seed demo data on startup: ", e);
        }
    }
}
