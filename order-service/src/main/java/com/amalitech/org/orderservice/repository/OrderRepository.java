package com.amalitech.org.orderservice.repository;

import com.amalitech.org.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
