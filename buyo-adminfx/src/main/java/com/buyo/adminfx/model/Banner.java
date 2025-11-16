package com.buyo.adminfx.model;

import java.time.LocalDateTime;

public class Banner {
    private final int id;
    private final String title;
    private final String subtitle;
    private final String imageUrl;
    private final String linkUrl;
    private final int order;
    private final boolean active;
    private final LocalDateTime startAt;
    private final LocalDateTime endAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Banner(int id,
                  String title,
                  String subtitle,
                  String imageUrl,
                  String linkUrl,
                  int order,
                  boolean active,
                  LocalDateTime startAt,
                  LocalDateTime endAt,
                  LocalDateTime createdAt,
                  LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        this.order = order;
        this.active = active;
        this.startAt = startAt;
        this.endAt = endAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getImageUrl() { return imageUrl; }
    public String getLinkUrl() { return linkUrl; }
    public int getOrder() { return order; }
    public boolean isActive() { return active; }
    public LocalDateTime getStartAt() { return startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
