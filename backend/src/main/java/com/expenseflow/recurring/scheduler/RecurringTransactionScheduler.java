package com.expenseflow.recurring.scheduler;

import com.expenseflow.recurring.service.RecurringTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecurringTransactionScheduler {

    private final RecurringTransactionService recurringTransactionService;

    @Scheduled(cron = "0 0 0 * * ?")
    @SchedulerLock(name = "recurringTransactionTask", lockAtMostFor = "PT10M", lockAtLeastFor = "PT1M")
    public void runRecurringTransactions() {
        log.info("ShedLock: starting daily recurring transactions task execution");
        long start = System.currentTimeMillis();
        
        try {
            recurringTransactionService.executeDueTransactions();
            long duration = System.currentTimeMillis() - start;
            log.info("ShedLock: successfully finished recurring transactions execution. Duration: {} ms", duration);
        } catch (Exception e) {
            log.error("ShedLock: recurring transactions scheduler task failed: {}", e.getMessage(), e);
        }
    }
}
