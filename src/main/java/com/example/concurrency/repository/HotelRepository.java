package com.example.concurrency.repository;

import com.example.concurrency.entity.Hotel;
import com.example.concurrency.entity.Person;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
    @Modifying // select 문이 아님을 나타낸다
    @Query("UPDATE  Hotel SET restCount = restCount-1 WHERE id = :hotelId")
    void minusRestCount(@Param("hotelId") Long hotelId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM Hotel h WHERE h.id = :hotelId")
    Optional<Hotel> findByIdLock(@Param("hotelId") Long hotelId);
}
