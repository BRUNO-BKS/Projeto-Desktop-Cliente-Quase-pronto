-- Script de criação de tabelas de exemplo (MySQL) - schema PT-BR
CREATE DATABASE IF NOT EXISTS admin_app_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE admin_app_db;

CREATE TABLE IF NOT EXISTS cliente (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(150) NOT NULL,
  email VARCHAR(150),
  telefone VARCHAR(30),
  endereco VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS produto (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(150) NOT NULL,
  descricao TEXT,
  preco DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  quantidade INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS pedido (
  id INT AUTO_INCREMENT PRIMARY KEY,
  cliente_id INT NOT NULL,
  data_pedido DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS item_pedido (
  id INT AUTO_INCREMENT PRIMARY KEY,
  pedido_id INT NOT NULL,
  produto_id INT NOT NULL,
  quantidade INT NOT NULL DEFAULT 1,
  subtotal DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  FOREIGN KEY (pedido_id) REFERENCES pedido(id) ON DELETE CASCADE,
  FOREIGN KEY (produto_id) REFERENCES produto(id)
);

-- Inserts exemplo
INSERT INTO cliente (nome, email, telefone, endereco) VALUES
('Empresa A', 'contato@empresaa.com', '11-9999-0000', 'Rua A, 123'),
('Cliente B', 'cliente.b@example.com', '21-8888-1111', 'Av. B, 45');

INSERT INTO produto (nome, descricao, preco, quantidade) VALUES
('Produto 1', 'Descrição do produto 1', 10.50, 100),
('Produto 2', 'Descrição do produto 2', 25.00, 50);
