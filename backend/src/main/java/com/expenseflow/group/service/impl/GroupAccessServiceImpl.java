package com.expenseflow.group.service.impl;

import com.expenseflow.group.entity.GroupCode;
import com.expenseflow.group.entity.GroupEntity;
import com.expenseflow.group.exception.InvalidRoomCodeException;
import com.expenseflow.group.exception.RoomCodeCollisionException;
import com.expenseflow.group.repository.GroupRepository;
import com.expenseflow.group.service.GroupAccessService;
import com.expenseflow.group.validation.RoomCodeValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupAccessServiceImpl implements GroupAccessService {

    private final GroupRepository groupRepository;
    private final RoomCodeValidator roomCodeValidator;

    private static final String ROOM_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${app.group.base-url:http://localhost:8080/join/}")
    private String baseUrl;

    @Override
    @Transactional
    public GroupCode generateGroupCode() {
        for (int i = 0; i < 5; i++) {
            GroupCode code = new GroupCode(generateCodeString());
            if (!groupRepository.existsByGroupCodeAndIsDeletedFalse(code)) {
                return code;
            }
        }
        throw new RoomCodeCollisionException("Failed to generate a unique room code after 5 attempts");
    }

    @Override
    public GroupEntity validateAndResolveCode(String roomCode) {
        GroupCode code = new GroupCode(roomCode); // validates 8-character format
        GroupEntity group = groupRepository.findByGroupCodeAndIsDeletedFalse(code)
                .orElseThrow(() -> new InvalidRoomCodeException("Invalid room code"));

        roomCodeValidator.validateRoomCodeActive(group);
        return group;
    }

    @Override
    public String generateJoinLink(GroupCode groupCode) {
        if (groupCode == null) {
            return null;
        }
        return baseUrl + groupCode.value();
    }

    private String generateCodeString() {
        StringBuilder codeBuilder = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            codeBuilder.append(ROOM_CODE_ALPHABET.charAt(RANDOM.nextInt(ROOM_CODE_ALPHABET.length())));
        }
        return codeBuilder.toString();
    }
}
