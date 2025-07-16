package com.example.concurrency.service;

import com.example.concurrency.entity.Statistics;
import com.example.concurrency.repository.StatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsInnerService {
    private final StatisticsRepository statisticsRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Statistics saveStatistics(String name) {
        return statisticsRepository.save(
                Statistics.builder()
                        .name(name)
                        .build()
        );
    }

}
