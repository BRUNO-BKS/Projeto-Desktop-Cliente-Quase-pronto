package com.buyo.adminfx;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseTest {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/buyo?useSSL=false&serverTimezone=UTC";
        Properties props = new Properties();
        props.setProperty("user", "root");
        props.setProperty("password", "12345678");
        
        System.out.println("Tentando conectar ao banco de dados...");
        
        try (Connection conn = DriverManager.getConnection(url, props)) {
            System.out.println("Conexão bem-sucedida!");
            System.out.println("Versão do MySQL: " + conn.getMetaData().getDatabaseProductVersion());
        } catch (SQLException e) {
            System.err.println("Erro ao conectar ao banco de dados:");
            e.printStackTrace();
        }
    }
}
