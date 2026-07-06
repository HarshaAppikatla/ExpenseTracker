package com.expenseflow.exception;

import com.expenseflow.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Validation failed: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation validation failed", errors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument exception: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(SecurityHardeningException.class)
    public ResponseEntity<ApiResponse<Object>> handleSecurityHardeningException(SecurityHardeningException ex) {
        log.warn("Security hardening exception triggered [{}]: {}", ex.getCode(), ex.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if ("AUTH_006".equals(ex.getCode())) {
            status = HttpStatus.TOO_MANY_REQUESTS;
        }
        return ResponseEntity
                .status(status)
                .body(ApiResponse.error(ex.getMessage(), ex.getCode(), ex.getData()));
    }

    @ExceptionHandler(com.expenseflow.group.exception.GroupException.class)
    public ResponseEntity<ApiResponse<Void>> handleGroupException(com.expenseflow.group.exception.GroupException ex) {
        log.warn("Group domain exception triggered [{}]: {}", ex.getCode(), ex.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (ex instanceof com.expenseflow.group.exception.GroupNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else if (ex instanceof com.expenseflow.group.exception.PermissionDeniedException) {
            status = HttpStatus.FORBIDDEN;
        } else if (ex instanceof com.expenseflow.group.exception.DuplicateMemberException ||
                   ex instanceof com.expenseflow.group.exception.OwnerCannotLeaveException ||
                   ex instanceof com.expenseflow.group.exception.GroupArchivedException) {
            status = HttpStatus.CONFLICT;
        }
        return ResponseEntity
                .status(status)
                .body(ApiResponse.error(ex.getMessage(), ex.getCode()));
    }

    @ExceptionHandler(com.expenseflow.trip.exception.TripException.class)
    public ResponseEntity<ApiResponse<Void>> handleTripException(com.expenseflow.trip.exception.TripException ex) {
        log.warn("Trip domain exception triggered [{}]: {}", ex.getCode(), ex.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (ex instanceof com.expenseflow.trip.exception.TripNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else if (ex instanceof com.expenseflow.trip.exception.PermissionDeniedException) {
            status = HttpStatus.FORBIDDEN;
        } else if (ex instanceof com.expenseflow.trip.exception.ParticipantConflictException) {
            status = HttpStatus.CONFLICT;
        } else if (ex instanceof com.expenseflow.trip.exception.InvalidTripStateException) {
            status = HttpStatus.BAD_REQUEST;
        }
        return ResponseEntity
                .status(status)
                .body(ApiResponse.error(ex.getMessage(), ex.getCode()));
    }

    @ExceptionHandler(com.expenseflow.expense.exception.ExpenseException.class)
    public ResponseEntity<ApiResponse<Void>> handleExpenseException(com.expenseflow.expense.exception.ExpenseException ex) {
        log.warn("Expense domain exception triggered [{}]: {}", ex.getCode(), ex.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (ex instanceof com.expenseflow.expense.exception.ExpenseNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else if (ex instanceof com.expenseflow.expense.exception.ExpensePermissionDeniedException) {
            status = HttpStatus.FORBIDDEN;
        } else if (ex instanceof com.expenseflow.expense.exception.InvalidExpenseStateException) {
            status = HttpStatus.BAD_REQUEST;
        }
        return ResponseEntity
                .status(status)
                .body(ApiResponse.error(ex.getMessage(), ex.getCode()));
    }

    @ExceptionHandler(com.expenseflow.settlement.exception.SettlementException.class)
    public ResponseEntity<ApiResponse<Void>> handleSettlementException(com.expenseflow.settlement.exception.SettlementException ex) {
        log.warn("Settlement domain exception triggered [{}]: {}", ex.getCode(), ex.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (ex instanceof com.expenseflow.settlement.exception.SettlementNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else if (ex instanceof com.expenseflow.settlement.exception.SettlementPermissionDeniedException) {
            status = HttpStatus.FORBIDDEN;
        } else if (ex instanceof com.expenseflow.settlement.exception.InvalidSettlementStateException) {
            status = HttpStatus.CONFLICT; // 409 Conflict
        } else if (ex instanceof com.expenseflow.settlement.exception.NoPostedExpensesException) {
            status = HttpStatus.UNPROCESSABLE_ENTITY; // 422
        }
        return ResponseEntity
                .status(status)
                .body(ApiResponse.error(ex.getMessage(), ex.getCode()));
    }


    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Resource not found"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("An unexpected error occurred: ", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred. Please try again."));
    }
}
