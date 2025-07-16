package com.example.concurrency.repository;

import com.example.concurrency.entity.Statistics;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StatisticsRepository extends JpaRepository<Statistics, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Statistics> findByName(String name);

    @Modifying
    @Query("UPDATE Statistics s SET s.count = s.count + :count WHERE s.name = :name")
    void incrementCount(@Param("name") String name, @Param("count") int count);
}
