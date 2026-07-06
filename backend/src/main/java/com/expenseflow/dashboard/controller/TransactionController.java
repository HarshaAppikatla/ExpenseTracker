package com.expenseflow.dashboard.controller;

import com.expenseflow.dashboard.dto.TransactionDto;
import com.expenseflow.dashboard.service.TransactionService;
import com.expenseflow.dto.ApiResponse;
import com.expenseflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TransactionDto>>> searchTransactions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "sourceOrMerchant", required = false) String sourceOrMerchant,
            @RequestParam(value = "minAmount", required = false) BigDecimal minAmount,
            @RequestParam(value = "maxAmount", required = false) BigDecimal maxAmount,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "tag", required = false) String tag,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionDto> result = transactionService.searchTransactions(
                principal.getId(), type, category, sourceOrMerchant, minAmount, maxAmount, fromDate, toDate, description, tag, pageable
        );
        return ResponseEntity.ok(ApiResponse.success("Transactions resolved successfully", result));
    }
}
