package com.example.concurrency.service;

import com.example.concurrency.entity.Hotel;
import com.example.concurrency.entity.Hotel_Optimistic;
import com.example.concurrency.entity.Person;
import com.example.concurrency.repository.HotelOptimisticRepository;
import com.example.concurrency.repository.HotelRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HotelServiceTest {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private HotelOptimisticService hotelOptimisticService;

    @Autowired
    private HotelRepository hotelRepository;
    @Autowired
    private HotelOptimisticRepository hotelOptimisticRepository;

    @Autowired
    private PersonService personService;

    @Test
    void 기본_남은_restCount가_minus가_되는_경우() throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2); // 동시성 테스트를 위한

        Person p_1 = personService.register("기본_p_1");
        Person p_2 = personService.register("기본_p_2");
        Hotel hotel = hotelService.register("기본_hotel");

        System.out.println("======== 동시성 테스트 진행 ========");
        service.execute(() -> {
            hotelService.reserve(hotel.getId(), p_1);
            latch.countDown();
        });
        service.execute(() -> {
            hotelService.reserve(hotel.getId(), p_2);
            latch.countDown();
        });
        latch.await();

        System.out.println("======== 동시성 테스트 결과 ========");
        Hotel result = hotelRepository.findById(hotel.getId()).orElseThrow();
        System.out.println("호텔의 getRestCount() = " + result.getRestCount());
        assertNotEquals(result.getRestCount(), 0);

    }

    @Test
    void synchronized사용_남은_restCount가_0이_되는_경우() throws InterruptedException { // local에서는 0으로 나오지만, server가 여러대인 경우에는 minus가 될 수 있다.
        ExecutorService service = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2); // 동시성 테스트를 위한

        Person p_1 = personService.register("synchronized사용_p_1");
        Person p_2 = personService.register("synchronized사용_p_2");
        Hotel hotel = hotelService.register("synchronized사용_hotel");

        System.out.println("======== 동시성 테스트 진행 ========");
        service.execute(() -> {
            hotelService.reserve_with_synchronized(hotel.getId(), p_1);
            latch.countDown();
        });
        service.execute(() -> {
            hotelService.reserve_with_synchronized(hotel.getId(), p_2);
            latch.countDown();
        });
        latch.await();

        System.out.println("======== 동시성 테스트 결과 ========");
        Hotel result = hotelRepository.findById(hotel.getId()).orElseThrow();
        System.out.println("호텔의 getRestCount() = " + result.getRestCount());
        assertEquals(result.getRestCount(), 0);

    }

    @Test
    void 낙관적_lock사용() throws InterruptedException { // 꼭 try-catch 해주기
        ExecutorService service = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2); // 동시성 테스트를 위한

        Person p_1 = personService.register("낙관적 lock 사용_p_1");
        Person p_2 = personService.register("낙관적 lock 사용_p_2");
        Hotel_Optimistic hotel = hotelOptimisticService.register("낙관적 lock 사용_hotel");

        System.out.println("======== 동시성 테스트 진행 ========");
        service.execute(() -> {
            try {
                hotelOptimisticService.reserve_with_optimistic_lock(hotel.getId(), p_1);
            } catch (Exception e) {
                e.printStackTrace(); // 여기서 catch 해주기
            } finally {
                latch.countDown();
            }
        });
        service.execute(() -> {
            try {
                hotelOptimisticService.reserve_with_optimistic_lock(hotel.getId(), p_2);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
        latch.await();

        System.out.println("======== 동시성 테스트 결과 ========");
        Hotel_Optimistic result = hotelOptimisticRepository.findById(hotel.getId()).orElseThrow();
        System.out.println("호텔의 getRestCount() = " + result.getRestCount());
        assertEquals(0, result.getRestCount());
    }

    @Test
    void 비관적_lock사용() throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2); // 동시성 테스트를 위한

        Person p_1 = personService.register("비관적 lock 사용_p_1");
        Person p_2 = personService.register("비관적 lock 사용_p_2");
        Hotel hotel = hotelService.register("비관적 lock 사용_hotel");

        System.out.println("======== 동시성 테스트 진행 ========");
        service.execute(() -> {
            hotelService.reserve_with_pessimistic_lock(hotel.getId(), p_1);
            latch.countDown();
        });
        service.execute(() -> {
            hotelService.reserve_with_pessimistic_lock(hotel.getId(), p_2);
            latch.countDown();
        });
        latch.await();

        System.out.println("======== 동시성 테스트 결과 ========");
        Hotel result = hotelRepository.findById(hotel.getId()).orElseThrow();
        System.out.println("호텔의 getRestCount() = " + result.getRestCount());
        assertEquals(result.getRestCount(), 0);
    }
}