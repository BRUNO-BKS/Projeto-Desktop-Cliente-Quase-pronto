package com.buyo.adminfx.dao;

import com.buyo.adminfx.db.Database;
import com.buyo.adminfx.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class ProductDAO {
    public List<Product> listAll() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.id, p.nome_produto AS name, p.preco AS price, IFNULL(e.quantidade, 0) AS stock, " +
                     "p.categoria_id AS category_id, c.nome AS category_name, p.imagem_url AS image_url, p.ativo AS active " +
                     "FROM produtos p " +
                     "LEFT JOIN estoque e ON e.produto_id = p.id " +
                     "LEFT JOIN categorias c ON c.id = p.categoria_id " +
                     "ORDER BY p.id DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                boolean active = false;
                try { active = rs.getInt("active") == 1; } catch (Exception ignore) {}
                list.add(new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getBigDecimal("price"),
                    rs.getInt("stock"),
                    (Integer) rs.getObject("category_id"),
                    rs.getString("category_name"),
                    rs.getString("image_url"),
                    active
                ));
            }
        } catch (Exception e) {
            // Sem DB configurado, retorna lista vazia
        }
        return list;
    }

    public Integer createProduct(String name, BigDecimal price, Integer categoryId, String imageUrl, int stock, boolean active) {
        if (name == null || name.isBlank() || price == null) return null;
        String sqlProd = "INSERT INTO produtos (nome_produto, preco, categoria_id, imagem_url, ativo) VALUES (?,?,?,?,?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlProd, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name.trim());
            ps.setBigDecimal(2, price);
            if (categoryId == null) ps.setNull(3, java.sql.Types.INTEGER); else ps.setInt(3, categoryId);
            ps.setString(4, imageUrl);
            ps.setInt(5, active ? 1 : 0);
            int rows = ps.executeUpdate();
            if (rows == 0) return null;
            Integer newId = null;
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) newId = keys.getInt(1); }
            if (newId != null) {
                // upsert estoque
                try (PreparedStatement psEst = conn.prepareStatement("INSERT INTO estoque (produto_id, quantidade) VALUES (?,?) ON DUPLICATE KEY UPDATE quantidade=VALUES(quantidade)")) {
                    psEst.setInt(1, newId);
                    psEst.setInt(2, stock);
                    psEst.executeUpdate();
                }
            }
            return newId;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean updateProduct(int id, String name, BigDecimal price, Integer categoryId, String imageUrl, int stock, boolean active) {
        String sql = "UPDATE produtos SET nome_produto=?, preco=?, categoria_id=?, imagem_url=?, ativo=? WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setBigDecimal(2, price);
            if (categoryId == null) ps.setNull(3, java.sql.Types.INTEGER); else ps.setInt(3, categoryId);
            ps.setString(4, imageUrl);
            ps.setInt(5, active ? 1 : 0);
            ps.setInt(6, id);
            int r1 = ps.executeUpdate();
            int r2 = 1;
            try (PreparedStatement psEst = conn.prepareStatement("INSERT INTO estoque (produto_id, quantidade) VALUES (?,?) ON DUPLICATE KEY UPDATE quantidade=VALUES(quantidade)")) {
                psEst.setInt(1, id);
                psEst.setInt(2, stock);
                r2 = psEst.executeUpdate();
            }
            return r1 > 0 && r2 > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteProduct(int id) {
        try (Connection conn = Database.getConnection()) {
            try (PreparedStatement psEst = conn.prepareStatement("DELETE FROM estoque WHERE produto_id = ?")) {
                psEst.setInt(1, id);
                psEst.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM produtos WHERE id = ?")) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public boolean adjustStock(int productId, int delta) {
        try (Connection conn = Database.getConnection()) {
            // tenta atualizar existente
            try (PreparedStatement ps = conn.prepareStatement("UPDATE estoque SET quantidade = GREATEST(0, quantidade + ?) WHERE produto_id = ?")) {
                ps.setInt(1, delta);
                ps.setInt(2, productId);
                int r = ps.executeUpdate();
                if (r > 0) return true;
            }
            // se não existir linha, insere
            int initial = Math.max(0, delta);
            try (PreparedStatement ps2 = conn.prepareStatement("INSERT INTO estoque (produto_id, quantidade) VALUES (?,?)")) {
                ps2.setInt(1, productId);
                ps2.setInt(2, initial);
                return ps2.executeUpdate() > 0;
            }
        } catch (Exception e) {
            return false;
        }
    }
}

