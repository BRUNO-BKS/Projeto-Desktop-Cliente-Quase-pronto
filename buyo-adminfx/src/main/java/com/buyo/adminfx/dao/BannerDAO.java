package com.buyo.adminfx.dao;

import com.buyo.adminfx.db.Database;
import com.buyo.adminfx.model.Banner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class BannerDAO {

    public List<Banner> listAll() {
        List<Banner> out = new ArrayList<>();
        String sql = "SELECT * FROM banners ORDER BY ordem ASC, id DESC";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = safeGetInt(rs, "id");
                String title = safeGetString(rs, "titulo", "title");
                String subtitle = safeGetString(rs, "subtitulo", "subtitle");
                String imageUrl = safeGetString(rs, "imagem_url", "image_url", "imageUrl");
                String linkUrl = safeGetString(rs, "link_url", "linkUrl");
                int order = safeGetInt(rs, "ordem", "order");
                boolean active = safeGetBoolean(rs, "ativo", "active");
                java.time.LocalDateTime inicio = toLdt(safeGetTimestamp(rs, "inicio", "start_at", "startAt"));
                java.time.LocalDateTime fim = toLdt(safeGetTimestamp(rs, "fim", "end_at", "endAt"));
                java.time.LocalDateTime criadoEm = toLdt(safeGetTimestamp(rs, "criado_em", "created_at"));
                java.time.LocalDateTime atualizadoEm = toLdt(safeGetTimestamp(rs, "atualizado_em", "updated_at"));

                out.add(new Banner(id, title, subtitle, imageUrl, linkUrl, order, active,
                        inicio, fim, criadoEm, atualizadoEm));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    private int safeGetInt(ResultSet rs, String... names) {
        for (String n : names) {
            try { return rs.getInt(n); } catch (Exception ignore) {}
        }
        return 0;
    }

    private String safeGetString(ResultSet rs, String... names) {
        for (String n : names) {
            try { return rs.getString(n); } catch (Exception ignore) {}
        }
        return null;
    }

    private boolean safeGetBoolean(ResultSet rs, String... names) {
        for (String n : names) {
            try { return rs.getInt(n) == 1; } catch (Exception ignore) {}
            try { return rs.getBoolean(n); } catch (Exception ignore2) {}
        }
        return false;
    }

    private Timestamp safeGetTimestamp(ResultSet rs, String... names) {
        for (String n : names) {
            try { return rs.getTimestamp(n); } catch (Exception ignore) {}
        }
        return null;
    }

    private java.time.LocalDateTime toLdt(Timestamp t) {
        return t == null ? null : t.toLocalDateTime();
    }

    public boolean insert(String title, String subtitle, String imageUrl, String linkUrl, int order, boolean active) {
        String sql = "INSERT INTO banners (titulo, subtitulo, imagem_url, link_url, ordem, ativo) VALUES (?,?,?,?,?,?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, subtitle);
            ps.setString(3, imageUrl);
            ps.setString(4, linkUrl);
            ps.setInt(5, order);
            ps.setInt(6, active ? 1 : 0);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(int id, String title, String subtitle, String imageUrl, String linkUrl, int order, boolean active) {
        String sql = "UPDATE banners SET titulo = ?, subtitulo = ?, imagem_url = ?, link_url = ?, ordem = ?, ativo = ?, atualizado_em = NOW() WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, subtitle);
            ps.setString(3, imageUrl);
            ps.setString(4, linkUrl);
            ps.setInt(5, order);
            ps.setInt(6, active ? 1 : 0);
            ps.setInt(7, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM banners WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
