package com.buyo.adminfx.model;

import java.math.BigDecimal;

public class CartItem {
    private final int id;
    private final int cartId;
    private final int productId;
    private final int quantity;
    private final BigDecimal price;

    public CartItem(int id, int cartId, int productId, int quantity, BigDecimal price) {
        this.id = id; this.cartId = cartId; this.productId = productId; this.quantity = quantity; this.price = price;
    }
    public int getId() { return id; }
    public int getCartId() { return cartId; }
    public int getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; }
}
