package com.example.concurrency.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "name_unique", columnNames = {"name"})
})
public class Statistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Builder.Default
    private int count = 0;

    public void increaseCount(int count) {
        this.count =  this.count + count;
    }
    public void decreaseCount(int count) {
        this.count =   this.count - count;
    }
}
