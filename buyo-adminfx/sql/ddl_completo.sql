-- DDL completo para Buyo AdminFX (atualizado conforme dump)

CREATE DATABASE IF NOT EXISTS tockro_bd DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE tockro_bd;

-- usuarios
CREATE TABLE IF NOT EXISTS usuarios (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  senha_hash VARCHAR(255) NOT NULL,
  telefone VARCHAR(20),
  data_cadastro DATETIME DEFAULT CURRENT_TIMESTAMP,
  is_admin TINYINT(1) NOT NULL DEFAULT 0,
  UNIQUE KEY uk_usuarios_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- categorias
CREATE TABLE IF NOT EXISTS categorias (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(120) NOT NULL,
  descricao VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- produtos
CREATE TABLE IF NOT EXISTS produtos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nome_produto VARCHAR(255) NOT NULL,
  descricao_prod TEXT,
  preco DECIMAL(10,2) NOT NULL,
  preco_promocional DECIMAL(10,2) DEFAULT NULL,
  oferta_inicio DATETIME DEFAULT NULL,
  oferta_fim DATETIME DEFAULT NULL,
  categoria_id INT,
  imagem_url VARCHAR(500),
  ativo TINYINT(1) DEFAULT 1,
  CONSTRAINT fk_produtos_categoria
    FOREIGN KEY (categoria_id) REFERENCES categorias(id)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- estoque
CREATE TABLE IF NOT EXISTS estoque (
  produto_id INT PRIMARY KEY,
  quantidade INT NOT NULL DEFAULT 0,
  CONSTRAINT fk_estoque_produto
    FOREIGN KEY (produto_id) REFERENCES produtos(id)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- enderecos
CREATE TABLE IF NOT EXISTS enderecos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  usuario_id INT,
  rua VARCHAR(255) DEFAULT NULL,
  numero VARCHAR(50) DEFAULT NULL,
  complemento VARCHAR(255) DEFAULT NULL,
  cidade VARCHAR(100) DEFAULT NULL,
  estado VARCHAR(100) DEFAULT NULL,
  cep VARCHAR(20) DEFAULT NULL,
  CONSTRAINT fk_enderecos_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- pedidos
CREATE TABLE IF NOT EXISTS pedidos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  usuario_id INT,
  endereco_id INT,
  data_pedido DATETIME DEFAULT CURRENT_TIMESTAMP,
  status VARCHAR(50) DEFAULT NULL,
  total DECIMAL(10,2) DEFAULT NULL,
  CONSTRAINT fk_pedidos_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT fk_pedidos_endereco
    FOREIGN KEY (endereco_id) REFERENCES enderecos(id)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- pedido_itens
CREATE TABLE IF NOT EXISTS pedido_itens (
  id INT AUTO_INCREMENT PRIMARY KEY,
  pedido_id INT,
  produto_id INT,
  quantidade INT,
  preco_unitario DECIMAL(10,2) DEFAULT NULL,
  CONSTRAINT fk_pedido_itens_pedido
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_pedido_itens_produto
    FOREIGN KEY (produto_id) REFERENCES produtos(id)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- carrinho
CREATE TABLE IF NOT EXISTS carrinho (
  id INT AUTO_INCREMENT PRIMARY KEY,
  usuario_id INT,
  criado_em DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_carrinho_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- carrinho_itens
CREATE TABLE IF NOT EXISTS carrinho_itens (
  id INT AUTO_INCREMENT PRIMARY KEY,
  carrinho_id INT,
  produto_id INT,
  quantidade INT,
  CONSTRAINT fk_carrinho_itens_carrinho
    FOREIGN KEY (carrinho_id) REFERENCES carrinho(id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_carrinho_itens_produto
    FOREIGN KEY (produto_id) REFERENCES produtos(id)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- avaliacoes
CREATE TABLE IF NOT EXISTS avaliacoes (
  id INT AUTO_INCREMENT PRIMARY KEY,
  usuario_id INT,
  produto_id INT,
  nota INT,
  comentario TEXT,
  data_avaliacao DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_avaliacoes_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT fk_avaliacoes_produto
    FOREIGN KEY (produto_id) REFERENCES produtos(id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT chk_avaliacoes_nota
    CHECK (nota BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- banners
CREATE TABLE IF NOT EXISTS banners (
  id INT AUTO_INCREMENT PRIMARY KEY,
  titulo VARCHAR(120) NOT NULL,
  subtitulo VARCHAR(255) DEFAULT NULL,
  imagem_url VARCHAR(255) NOT NULL,
  link_url VARCHAR(255) DEFAULT NULL,
  ordem INT NOT NULL DEFAULT 0,
  ativo TINYINT(1) NOT NULL DEFAULT 1,
  inicio DATETIME DEFAULT NULL,
  fim DATETIME DEFAULT NULL,
  criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  atualizado_em DATETIME DEFAULT NULL,
  KEY idx_banners_ativo (ativo),
  KEY idx_banners_ordem (ordem),
  KEY idx_banners_inicio (inicio),
  KEY idx_banners_fim (fim)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- cupons
CREATE TABLE IF NOT EXISTS cupons (
  id INT AUTO_INCREMENT PRIMARY KEY,
  codigo VARCHAR(50) UNIQUE,
  desconto_percentual DECIMAL(5,2) DEFAULT NULL,
  data_expiracao DATETIME DEFAULT NULL,
  ativo TINYINT(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- historico_status_pedido
CREATE TABLE IF NOT EXISTS historico_status_pedido (
  id INT AUTO_INCREMENT PRIMARY KEY,
  pedido_id INT,
  status VARCHAR(50) DEFAULT NULL,
  data_status DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_historico_pedido
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- log_produtos
CREATE TABLE IF NOT EXISTS log_produtos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  produto_id INT NOT NULL,
  campo_alterado VARCHAR(100) DEFAULT NULL,
  valor_antigo TEXT,
  valor_novo TEXT,
  data_alteracao DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_log_produtos
    FOREIGN KEY (produto_id) REFERENCES produtos(id)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- pagamentos
CREATE TABLE IF NOT EXISTS pagamentos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  pedido_id INT,
  tipo_pagamento VARCHAR(50) DEFAULT NULL,
  status VARCHAR(50) DEFAULT NULL,
  data_pagamento DATETIME DEFAULT NULL,
  valor DECIMAL(10,2) DEFAULT NULL,
  CONSTRAINT fk_pagamentos_pedido
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- pedido_status_log
CREATE TABLE IF NOT EXISTS pedido_status_log (
  id INT AUTO_INCREMENT PRIMARY KEY,
  pedido_id INT NOT NULL,
  admin_id INT NOT NULL,
  status_anterior VARCHAR(50) DEFAULT NULL,
  status_novo VARCHAR(50) NOT NULL,
  criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_pedido_status_log_pedido
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_pedido_status_log_admin
    FOREIGN KEY (admin_id) REFERENCES usuarios(id)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- produto_log
CREATE TABLE IF NOT EXISTS produto_log (
  id INT AUTO_INCREMENT PRIMARY KEY,
  produto_id INT NOT NULL,
  admin_id INT NOT NULL,
  acao VARCHAR(50) NOT NULL,
  criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ip VARCHAR(45) DEFAULT NULL,
  user_agent VARCHAR(255) DEFAULT NULL,
  motivo VARCHAR(255) DEFAULT NULL,
  CONSTRAINT fk_produto_log_produto
    FOREIGN KEY (produto_id) REFERENCES produtos(id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_produto_log_admin
    FOREIGN KEY (admin_id) REFERENCES usuarios(id)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
