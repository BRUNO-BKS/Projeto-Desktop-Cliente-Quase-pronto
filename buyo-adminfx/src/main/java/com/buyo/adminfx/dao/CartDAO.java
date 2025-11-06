package com.buyo.adminfx.dao;

import com.buyo.adminfx.db.Database;
import com.buyo.adminfx.model.Cart;
import com.buyo.adminfx.model.CartItem;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CartDAO {
    public List<Cart> listCarts(Integer userId) {
        List<Cart> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder("SELECT id, usuario_id, criado_em FROM carrinho WHERE 1=1");
        if (userId != null) sb.append(" AND usuario_id = ?");
        sb.append(" ORDER BY id DESC");
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sb.toString())) {
            if (userId != null) ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Cart(
                            rs.getInt("id"),
                            rs.getInt("usuario_id"),
                            toLdt(rs.getTimestamp("criado_em"))
                    ));
                }
            }
        } catch (Exception e) { }
        return out;
    }

    public List<CartItem> listItems(int cartId) {
        List<CartItem> out = new ArrayList<>();
        String sql = "SELECT ci.id, ci.carrinho_id, ci.produto_id, ci.quantidade, p.preco AS preco " +
                     "FROM carrinho_itens ci JOIN produtos p ON p.id = ci.produto_id " +
                     "WHERE ci.carrinho_id = ? ORDER BY ci.id";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new CartItem(
                            rs.getInt("id"),
                            rs.getInt("carrinho_id"),
                            rs.getInt("produto_id"),
                            rs.getInt("quantidade"),
                            rs.getBigDecimal("preco")
                    ));
                }
            }
        } catch (Exception e) { }
        return out;
    }

    public Integer convertToOrder(int cartId) {
        String getCart = "SELECT usuario_id FROM carrinho WHERE id = ?";
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            Integer userId = null;
            try (PreparedStatement ps = c.prepareStatement(getCart)) {
                ps.setInt(1, cartId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) userId = rs.getInt(1);
                }
            }
            if (userId == null) { c.rollback(); return null; }

            OrderDAO orderDAO = new OrderDAO();
            Integer orderId = orderDAO.createOrder(userId);
            if (orderId == null) { c.rollback(); return null; }

            // carregar itens e inserir como pedido_itens
            List<CartItem> items = listItems(cartId);
            for (CartItem it : items) {
                boolean ok = orderDAO.addItem(orderId, it.getProductId(), it.getQuantity(),
                        it.getPrice() == null ? BigDecimal.ZERO : it.getPrice());
                if (!ok) { c.rollback(); return null; }
            }
            orderDAO.recalcTotal(orderId);

            // limpar carrinho
            try (PreparedStatement delItems = c.prepareStatement("DELETE FROM carrinho_itens WHERE carrinho_id = ?")) {
                delItems.setInt(1, cartId);
                delItems.executeUpdate();
            }
            try (PreparedStatement delCart = c.prepareStatement("DELETE FROM carrinho WHERE id = ?")) {
                delCart.setInt(1, cartId);
                delCart.executeUpdate();
            }
            c.commit();
            return orderId;
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime toLdt(Timestamp t) { return t == null ? null : t.toLocalDateTime(); }
}
