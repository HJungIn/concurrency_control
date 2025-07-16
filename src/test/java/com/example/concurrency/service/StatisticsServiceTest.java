package com.example.concurrency.service;

import com.example.concurrency.dto.StatisticsDto;
import com.example.concurrency.repository.StatisticsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class StatisticsServiceTest {

    @Autowired
    private StatisticsService  statisticsService;

    @Autowired
    private StatisticsRepository statisticsRepository;

    @Transactional
    @Rollback(false)
    @Test
    void increase_Synchronized_Test() throws InterruptedException {
        StatisticsDto dto = StatisticsDto.builder()
                .name("test")
                .count(1)
                .build();

        Runnable increaseCount = () -> statisticsService.checkWithSynchronized(dto);
        concurrentTest(30, increaseCount);
    }

    @Transactional
    @Rollback(false)
    @Test
    void increase_ReadCommitedAndWriteLock_Test() throws InterruptedException {
        StatisticsDto dto = StatisticsDto.builder()
                .name("test")
                .count(1)
                .build();

        Runnable increaseCount = () -> statisticsService.checkWithReadCommitedAndWriteLock(dto);
        concurrentTest(30, increaseCount);
    }

    @Transactional
    @Rollback(false)
    @Test
    void increaseTest() throws InterruptedException {
        StatisticsDto dto = StatisticsDto.builder()
                .name("test")
                .count(1)
                .build();

        Runnable increaseCount = () -> statisticsService.checkWithUniQue(dto);
        concurrentTest(30, increaseCount);
    }

    void concurrentTest(int executeCount, Runnable methodToTest) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(22);
        CountDownLatch countDownLatch = new CountDownLatch(executeCount);

        for (int i = 0; i < executeCount; i++) {
            executorService.submit(() -> {
                methodToTest.run();
                countDownLatch.countDown();
            });
        }

        countDownLatch.await();
    }
}