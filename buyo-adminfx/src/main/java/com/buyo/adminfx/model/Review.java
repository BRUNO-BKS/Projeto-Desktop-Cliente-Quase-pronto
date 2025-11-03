package com.buyo.adminfx.model;

import java.time.LocalDateTime;

public class Review {
    private final int id;
    private final int productId;
    private final int userId;
    private final int rating;
    private final String comment;
    private final String status; // PENDENTE, APROVADO, REJEITADO
    private final LocalDateTime createdAt;

    public Review(int id, int productId, int userId, int rating, String comment, String status, LocalDateTime createdAt) {
        this.id = id; this.productId = productId; this.userId = userId; this.rating = rating; this.comment = comment; this.status = status; this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public int getProductId() { return productId; }
    public int getUserId() { return userId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
