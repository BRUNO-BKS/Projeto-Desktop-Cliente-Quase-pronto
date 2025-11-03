package com.buyo.adminfx.auth;

import com.buyo.adminfx.dao.UserDAO;
import com.buyo.adminfx.model.User;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class RememberMe {
    private static Path sessionFile() {
        String home = System.getProperty("user.home");
        Path dir = Paths.get(home, ".buyo_adminfx");
        try { if (!Files.exists(dir)) Files.createDirectories(dir); } catch (IOException ignore) {}
        return dir.resolve("session.properties");
    }

    public static void save(int userId) {
        try {
            Properties props = new Properties();
            props.setProperty("userId", String.valueOf(userId));
            try (OutputStream out = Files.newOutputStream(sessionFile())) {
                props.store(out, "Buyo AdminFX Session");
            }
        } catch (Exception ignore) {}
    }

    public static void clear() {
        try {
            Path f = sessionFile();
            if (Files.exists(f)) Files.delete(f);
        } catch (Exception ignore) {}
    }

    public static User tryRestore() {
        Path f = sessionFile();
        if (!Files.exists(f)) return null;
        try (InputStream in = Files.newInputStream(f)) {
            Properties props = new Properties();
            props.load(in);
            String idStr = props.getProperty("userId");
            if (idStr == null || idStr.isBlank()) return null;
            int id = Integer.parseInt(idStr.trim());
            UserDAO dao = new UserDAO();
            return dao.getById(id);
        } catch (Exception e) {
            return null;
        }
    }
}
