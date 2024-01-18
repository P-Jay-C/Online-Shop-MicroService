package com.amalitech.org.event;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderPlacedEvent {
    private String orderNumber;
}