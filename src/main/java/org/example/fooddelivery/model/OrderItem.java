package org.example.fooddelivery.model;

import java.math.BigDecimal;

public class OrderItem {
    private int id;
    private int orderId;
    private Product product;
    private int quantity;

    public OrderItem(int id, int orderId, Product product, int quantity) {
        this.id = id;
        this.orderId = orderId;
        this.product = product;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Product getProduct() {
        return product;
    }
    public void setProduct(Product product) {
        this.product = product;
    }

    public int getProductId() {
        return product != null ? product.getId() : 0;
    }

    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        if (product != null && product.getPrice() != null) {
            return product.getPrice().multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        String productName = product != null ? product.getName() : "Unknown Product";
        return String.format("%dx %s", quantity, productName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return id == orderItem.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}