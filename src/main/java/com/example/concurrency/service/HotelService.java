package com.example.concurrency.service;

import com.example.concurrency.entity.Hotel;
import com.example.concurrency.entity.Person;
import com.example.concurrency.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;

    @Transactional
    public Hotel register(String name){
        return hotelRepository.save(Hotel.builder().name(name).build());
    }

    @Transactional
    public boolean reserve(Long hotelId, Person person){
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow();
        System.out.println("person.get = " + person.getName() +" , restCount = "+ hotel.getRestCount());
        if(hotel.getRestCount() > 0){
            hotelRepository.minusRestCount(hotelId);
        }
        return true;
    }


    @Transactional
    public synchronized boolean reserve_with_synchronized(Long hotelId, Person person){
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow();
        System.out.println("person.get = " + person.getName() +" , restCount = "+ hotel.getRestCount());
        if(hotel.getRestCount() > 0){
            hotelRepository.minusRestCount(hotelId);
        }
        return true;
    }

    @Transactional
    public boolean reserve_with_pessimistic_lock(Long hotelId, Person person){
        Hotel hotel = hotelRepository.findByIdLock(hotelId).orElseThrow();
        System.out.println("person.get = " + person.getName() +" , restCount = "+ hotel.getRestCount());
        if(hotel.getRestCount() > 0){
            hotelRepository.minusRestCount(hotelId);
        }
        return true;
    }

}
