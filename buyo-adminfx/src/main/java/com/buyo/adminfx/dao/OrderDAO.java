package com.buyo.adminfx.dao;

import com.buyo.adminfx.db.Database;
import com.buyo.adminfx.model.Order;
import com.buyo.adminfx.model.OrderItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class OrderDAO {
    public List<Order> listAll() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT p.id, p.usuario_id AS customer_id, u.nome AS customer_name, p.status, p.total, p.data_pedido AS created_at " +
                     "FROM pedidos p LEFT JOIN usuarios u ON u.id = p.usuario_id ORDER BY p.id DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Order(
                        rs.getInt("id"),
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("status"),
                        rs.getBigDecimal("total"),
                        rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null
                ));
            }
        } catch (Exception e) {
            // Sem DB configurado, retorna lista vazia
        }
        return list;
    }

    public Integer createOrder(int usuarioId) {
        String sql = "INSERT INTO pedidos (usuario_id, status, total) VALUES (?, 'CRIADO', 0)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, usuarioId);
            int rows = ps.executeUpdate();
            if (rows == 0) return null;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public List<OrderItem> listItems(int pedidoId) {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT id, pedido_id, produto_id, quantidade, preco_unitario FROM pedido_itens WHERE pedido_id = ? ORDER BY id";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pedidoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new OrderItem(
                            rs.getInt("id"),
                            rs.getInt("pedido_id"),
                            rs.getInt("produto_id"),
                            rs.getInt("quantidade"),
                            rs.getBigDecimal("preco_unitario")
                    ));
                }
            }
        } catch (Exception e) {
            // retorna vazio
        }
        return list;
    }

    public boolean addItem(int pedidoId, int produtoId, int quantidade, BigDecimal precoUnitario) {
        String sql = "INSERT INTO pedido_itens (pedido_id, produto_id, quantidade, preco_unitario) VALUES (?,?,?,?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pedidoId);
            ps.setInt(2, produtoId);
            ps.setInt(3, Math.max(1, quantidade));
            ps.setBigDecimal(4, precoUnitario);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean removeItem(int itemId) {
        String sql = "DELETE FROM pedido_itens WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean updateItem(int itemId, int quantidade, BigDecimal precoUnitario) {
        String sql = "UPDATE pedido_itens SET quantidade = ?, preco_unitario = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, quantidade));
            ps.setBigDecimal(2, precoUnitario);
            ps.setInt(3, itemId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean recalcTotal(int pedidoId) {
        String sql = "UPDATE pedidos p SET total = COALESCE((SELECT SUM(quantidade * preco_unitario) FROM pedido_itens WHERE pedido_id = ?), 0) WHERE p.id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pedidoId);
            ps.setInt(2, pedidoId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public String getCurrentStatus(int pedidoId) {
        String sql = "SELECT status FROM pedidos WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pedidoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public boolean updateStatus(int pedidoId, String novoStatus, Integer adminId) {
        String anterior = getCurrentStatus(pedidoId);
        if (anterior == null) return false;
        String sqlUpd = "UPDATE pedidos SET status = ? WHERE id = ?";
        String sqlLogHist = "INSERT INTO historico_status_pedido (pedido_id, admin_id, status_anterior, status_novo) VALUES (?,?,?,?)";
        String sqlLogLegacy = "INSERT INTO pedido_status_log (pedido_id, admin_id, status_anterior, status_novo) VALUES (?,?,?,?)";
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(sqlUpd)) {
                ps1.setString(1, novoStatus);
                ps1.setInt(2, pedidoId);
                if (ps1.executeUpdate() == 0) { conn.rollback(); return false; }
            }
            boolean logged = false;
            // tenta nova tabela primeiro
            try (PreparedStatement ps2 = conn.prepareStatement(sqlLogHist)) {
                ps2.setInt(1, pedidoId);
                if (adminId == null) ps2.setNull(2, java.sql.Types.INTEGER); else ps2.setInt(2, adminId);
                ps2.setString(3, anterior);
                ps2.setString(4, novoStatus);
                ps2.executeUpdate();
                logged = true;
            } catch (Exception ignore) { /* fallback abaixo */ }
            if (!logged) {
                try (PreparedStatement ps3 = conn.prepareStatement(sqlLogLegacy)) {
                    ps3.setInt(1, pedidoId);
                    if (adminId == null) ps3.setNull(2, java.sql.Types.INTEGER); else ps3.setInt(2, adminId);
                    ps3.setString(3, anterior);
                    ps3.setString(4, novoStatus);
                    ps3.executeUpdate();
                    logged = true;
                }
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<String[]> listStatusLog(int pedidoId) {
        List<String[]> list = new ArrayList<>();
        String sqlHist = "SELECT status_anterior, status_novo, criado_em, admin_id FROM historico_status_pedido WHERE pedido_id = ? ORDER BY id DESC";
        String sqlLegacy = "SELECT status_anterior, status_novo, criado_em, admin_id FROM pedido_status_log WHERE pedido_id = ? ORDER BY id DESC";
        try (Connection conn = Database.getConnection()) {
            boolean used = false;
            try (PreparedStatement ps = conn.prepareStatement(sqlHist)) {
                ps.setInt(1, pedidoId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(new String[]{
                                rs.getString("status_anterior"),
                                rs.getString("status_novo"),
                                String.valueOf(rs.getTimestamp("criado_em")),
                                String.valueOf(rs.getObject("admin_id"))
                        });
                    }
                    used = true;
                }
            } catch (Exception ignore) { /* tenta legacy abaixo */ }
            if (!used) {
                try (PreparedStatement ps = conn.prepareStatement(sqlLegacy)) {
                    ps.setInt(1, pedidoId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            list.add(new String[]{
                                    rs.getString("status_anterior"),
                                    rs.getString("status_novo"),
                                    String.valueOf(rs.getTimestamp("criado_em")),
                                    String.valueOf(rs.getObject("admin_id"))
                            });
                        }
                    }
                } catch (Exception ignore) {}
            }
        } catch (Exception e) { /* vazio */ }
        return list;
    }
}
