package com.buyo.adminfx.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Coupon {
    private final int id;
    private final String code;
    private final boolean active;
    private final LocalDate expiresAt;
    private final BigDecimal percent;
    private final BigDecimal amount;
    private final BigDecimal minimum;

    public Coupon(int id, String code, boolean active, LocalDate expiresAt, BigDecimal percent, BigDecimal amount, BigDecimal minimum) {
        this.id = id; this.code = code; this.active = active; this.expiresAt = expiresAt; this.percent = percent; this.amount = amount; this.minimum = minimum;
    }
    public int getId() { return id; }
    public String getCode() { return code; }
    public boolean isActive() { return active; }
    public LocalDate getExpiresAt() { return expiresAt; }
    public BigDecimal getPercent() { return percent; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getMinimum() { return minimum; }
}
