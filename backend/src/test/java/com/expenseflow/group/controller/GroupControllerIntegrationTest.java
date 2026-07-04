package com.expenseflow.group.controller;

import com.expenseflow.BaseIntegrationTest;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.entity.UserStatus;
import com.expenseflow.group.dto.CreateGroupRequest;
import com.expenseflow.group.dto.JoinGroupRequest;
import com.expenseflow.group.entity.GroupCode;
import com.expenseflow.group.entity.GroupEntity;
import com.expenseflow.group.entity.GroupMemberEntity;
import com.expenseflow.group.entity.GroupMemberStatus;
import com.expenseflow.group.entity.GroupRole;
import com.expenseflow.group.repository.GroupActivityRepository;
import com.expenseflow.group.repository.GroupMemberRepository;
import com.expenseflow.group.repository.GroupRepository;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GroupControllerIntegrationTest extends BaseIntegrationTest {

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
    private GroupActivityRepository groupActivityRepository;

    private UserEntity ownerUser;
    private UserEntity memberUser;
    private UserEntity strangerUser;

    private UserPrincipal ownerPrincipal;
    private UserPrincipal memberPrincipal;
    private UserPrincipal strangerPrincipal;

    private GroupEntity testGroup;

    @BeforeEach
    void setUp() {
        groupActivityRepository.deleteAllInBatch();
        groupMemberRepository.deleteAllInBatch();
        groupRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        ownerUser = UserEntity.builder()
                .fullName("Group Owner")
                .email("owner@example.com")
                .password("Password123!")
                .status(UserStatus.ACTIVE)
                .loginProvider("LOCAL")
                .roles(Collections.emptySet())
                .build();
        userRepository.save(ownerUser);
        ownerPrincipal = new UserPrincipal(ownerUser);

        memberUser = UserEntity.builder()
                .fullName("Group Member")
                .email("member@example.com")
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

        testGroup = GroupEntity.builder()
                .id("group-1")
                .name("Paris Holiday")
                .currency("EUR")
                .groupCode(new GroupCode("CODE1234"))
                .owner(ownerUser)
                .build();
        groupRepository.save(testGroup);

        GroupMemberEntity ownerMembership = GroupMemberEntity.builder()
                .id("m-owner")
                .group(testGroup)
                .user(ownerUser)
                .role(GroupRole.OWNER)
                .status(GroupMemberStatus.ACTIVE)
                .build();
        groupMemberRepository.save(ownerMembership);

        GroupMemberEntity memberMembership = GroupMemberEntity.builder()
                .id("m-member")
                .group(testGroup)
                .user(memberUser)
                .role(GroupRole.MEMBER)
                .status(GroupMemberStatus.ACTIVE)
                .build();
        groupMemberRepository.save(memberMembership);
    }

    @Test
    void testAuthenticationRequired_Returns41() throws Exception {
        mockMvc.perform(get("/api/v1/groups"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateGroup_Success() throws Exception {
        CreateGroupRequest request = new CreateGroupRequest("Weekend Getaway", "Splitting weekend costs", "USD");

        mockMvc.perform(post("/api/v1/groups")
                        .with(user(ownerPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Group created successfully"))
                .andExpect(jsonPath("$.data.name").value("Weekend Getaway"));
    }

    @Test
    void testCreateGroup_ValidationFailure_Returns400() throws Exception {
        // Blank name and invalid currency code length
        CreateGroupRequest request = new CreateGroupRequest("", "Desc", "INVALID_CURRENCY_CODE");

        mockMvc.perform(post("/api/v1/groups")
                        .with(user(ownerPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VAL_001"));
    }

    @Test
    void testGetGroupDetails_SuccessForMember() throws Exception {
        mockMvc.perform(get("/api/v1/groups/" + testGroup.getId())
                        .with(user(memberPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testGroup.getId()));
    }

    @Test
    void testGetGroupDetails_FailureForNonMember_Returns403() throws Exception {
        mockMvc.perform(get("/api/v1/groups/" + testGroup.getId())
                        .with(user(strangerPrincipal)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testGroupJoinRateLimiting_Returns429() throws Exception {
        JoinGroupRequest joinRequest = new JoinGroupRequest("CODE1234");
        String jsonPayload = objectMapper.writeValueAsString(joinRequest);

        boolean got429 = false;
        // The join rate limit configured is 10 requests per minute
        for (int i = 0; i < 15; i++) {
            int status = mockMvc.perform(post("/api/v1/groups/join")
                            .with(user(strangerPrincipal))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonPayload))
                    .andReturn().getResponse().getStatus();

            if (status == 429) {
                got429 = true;
                break;
            }
        }
        assertThat(got429).isTrue();
    }
}
