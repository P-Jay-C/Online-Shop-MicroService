package com.amalitech.org.orderservice.service;

import com.amalitech.org.orderservice.dto.OrderLineItemsDtoList;
import com.amalitech.org.orderservice.dto.OrderRequest;
import com.amalitech.org.orderservice.model.Order;
import com.amalitech.org.orderservice.model.OrderLineItems;
import com.amalitech.org.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    public void placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoLists()
                .stream()
                .map(this::mapToDto)
                .toList();
        order.setOrderLineItemsList(orderLineItems);
        orderRepository.save(order);

    }

    private OrderLineItems mapToDto(OrderLineItemsDtoList orderLineItemsDtoList){
        return OrderLineItems.builder()
                .price(orderLineItemsDtoList.getPrice())
                .quantity(orderLineItemsDtoList.getQuantity())
                .skuCode(orderLineItemsDtoList.getSkuCode())
                .build();
    }
}
