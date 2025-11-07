package com.buyo.adminfx.model;

import java.time.LocalDateTime;

public class ProductLogEntry {
    private final int id;
    private final int productId;
    private final String fieldChanged;
    private final String oldValue;
    private final String newValue;
    private final LocalDateTime changedAt;

    public ProductLogEntry(int id, int productId, String fieldChanged, String oldValue, String newValue, LocalDateTime changedAt) {
        this.id = id;
        this.productId = productId;
        this.fieldChanged = fieldChanged;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedAt = changedAt;
    }

    public int getId() { return id; }
    public int getProductId() { return productId; }
    public String getFieldChanged() { return fieldChanged; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
    public LocalDateTime getChangedAt() { return changedAt; }
}

