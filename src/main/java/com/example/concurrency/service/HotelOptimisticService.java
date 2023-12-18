package com.example.concurrency.service;

import com.example.concurrency.entity.Hotel;
import com.example.concurrency.entity.Hotel_Optimistic;
import com.example.concurrency.entity.Person;
import com.example.concurrency.repository.HotelOptimisticRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelOptimisticService {

    private final HotelOptimisticRepository hotelOptimisticRepository;

    @Transactional
    public Hotel_Optimistic register(String name) {
        return hotelOptimisticRepository.save(Hotel_Optimistic.builder().name(name).build());
    }

    @Transactional
    public boolean reserve_with_optimistic_lock(Long hotelId, Person person) {
        Hotel_Optimistic hotel = hotelOptimisticRepository.findById(hotelId).orElseThrow(RuntimeException::new); // @Version을 갖고 있어서 자동으로 Optimistic으로 Lock걸린다.
        System.out.println("person.get = " + person.getName() + " , restCount = " + hotel.getRestCount());
        hotel.minusRestCount(1);
        return true;
    }

}
