package com.buyo.adminfx.model;

import java.time.LocalDateTime;

public class Cart {
    private final int id;
    private final int userId;
    private final LocalDateTime createdAt;

    public Cart(int id, int userId, LocalDateTime createdAt) {
        this.id = id; this.userId = userId; this.createdAt = createdAt;
    }
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
