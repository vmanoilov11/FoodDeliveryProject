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

    public int getUserId() {
        return user.getId();
    }

    public int getRestaurantId() {
        return restaurant.getId();
    }

    public Restaurant getRestaurant() {
        return restaurant;
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
        return String.format("Order #%d (%s) - Restaurant ID: %d", id, status, restaurant.getId());
    }

    public Restaurant getRestaurant(List<Restaurant> allRestaurants) {
        for (Restaurant restaurant : allRestaurants) {
            if (restaurant.getId() == this.restaurant.getId()) {
                return restaurant;
            }
        }
        return null;
    }

    public List<OrderItem> getOrderItems() {
        return items;
    }

    public User getUser() {
        return user;
    }
}
