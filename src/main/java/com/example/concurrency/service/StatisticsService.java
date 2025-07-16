package com.example.concurrency.service;

import com.example.concurrency.dto.StatisticsDto;
import com.example.concurrency.entity.Statistics;
import com.example.concurrency.repository.StatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final StatisticsRepository statisticsRepository;
    private final StatisticsInnerService statisticsInnerService;

    public synchronized void checkWithSynchronized(StatisticsDto statisticsDto) {
        log.info("@@ check start");
        try {
            Statistics statistics = statisticsRepository.findByName(statisticsDto.getName()).orElseGet(() ->
                    statisticsInnerService.saveStatistics(statisticsDto.getName())
            );
            log.info("@@ check - statistics.getId : {}", statistics.getId());
            statistics.increaseCount(statisticsDto.getCount());
            statisticsRepository.save(statistics);
            log.info("@@ check - statistics.getCount : {}", statistics.getCount());
        } catch (LockAcquisitionException e) {
            log.error("LockAcquisitionException - {}", e.getMessage());
        } catch (RuntimeException e) {
            log.error("RuntimeException - {}", e.getMessage());
        } catch (Exception e) {
            log.error("Exception - {}", e.getMessage());
        }
        log.info("@@ check end");
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public void checkWithReadCommitedAndWriteLock(StatisticsDto statisticsDto) {
        log.info("@@ check start");
        try {
            Statistics statistics = statisticsRepository.findByName(statisticsDto.getName()).orElseGet(() ->
                    statisticsInnerService.saveStatistics(statisticsDto.getName())
            );
            log.info("@@ check - statistics.getId : {}", statistics.getId());
            statistics.increaseCount(statisticsDto.getCount());
            log.info("@@ check - statistics.getCount : {}", statistics.getCount());
        } catch (LockAcquisitionException e) {
            log.error("LockAcquisitionException - {}", e.getMessage());
        } catch (RuntimeException e) {
            log.error("RuntimeException - {}", e.getMessage());
        } catch (Exception e) {
            log.error("Exception - {}", e.getMessage());
        }
        log.info("@@ check end");
    }


    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public void checkWithUniQue(StatisticsDto statisticsDto) {
        log.info("@@ check start");
        try {
            Statistics statistics = statisticsRepository.findByName(statisticsDto.getName()).orElseGet(() ->
                    {
                        try {
                            System.out.println(" == save ");
                            return statisticsInnerService.saveStatistics(statisticsDto.getName());
                        } catch (DataIntegrityViolationException e) {
                            System.out.println(" == 재조회 ");
                            return statisticsRepository.findByName(statisticsDto.getName()).orElseThrow(); // save를 분리하지 않고 같은 트랜잭션에서 save 시동 후 DataIntegrityViolationException에서 재 find 시 id가 없는 entity가 발건됨 -> why? 1차캐시에서 세션 관리를 하기 때문에
                        }
                    }
            );
            log.info("@@ check - statistics.getId : {}", statistics.getId());
//            statistics.increaseCount(statisticsDto.getCount()); // JPA 감지 누락이 생김
            statisticsRepository.incrementCount(statisticsDto.getName(), statisticsDto.getCount()); // SQL 레벨에서 바로 증가하므로, JPA 감지 누락 없이 100% 반영됨 || 동시성 환경에서 가장 실전적
            log.info("@@ check - statistics.getCount : {}", statistics.getCount());
        } catch (LockAcquisitionException e) {
            log.error("LockAcquisitionException - {}", e.getMessage());
        } catch (RuntimeException e) {
            log.error("RuntimeException - {}", e.getMessage());
        } catch (Exception e) {
            log.error("Exception - {}", e.getMessage());
        }
        log.info("@@ check end");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Statistics saveStatistics(String name) {
        return statisticsRepository.save(
                Statistics.builder()
                        .name(name)
                        .build()
        );
    }
}
