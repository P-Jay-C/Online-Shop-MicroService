package com.amalitech.org.orderservice.service;

import com.amalitech.org.orderservice.dto.InventoryResponse;
import com.amalitech.org.orderservice.dto.OrderLineItemsDtoList;
import com.amalitech.org.orderservice.dto.OrderRequest;
import com.amalitech.org.orderservice.model.Order;
import com.amalitech.org.orderservice.model.OrderLineItems;
import com.amalitech.org.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
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
    public void placeOrder(OrderRequest orderRequest){
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

        boolean allProductInStock = Arrays.stream(Objects.requireNonNull(inventoryResponseArray)).allMatch(InventoryResponse::isInStock);

        if (allProductInStock) {
            orderRepository.save(order);
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
