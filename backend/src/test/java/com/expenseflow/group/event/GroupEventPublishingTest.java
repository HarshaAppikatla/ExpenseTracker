package com.expenseflow.group.event;

import com.expenseflow.BaseIntegrationTest;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.entity.UserStatus;
import com.expenseflow.group.dto.CreateGroupRequest;
import com.expenseflow.group.service.GroupCommandService;
import com.expenseflow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RecordApplicationEvents
class GroupEventPublishingTest extends BaseIntegrationTest {

    @Autowired
    private GroupCommandService groupCommandService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Test
    @Transactional
    void createGroup_ShouldPublishGroupCreatedEvent() {
        UserEntity user = UserEntity.builder()
                .fullName("Event Owner")
                .email("event.owner@example.com")
                .password("Password123!")
                .status(UserStatus.ACTIVE)
                .loginProvider("LOCAL")
                .build();
        userRepository.saveAndFlush(user);

        CreateGroupRequest request = new CreateGroupRequest("Event Group", "Verification desc", "USD");
        groupCommandService.createGroup(request, user.getId());

        long count = applicationEvents.stream(GroupCreatedEvent.class).count();
        assertThat(count).isEqualTo(1);
    }
}
