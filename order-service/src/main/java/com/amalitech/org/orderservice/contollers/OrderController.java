package com.amalitech.org.orderservice.contollers;

import com.amalitech.org.orderservice.dto.OrderRequest;
import com.amalitech.org.orderservice.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> placeOrder(@RequestBody OrderRequest orderRequest) {
        UUID uuid = UUID.randomUUID();

        log.info("Placing order");
        orderService.placeOrder(uuid ,orderRequest);
        return ResponseEntity.ok("Order Sent");
    }
}
