package org.example.fooddelivery.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id;
    private User user;
    private Restaurant restaurant;
    private String status;
    private LocalDateTime orderDate;
    private List<OrderItem> items;

    public Order(int id, User user, Restaurant restaurant, List<OrderItem> items, LocalDateTime orderDate, String status) {
        this.id = id;
        this.user = user;
        this.restaurant = restaurant;
        this.items = items != null ? items : new ArrayList<>();
        this.orderDate = orderDate;
        this.status = status;
    }

    // Getters and setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public int getUserId() {
        return user != null ? user.getId() : 0;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }
    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public int getRestaurantId() {
        return restaurant != null ? restaurant.getId() : 0;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }
    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public List<OrderItem> getItems() {
        return items;
    }
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public void addOrderItem(OrderItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
    }

    public void removeOrderItem(OrderItem item) {
        if (items != null) {
            items.remove(item);
        }
    }

    @Override
    public String toString() {
        return String.format("Order #%d (%s) - %s", id, status, restaurant != null ? restaurant.getName() : "Unknown Restaurant");
    }
}