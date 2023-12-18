package com.example.concurrency.repository;

import com.example.concurrency.entity.Hotel;
import com.example.concurrency.entity.Hotel_Optimistic;
import jakarta.persistence.LockModeType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HotelOptimisticRepository extends JpaRepository<Hotel_Optimistic, Long> {
//    @Modifying // select 문이 아님을 나타낸다
//    @Query("UPDATE  Hotel_Optimistic SET restCount = restCount-1, version = version+1 WHERE id = :hotelId and version = :versionNumber")
//    void minusRestCount(@Param("hotelId") Long hotelId, @Value("versionNumber") int versionNumber);
}
