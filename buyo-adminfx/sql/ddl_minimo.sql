-- DDL mínimo para login/cadastro funcionar
-- Cria o schema e a tabela 'usuarios'

CREATE DATABASE IF NOT EXISTS buyo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE buyo;

CREATE TABLE IF NOT EXISTS usuarios (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(120) NOT NULL,
  email VARCHAR(180) NOT NULL,
  senha_hash VARCHAR(100) NOT NULL,
  telefone VARCHAR(30),
  is_admin TINYINT(1) NOT NULL DEFAULT 0,
  UNIQUE KEY uk_usuarios_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
