package com.example.orderservice.controller;

import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.jsonwebtoken.Claims;


import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUserId(@RequestHeader("Authorization") String authHeader, @PathVariable String userId) {
        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtService.verifyToken(token);
        if (claims == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping
    public ResponseEntity<Page<Order>> getAllOrders(@RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtService.verifyToken(token);
        if (claims == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Page<Order> orders = orderService.getAllOrders(page, size);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{orderId}")
    public ResponseEntity<Order> getOrderStatusByOrderId(@RequestHeader("Authorization") String authHeader, @PathVariable Long orderId) {
        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtService.verifyToken(token);
        if (claims == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<Order> optionalOrder = orderService.getOrderById(orderId);
        return optionalOrder.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@RequestHeader("Authorization") String authHeader, @PathVariable Long orderId) {
        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtService.verifyToken(token);
        if (claims == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<Order> optionalOrder = orderService.getOrderById(orderId);
        if(!optionalOrder.isPresent()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(optionalOrder.get());
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestHeader("Authorization") String authHeader, @RequestBody Order order) {
        for (OrderItem item : order.getItems()) {
            item.setOrder(order);
        }
        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtService.verifyToken(token);
        Integer userId = claims.get("id", Integer.class);
        order.setUserId(String.valueOf(userId));
        Order createdOrder = orderService.createOrder(order);
        return ResponseEntity.ok(createdOrder);
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(@RequestHeader("Authorization") String authHeader, @PathVariable Long orderId) {
        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtService.verifyToken(token);
        if (claims == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<Order> optionalOrder = orderService.cancelOrder(orderId);
        return optionalOrder.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<Order> updateOrder(@RequestHeader("Authorization") String authHeader, @PathVariable Long orderId, @RequestBody Order updatedOrder) {
        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtService.verifyToken(token);
        if(claims == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<Order> optionalOrder = orderService.updateOrder(orderId, updatedOrder.getItems());
        return optionalOrder.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/addProduct/{orderId}")
    public ResponseEntity<Order> addItemToOrder(@RequestHeader("Authorization") String authHeader, @PathVariable Long orderId, @RequestBody OrderItem newItem) {
        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtService.verifyToken(token);
        if (claims == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<Order> optionalAddedOrder = orderService.addOrderItemToOrder(orderId, newItem);
        return optionalAddedOrder.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<Order> removeItemFromOrder(@RequestHeader("Authorization") String authHeader,
            @PathVariable Long orderId,
            @PathVariable Long itemId) {
        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtService.verifyToken(token);
        if (claims == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<Order> updatedOrder = orderService.removeItemFromOrder(orderId, itemId);

        return updatedOrder
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> removeOrder(@RequestHeader("Authorization") String authHeader, @PathVariable Long orderId){
        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtService.verifyToken(token);
        if (claims == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean removed = orderService.removeOrderById(orderId);

        if(removed){
            return ResponseEntity.ok("Order with ID " + orderId + " has been removed successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order with ID " + orderId + " not found.");
        }
    }
}
