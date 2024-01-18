package com.amalitech.org;

import com.amalitech.org.event.OrderPlacedEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

    @KafkaListener(topics = {"notificationTopic"}, groupId = "notificationId")
    public void handleNotification(OrderPlacedEvent orderPlacedEvent) {
        log.info("Got message <{}>", orderPlacedEvent);
        log.info("Received Notification for Order - {}", orderPlacedEvent.getOrderNumber());
        // send out an email notification or perform other processing
    }
}
