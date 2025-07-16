package com.example.concurrency.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StatisticsDto {
    private String name;
    private int count;
}
