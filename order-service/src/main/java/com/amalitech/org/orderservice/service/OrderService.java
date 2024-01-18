package com.amalitech.org.orderservice.service;

import com.amalitech.org.orderservice.dto.InventoryResponse;
import com.amalitech.org.orderservice.dto.OrderLineItemsDtoList;
import com.amalitech.org.orderservice.dto.OrderRequest;
import com.amalitech.org.orderservice.event.OrderPlacedEvent;
import com.amalitech.org.orderservice.model.Order;
import com.amalitech.org.orderservice.model.OrderLineItems;
import com.amalitech.org.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final KafkaTemplate<UUID, OrderPlacedEvent> kafkaTemplate;
    private final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    public void placeOrder(UUID key, OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoLists()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        // Call inventory service, and place order if product is in  stock
        InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get()
                .uri("http://inventory-service/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        boolean allProductsInStock = Arrays.stream(Objects.requireNonNull(inventoryResponseArray))
                .allMatch(InventoryResponse::isInStock);

        if (allProductsInStock) {
            orderRepository.save(order);

            OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent(order.getOrderNumber());
            // publish Order Placed Event
            var future = kafkaTemplate.send("notificationTopic", key, orderPlacedEvent);
            future.whenComplete((sendResult, exception) -> {
                if (exception != null){
                    LOGGER.error(exception.getMessage());
                    future.completeExceptionally(exception);
                }else {
                    future.complete(sendResult);
                }
                LOGGER.info( "Order id is : "+ orderPlacedEvent.getOrderNumber());
            });
        } else {
            throw new IllegalArgumentException("Product is not in stock, please try again later");
        }
    }

    private OrderLineItems mapToDto(OrderLineItemsDtoList orderLineItemsDtoList){
        return OrderLineItems.builder()
                .price(orderLineItemsDtoList.getPrice())
                .quantity(orderLineItemsDtoList.getQuantity())
                .skuCode(orderLineItemsDtoList.getSkuCode())
                .build();
    }
}
