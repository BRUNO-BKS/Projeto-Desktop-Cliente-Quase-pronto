package com.buyo.adminfx.dao;

import com.buyo.adminfx.db.Database;
import com.buyo.adminfx.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

public class UserDAO {
    private String lastError;

    public String getLastError() {
        return lastError;
    }

    public boolean ensureUsersTable() {
        String ddl = "CREATE TABLE IF NOT EXISTS usuarios (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "nome VARCHAR(120) NOT NULL, " +
                "email VARCHAR(180) NOT NULL, " +
                "senha_hash VARCHAR(100) NOT NULL, " +
                "telefone VARCHAR(30), " +
                "is_admin TINYINT(1) NOT NULL DEFAULT 0, " +
                "UNIQUE KEY uk_usuarios_email (email)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(ddl)) {
            ps.execute();
            return true;
        } catch (SQLException e) {
            lastError = e.getMessage();
            return false;
        }
    }
    public User authenticate(String emailOrName, String password) {
        String input = emailOrName == null ? "" : emailOrName.trim();
        if (input.isEmpty()) return null;
        boolean looksEmail = input.contains("@");
        String sql = looksEmail
                ? "SELECT id, nome, email, is_admin, senha_hash, telefone, foto_url FROM usuarios WHERE email = ? LIMIT 1"
                : "SELECT id, nome, email, is_admin, senha_hash, telefone, foto_url FROM usuarios WHERE nome = ? LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, input);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String nome = rs.getString("nome");
                    String mail = rs.getString("email");
                    boolean admin = rs.getInt("is_admin") == 1;
                    String hash = rs.getString("senha_hash");
                    if (hash == null) return null;

                    String pass = (password == null) ? "" : password;
                    boolean ok = false;

                    boolean looksBcrypt = (hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"))
                            && hash.length() >= 56 && hash.length() <= 80;
                    if (looksBcrypt) {
                        try {
                            ok = BCrypt.checkpw(pass, hash);
                        } catch (Exception ignore) {
                            ok = false; // malformed hash -> treat as invalid
                        }
                    } else {
                        // Fallback: plain-text compare if your DB stores plaintext
                        ok = hash.equals(pass);
                    }

                    if (!ok) return null;
                    User u = new User(id, nome, mail, admin);
                    try { u.setPhone(rs.getString("telefone")); } catch (Exception ignore) {}
                    try { u.setPhotoUrl(rs.getString("foto_url")); } catch (Exception ignore) {}
                    return u;
                }
            }
        } catch (Exception e) {
            lastError = e.getMessage();
        }
        return null;
    }

    public boolean createAdmin(String name, String email, String rawPassword) {
        String nm = name == null ? "" : name.trim();
        String em = email == null ? "" : email.trim();
        String pw = rawPassword == null ? "" : rawPassword;
        if (nm.isEmpty() || em.isEmpty() || pw.isEmpty()) return false;
        String hash = BCrypt.hashpw(pw, BCrypt.gensalt(10));
        String sql = "INSERT INTO usuarios (nome, email, senha_hash, is_admin) VALUES (?,?,?,1) " +
                     "ON DUPLICATE KEY UPDATE nome=VALUES(nome), senha_hash=VALUES(senha_hash), is_admin=1";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nm);
            ps.setString(2, em);
            ps.setString(3, hash);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            lastError = e.getMessage();
            return false;
        }
    }

    public boolean createClient(String name, String email, String rawPassword) {
        String nm = name == null ? "" : name.trim();
        String em = email == null ? "" : email.trim();
        String pw = rawPassword == null ? "" : rawPassword;
        if (nm.isEmpty() || em.isEmpty() || pw.isEmpty()) return false;
        String hash = BCrypt.hashpw(pw, BCrypt.gensalt(10));
        String sql = "INSERT INTO usuarios (nome, email, senha_hash, is_admin) VALUES (?,?,?,0) " +
                     "ON DUPLICATE KEY UPDATE nome=VALUES(nome), senha_hash=VALUES(senha_hash)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nm);
            ps.setString(2, em);
            ps.setString(3, hash);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean hasAnyAdmin() {
        String sql = "SELECT 1 FROM usuarios WHERE is_admin = 1 LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next();
        } catch (SQLException e) {
            lastError = e.getMessage();
            return false;
        }
    }

    public boolean ensureUserProfileColumns() {
        try (Connection conn = Database.getConnection()) {
            // telefone
            try (PreparedStatement chk = conn.prepareStatement(
                    "SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'usuarios' AND COLUMN_NAME = 'telefone'")) {
                try (ResultSet rs = chk.executeQuery()) {
                    if (!rs.next()) {
                        try (PreparedStatement add = conn.prepareStatement(
                                "ALTER TABLE usuarios ADD COLUMN telefone VARCHAR(30)")) {
                            add.execute();
                        }
                    }
                }
            }
            // foto_url
            try (PreparedStatement chk = conn.prepareStatement(
                    "SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'usuarios' AND COLUMN_NAME = 'foto_url'")) {
                try (ResultSet rs = chk.executeQuery()) {
                    if (!rs.next()) {
                        try (PreparedStatement add = conn.prepareStatement(
                                "ALTER TABLE usuarios ADD COLUMN foto_url VARCHAR(500)")) {
                            add.execute();
                        }
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            lastError = e.getMessage();
            return false;
        }
    }

    public boolean promoteOrResetAdmin(String name, String email, String rawPassword) {
        String nm = name == null ? "" : name.trim();
        String em = email == null ? "" : email.trim();
        String pw = rawPassword == null ? "" : rawPassword;
        if (em.isEmpty() || pw.isEmpty()) return false;
        String hash = BCrypt.hashpw(pw, BCrypt.gensalt(10));
        String sql = "UPDATE usuarios SET nome = COALESCE(?, nome), senha_hash = ?, is_admin = 1 WHERE email = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nm.isEmpty() ? null : nm);
            ps.setString(2, hash);
            ps.setString(3, em);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            lastError = e.getMessage();
            return false;
        }
    }

    public boolean existsByEmail(String email) {
        String em = email == null ? "" : email.trim();
        if (em.isEmpty()) return false;
        String sql = "SELECT 1 FROM usuarios WHERE email = ? LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, em);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
            return false;
        }
    }

    public User getById(int id) {
        String sql = "SELECT id, nome, email, is_admin, telefone, foto_url FROM usuarios WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User(rs.getInt("id"), rs.getString("nome"), rs.getString("email"), rs.getInt("is_admin") == 1);
                    u.setPhone(rs.getString("telefone"));
                    u.setPhotoUrl(rs.getString("foto_url"));
                    return u;
                }
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return null;
    }

    public boolean updateProfile(int id, String name, String phone, String photoUrl) {
        String sql = "UPDATE usuarios SET nome = ?, telefone = ?, foto_url = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, photoUrl);
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            lastError = e.getMessage();
            return false;
        }
    }
}
