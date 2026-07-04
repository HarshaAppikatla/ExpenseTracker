package com.expenseflow.group.repository;

import com.expenseflow.BaseIntegrationTest;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.entity.UserStatus;
import com.expenseflow.group.entity.*;
import com.expenseflow.group.specification.GroupSpecification;
import com.expenseflow.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class GroupRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private GroupActivityRepository groupActivityRepository;

    @Autowired
    private EntityManager entityManager;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        groupActivityRepository.deleteAllInBatch();
        groupMemberRepository.deleteAllInBatch();
        groupRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        testUser = UserEntity.builder()
                .fullName("Repo Tester")
                .email("repo.tester@example.com")
                .password("Password123!")
                .status(UserStatus.ACTIVE)
                .loginProvider("LOCAL")
                .build();
        userRepository.saveAndFlush(testUser);
    }

    @Test
    void testOptimisticLocking_ThrowsExceptionOnConcurrentUpdate() {
        GroupEntity group = GroupEntity.builder()
                .id("group-123")
                .name("Optimistic Group")
                .currency("USD")
                .groupCode(new GroupCode("ABCD2345"))
                .owner(testUser)
                .build();
        groupRepository.saveAndFlush(group);

        // Clear persistence context to ensure version is tracked freshly
        entityManager.clear();

        // Retrieve two distinct entities representing the same DB row
        GroupEntity firstInstance = groupRepository.findById(group.getId()).orElseThrow();
        GroupEntity secondInstance = groupRepository.findById(group.getId()).orElseThrow();

        // Perform first update
        firstInstance.setName("Update One");
        groupRepository.saveAndFlush(firstInstance);

        // Perform second update with stale version
        secondInstance.setName("Update Two");
        assertThatThrownBy(() -> groupRepository.saveAndFlush(secondInstance))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    @Test
    void testSoftDelete_FiltersDeletedEntities() {
        GroupEntity group = GroupEntity.builder()
                .id("group-123")
                .name("Active Group")
                .currency("USD")
                .groupCode(new GroupCode("ABCD2345"))
                .owner(testUser)
                .build();
        groupRepository.saveAndFlush(group);

        // Soft delete the group
        group.setDeleted(true);
        group.setDeletedAt(LocalDateTime.now());
        group.setDeletedBy("test");
        groupRepository.saveAndFlush(group);

        entityManager.clear();

        Optional<GroupEntity> found = groupRepository.findByIdAndIsDeletedFalse(group.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void testJsonMetadataMapping_SavesAndRetrievesCorrectly() {
        GroupEntity group = GroupEntity.builder()
                .id("group-123")
                .name("JSON Group")
                .currency("USD")
                .groupCode(new GroupCode("ABCD2345"))
                .owner(testUser)
                .build();
        groupRepository.saveAndFlush(group);

        Map<String, Object> metadata = Map.of("actorId", "user-123", "action", "TEST", "value", 42.0);

        GroupActivityEntity activity = GroupActivityEntity.builder()
                .id("activity-123")
                .group(group)
                .user(testUser)
                .actionType(ActivityType.GROUP_CREATED)
                .metadata(metadata)
                .createdBy("system")
                .createdAt(LocalDateTime.now())
                .build();
        groupActivityRepository.saveAndFlush(activity);

        entityManager.clear();

        GroupActivityEntity retrieved = groupActivityRepository.findById(activity.getId()).orElseThrow();
        assertThat(retrieved.getMetadata()).containsEntry("actorId", "user-123");
        assertThat(retrieved.getMetadata()).containsEntry("action", "TEST");
        assertThat(retrieved.getMetadata()).containsEntry("value", 42.0);
    }

    @Test
    void testGroupSpecifications_FiltersAppropriately() {
        GroupEntity group1 = GroupEntity.builder()
                .id("group-1")
                .name("Paris Holiday")
                .currency("EUR")
                .groupCode(new GroupCode("CODE1234"))
                .owner(testUser)
                .build();
        groupRepository.saveAndFlush(group1);

        GroupEntity group2 = GroupEntity.builder()
                .id("group-2")
                .name("Rome Trip")
                .currency("EUR")
                .groupCode(new GroupCode("CODE5678"))
                .owner(testUser)
                .build();
        groupRepository.saveAndFlush(group2);

        // Save memberships
        GroupMemberEntity m1 = GroupMemberEntity.builder()
                .id("m-1")
                .group(group1)
                .user(testUser)
                .role(GroupRole.OWNER)
                .status(GroupMemberStatus.ACTIVE)
                .build();
        groupMemberRepository.saveAndFlush(m1);

        // Run search query spec
        Specification<GroupEntity> spec = Specification.where(GroupSpecification.isNotDeleted())
                .and(GroupSpecification.isMember(testUser.getId()))
                .and(GroupSpecification.nameOrDescriptionContains("Paris"));

        Page<GroupEntity> page = groupRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Paris Holiday");
    }
}
