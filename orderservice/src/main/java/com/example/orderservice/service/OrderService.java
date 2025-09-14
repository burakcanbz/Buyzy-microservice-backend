package com.example.orderservice.service;

import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.logger.LoggerService;
import com.example.orderservice.dto.OrderUpdatedDTO;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.messaging.OrderPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderPublisher orderPublisher;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private LoggerService logger;

    private ObjectMapper mapper = new ObjectMapper();

    public List<Order> getOrdersByUserId(String userId) {
        logger.info("order getting by user id", userId);
        return orderRepository.findByUserId(userId);
    }

    public Page<Order> getAllOrders(int page, int size) {
        logger.info("all orders getting !");
        return orderRepository.findAll(PageRequest.of(page, size));
    }

    public Optional<Order> getOrderById(Long orderId) {
        logger.info("order getting by id", orderId);
        return orderRepository.findById(orderId);
    }

    public Optional<OrderStatus> getOrderStatusByOrderId(Long orderId) {
        logger.info("order status getting by order id", orderId);
        return orderRepository.findById(orderId).map(Order::getStatus);
    }

    public Order createOrder(Order order) {
        order.setStatus(OrderStatus.CREATED);
        logger.info("order creating !");
        Order createdOrder = orderRepository.save(order);

        try {
            String json = mapper.writeValueAsString(createdOrder);
            orderPublisher.publishOrderCreated(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.error("Failed to publish order created event with order id: " + createdOrder.getId(), e);        }

        return createdOrder;
    }

    public Optional<Order> addOrderItemToOrder(Long orderId, OrderItem newItem){
        Optional <Order> optionalOrder = orderRepository.findById(orderId);

        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.addItem(newItem);
            Order itemAddedOrder = orderRepository.save(order);
            try{
                String orderItemJson = mapper.writeValueAsString(newItem);
                orderPublisher.publishOrderItemAdded(orderItemJson);
            }
            catch (Exception e) {
                logger.error("Failed to publish orderItem added event with orderItem id: " + newItem.getId(), e);
            }
            logger.info("Item added to order: " + orderId);
            return Optional.of(itemAddedOrder);
        }
        logger.warn("Order item not found : " + orderId);
        return Optional.empty();
    }

    public Optional<Order> updateOrder(Long orderId, List<OrderItem> updatedItems) {
        Optional<Order> optionalExistingOrder = orderRepository.findById(orderId);
        if (optionalExistingOrder.isPresent()) {
            Order existingOrder = optionalExistingOrder.get();
            List<OrderItem> oldItems = existingOrder.getItems().stream()
                    .map(item -> new OrderItem(item.getProductId(), item.getQuantity()))
                    .collect(Collectors.toList());
            List<OrderItem> currentItems = existingOrder.getItems();
            currentItems.removeIf(item -> updatedItems.stream()
                    .noneMatch(u -> u.getId().equals(item.getId())));
            for (OrderItem updatedItem : updatedItems) {
                Optional<OrderItem> existingItemOpt = currentItems.stream()
                        .filter(i -> i.getId().equals(updatedItem.getId()))
                        .findFirst();
                if (existingItemOpt.isPresent()) {
                    OrderItem existingItem = existingItemOpt.get();
                    existingItem.setQuantity(updatedItem.getQuantity());
                } else {
                    currentItems.add(updatedItem);
                }
            }
            orderRepository.save(existingOrder);
            OrderUpdatedDTO updatedOrderDto = new OrderUpdatedDTO.Builder()
                    .orderId(existingOrder.getId())
                    .userId(existingOrder.getUserId())
                    .status(existingOrder.getStatus())
                    .existingItems(oldItems)
                    .updatedItems(updatedItems)
                    .build();
            try {
                String orderItemJson = mapper.writeValueAsString(updatedOrderDto);
                orderPublisher.publishOrderUpdated(orderItemJson);
            } catch (Exception e) {
                logger.error("Failed to publish order updated event", updatedOrderDto, e);
            }
            return Optional.of(existingOrder);
        }
        logger.warn("Order not found: " + orderId);
        return Optional.empty();
    }

    public Optional<Order> cancelOrder(Long orderId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setStatus(OrderStatus.CANCELED);
            Order canceledOrder = orderRepository.save(order);
            try{
                String json = mapper.writeValueAsString(canceledOrder);
                orderPublisher.publishOrderCanceled(json);
            }
            catch(Exception e) {
                logger.error("Failed to publish order canceled event with order id: " + canceledOrder.getId() , e);
            }

            logger.info("Order cancelled: " + orderId);
            return Optional.of(canceledOrder);
        }
        logger.warn("Order not found: " + orderId);
        return Optional.empty(); // sipariş bulunmazsa boş dön
    }

    public Optional<Order> removeItemFromOrder(Long orderId, Long itemId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            OrderItem itemToRemove = order.getItems().stream()
                    .filter(item -> item.getId().equals(itemId))
                    .findFirst()
                    .orElse(null);
            if (itemToRemove != null) {
                order.removeItem(itemToRemove);
                logger.info("Item removed from order: " + itemId);
                Order updatedOrder = orderRepository.save(order);
                try {
                    String json = mapper.writeValueAsString(updatedOrder);
                    orderPublisher.publishOrderUpdated(json);
                } catch (Exception e) {
                    logger.error("Failed to publish order updated event with order id: " + updatedOrder.getId(), e);
                }
                return Optional.of(updatedOrder);
            } else {
                logger.warn("Item not found in order: " + itemId);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public boolean removeOrderById(Long orderId){
        Optional<Order> optionalDeletingOrder = orderRepository.findById(orderId);
        try{
            String json = mapper.writeValueAsString(optionalDeletingOrder.get());
            orderPublisher.publishOrderDeleted(json);
        }
        catch (Exception e) {
            logger.error("Failed to publish order delete event with order id:" + optionalDeletingOrder.get().getId() , e);
        }
        if (optionalDeletingOrder.isPresent()) {
            orderRepository.delete(optionalDeletingOrder.get());
            return true;
        }
        return false;
    }
}
