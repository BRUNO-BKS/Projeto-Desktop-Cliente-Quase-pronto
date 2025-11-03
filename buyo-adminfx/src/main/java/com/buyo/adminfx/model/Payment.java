package com.buyo.adminfx.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
    private int id;
    private int orderId;
    private BigDecimal amount;
    private String method;
    private String status;
    private String transaction;
    private LocalDateTime createdAt;

    public Payment(int id, int orderId, BigDecimal amount, String method, String status, String transaction, LocalDateTime createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.transaction = transaction;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public int getOrderId() { return orderId; }
    public BigDecimal getAmount() { return amount; }
    public String getMethod() { return method; }
    public String getStatus() { return status; }
    public String getTransaction() { return transaction; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
