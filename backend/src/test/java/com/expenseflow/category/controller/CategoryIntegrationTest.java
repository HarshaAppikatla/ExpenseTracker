package com.expenseflow.category.controller;

import com.expenseflow.BaseIntegrationTest;
import com.expenseflow.category.dto.CategoryRequest;
import com.expenseflow.category.entity.CategoryEntity;
import com.expenseflow.category.repository.CategoryRepository;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.entity.UserStatus;
import com.expenseflow.repository.UserRepository;
import com.expenseflow.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private UserEntity testUser;
    private UserPrincipal testPrincipal;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        testUser = UserEntity.builder()
                .fullName("Category Integrator")
                .email("cat.integration@example.com")
                .password("Password123!")
                .status(UserStatus.ACTIVE)
                .loginProvider("LOCAL")
                .roles(Collections.emptySet())
                .build();
        userRepository.save(testUser);

        testPrincipal = new UserPrincipal(testUser);
    }

    @Test
    void testCategoryOnboardAndLifecycleFlow() throws Exception {
        // 1. Fetch Categories -> Expect system-defined defaults (like Food, Shopping)
        mockMvc.perform(get("/api/v1/categories")
                        .with(user(testPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(10))
                .andExpect(jsonPath("$.data[0].systemCategory").value(true));

        // 2. Create Custom Category
        CategoryRequest request = new CategoryRequest("Leisure", "sports_esports", "#E67E22");
        mockMvc.perform(post("/api/v1/categories")
                        .with(user(testPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Leisure"))
                .andExpect(jsonPath("$.data.systemCategory").value(false));

        // Retrieve custom category ID
        Optional<CategoryEntity> customCat = categoryRepository.findByUserIdAndNameIgnoreCaseAndIsDeletedFalse(testUser.getId(), "Leisure");
        assertThat(customCat).isPresent();
        String customCatId = customCat.get().getId();

        // 3. Attempt Duplicate Name -> Expect CAT_006 Error
        mockMvc.perform(post("/api/v1/categories")
                        .with(user(testPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CAT_006"));

        // 4. Update Custom Category
        CategoryRequest updateRequest = new CategoryRequest("Chill Time", "sports_esports", "#E67E22");
        mockMvc.perform(put("/api/v1/categories/" + customCatId)
                        .with(user(testPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Chill Time"));

        // 5. Delete Custom Category (Soft delete)
        mockMvc.perform(delete("/api/v1/categories/" + customCatId)
                        .with(user(testPrincipal))
                        .with(csrf()))
                .andExpect(status().isOk());

        // Verify it is excluded from active list
        mockMvc.perform(get("/api/v1/categories")
                        .with(user(testPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(10)); // Back to default 10 system categories
    }
}
