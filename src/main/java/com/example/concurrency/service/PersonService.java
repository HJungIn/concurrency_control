package com.example.concurrency.service;

import com.example.concurrency.entity.Person;
import com.example.concurrency.repository.HotelRepository;
import com.example.concurrency.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;

    @Transactional
    public Person register(String name){
        return personRepository.save(Person.builder().name(name).build());
    }
}
