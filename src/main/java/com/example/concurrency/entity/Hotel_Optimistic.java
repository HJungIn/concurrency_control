package com.example.concurrency.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Hotel_Optimistic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Builder.Default
    private int restCount = 1;

    // 낙관적 락을 사용하기 위한 version
    @Version
    private Integer version;

    public void minusRestCount(int quantity) {

            if (this.restCount - quantity < 0) {
                throw new IllegalArgumentException("재고가 부족합니다.");
            }
            this.restCount -= quantity;
    }

}
