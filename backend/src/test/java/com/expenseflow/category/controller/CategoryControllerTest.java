package com.expenseflow.category.controller;

import com.expenseflow.category.dto.CategoryRequest;
import com.expenseflow.category.dto.CategoryResponse;
import com.expenseflow.category.service.CategoryService;
import com.expenseflow.config.SecurityProperties;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.security.JwtAuthenticationFilter;
import com.expenseflow.security.JwtService;
import com.expenseflow.security.UserDetailsServiceImpl;
import com.expenseflow.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.test.annotation.DirtiesContext
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean private CategoryService categoryService;
    
    // Spring security mocks
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private SecurityProperties securityProperties;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCreateCategory_ValidPayload_ReturnsOk() throws Exception {
        CategoryRequest req = new CategoryRequest("Fun", "sports", "#FF5733");
        CategoryResponse resp = new CategoryResponse("cat-uuid", "Fun", "sports", "#FF5733", false);

        UserEntity userEntity = new UserEntity();
        userEntity.setId("user-uuid");
        userEntity.setEmail("user@example.com");
        userEntity.setRoles(Collections.emptySet());
        UserPrincipal principal = new UserPrincipal(userEntity);

        when(categoryService.createCategory(any(String.class), any(CategoryRequest.class))).thenReturn(resp);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        mockMvc.perform(post("/api/v1/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Fun"));
    }

    @Test
    void testCreateCategory_InvalidColor_ReturnsBadRequest() throws Exception {
        CategoryRequest req = new CategoryRequest("Fun", "sports", "invalid-color");

        UserEntity userEntity = new UserEntity();
        userEntity.setId("user-uuid");
        userEntity.setEmail("user@example.com");
        userEntity.setRoles(Collections.emptySet());
        UserPrincipal principal = new UserPrincipal(userEntity);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        mockMvc.perform(post("/api/v1/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
