package com.example.orderservice.dto;

import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.model.OrderStatus;
import java.util.List;

public class OrderUpdatedDTO {

    private Long orderId;
    private String userId;
    private OrderStatus status;
    private Order order;
    private List<OrderItem> existingItems;
    private List<OrderItem> updatedItems;

    private OrderUpdatedDTO(Builder builder) {
        this.orderId = builder.orderId;
        this.userId = builder.userId;
        this.status = builder.status;
        this.order = builder.order;
        this.existingItems = builder.existingItems;
        this.updatedItems = builder.updatedItems;
    }

    public Long getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public OrderStatus getStatus() { return status; }
    public Order getOrder() { return order; }
    public List<OrderItem> getExistingItems() { return existingItems; }
    public List<OrderItem> getUpdatedItems() { return updatedItems; }

    public static class Builder {
        private Long orderId;
        private String userId;
        private OrderStatus status;
        private Order order;
        private List<OrderItem> existingItems;
        private List<OrderItem> updatedItems;

        public Builder orderId(Long orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public Builder order(Order order) {
            this.order = order;
            return this;
        }

        public Builder existingItems(List<OrderItem> existingItems) {
            this.existingItems = existingItems;
            return this;
        }

        public Builder updatedItems(List<OrderItem> updatedItems) {
            this.updatedItems = updatedItems;
            return this;
        }

        public OrderUpdatedDTO build() {
            return new OrderUpdatedDTO(this);
        }
    }
}
