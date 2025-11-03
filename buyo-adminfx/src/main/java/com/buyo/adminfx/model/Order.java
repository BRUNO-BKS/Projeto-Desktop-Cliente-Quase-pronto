package com.buyo.adminfx.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Order {
    private int id;
    private int customerId;
    private String customerName;
    private String status;
    private BigDecimal total;
    private LocalDateTime createdAt;

    public Order() {}

    public Order(int id, int customerId, BigDecimal total, LocalDateTime createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.total = total;
        this.createdAt = createdAt;
    }

    public Order(int id, int customerId, String customerName, String status, BigDecimal total, LocalDateTime createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.status = status;
        this.total = total;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
