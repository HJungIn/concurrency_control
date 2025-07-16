package com.example.concurrency.controller;

import com.example.concurrency.dto.StatisticsDto;
import com.example.concurrency.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/v1/test")
    public String test(){
        StatisticsDto dto = StatisticsDto.builder()
                .name("test")
                .count(1)
                .build();

        statisticsService.checkWithUniQue(dto);
        return "test";
    }
}
