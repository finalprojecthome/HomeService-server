package com.homeservice.homeservice_server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_counter")
public class OrderCounter {

    @Id
    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "counter", nullable = false)
    @Builder.Default
    private Integer counter = 0;
}
