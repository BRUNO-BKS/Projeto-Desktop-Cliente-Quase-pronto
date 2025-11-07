package com.buyo.adminfx.model;

import java.time.LocalDateTime;

public class ProdLogEntry {
    private final int id;
    private final int productId;
    private final String action;
    private final Integer adminId;
    private final LocalDateTime createdAt;

    public ProdLogEntry(int id, int productId, String action, Integer adminId, LocalDateTime createdAt) {
        this.id = id;
        this.productId = productId;
        this.action = action;
        this.adminId = adminId;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public int getProductId() { return productId; }
    public String getAction() { return action; }
    public Integer getAdminId() { return adminId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
