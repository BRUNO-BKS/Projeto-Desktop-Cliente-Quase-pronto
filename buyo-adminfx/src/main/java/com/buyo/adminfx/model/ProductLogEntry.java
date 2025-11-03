package com.buyo.adminfx.model;

import java.time.LocalDateTime;

public class ProductLogEntry {
    private final int id;
    private final int productId;
    private final String action;
    private final int quantity;
    private final Integer adminId;
    private final Integer orderId;
    private final String note;
    private final LocalDateTime createdAt;

    public ProductLogEntry(int id, int productId, String action, int quantity, Integer adminId, Integer orderId, String note, LocalDateTime createdAt) {
        this.id = id;
        this.productId = productId;
        this.action = action;
        this.quantity = quantity;
        this.adminId = adminId;
        this.orderId = orderId;
        this.note = note;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public int getProductId() { return productId; }
    public String getAction() { return action; }
    public int getQuantity() { return quantity; }
    public Integer getAdminId() { return adminId; }
    public Integer getOrderId() { return orderId; }
    public String getNote() { return note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
