package com.buyo.adminfx.db;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Database {
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }
        try {
            Properties props = new Properties();
            // Tenta carregar do classpath primeiro
            try (InputStream in = Database.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (in != null) {
                    props.load(in);
                } else {
                    // Fallbacks: caminhos comuns quando o resources não está no classpath
                    boolean loaded = false;
                    // 1) diretório de trabalho
                    Path p0 = Paths.get("db.properties");
                    // 2) projeto subpasta buyo-adminfx/resources
                    String userDir = System.getProperty("user.dir");
                    Path p1 = Paths.get(userDir, "buyo-adminfx", "src", "main", "resources", "db.properties");
                    // 3) projeto subpasta buyo-adminfx raiz
                    Path p2 = Paths.get(userDir, "buyo-adminfx", "db.properties");
                    // 4) projeto raiz src/main/resources
                    Path p3 = Paths.get(userDir, "src", "main", "resources", "db.properties");

                    for (Path p : new Path[]{p0, p1, p2, p3}) {
                        if (!loaded && p != null && Files.exists(p)) {
                            try (InputStream fin = new FileInputStream(p.toFile())) {
                                props.load(fin);
                                loaded = true;
                            }
                        }
                    }
                    if (!loaded) {
                        // Último recurso: tenta o nome no diretório de trabalho para que a exceção seja clara
                        try (InputStream fin = new FileInputStream("db.properties")) {
                            props.load(fin);
                        }
                    }
                }
            }
            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String pass = props.getProperty("db.password");
            connection = DriverManager.getConnection(url, user, pass);
            return connection;
        } catch (Exception e) {
            throw new SQLException("Falha ao conectar ao banco: " + e.getMessage(), e);
        }
    }
}

