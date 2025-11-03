-- Seeds para banco `buyo`
-- Execute este arquivo após criar o schema
USE buyo;

-- Categorias
INSERT INTO categorias (nome, descricao) VALUES
  ('Roupas', 'Vestuário em geral'),
  ('Acessórios', 'Acessórios diversos'),
  ('Bebidas Térmicas', 'Garrafas e copos térmicos');

-- Usuários (admins e clientes fictícios)
INSERT INTO usuarios (nome, email, senha_hash, telefone, is_admin) VALUES
  ('Admin', 'admin@buyo.local', '$2a$10$hashadmin', '11999990000', 1),
  ('João Silva', 'joao@exemplo.com', '$2a$10$hashcliente', '11911112222', 0),
  ('Maria Souza', 'maria@exemplo.com', '$2a$10$hashcliente', '11933334444', 0);

-- Produtos
INSERT INTO produtos (nome_produto, descricao_prod, preco, preco_promocional, categoria_id, imagem_url, ativo) VALUES
  ('Camiseta Básica', 'Camiseta 100% algodão', 59.90, NULL, (SELECT id FROM categorias WHERE nome='Roupas'), 'https://picsum.photos/seed/camiseta/300/200', 1),
  ('Boné Preto', 'Boné ajustável', 39.90, NULL, (SELECT id FROM categorias WHERE nome='Acessórios'), 'https://picsum.photos/seed/bone/300/200', 1),
  ('Copo Térmico 500ml', 'Mantém a bebida gelada', 89.90, 79.90, (SELECT id FROM categorias WHERE nome='Bebidas Térmicas'), 'https://picsum.photos/seed/copo/300/200', 1);

-- Estoque
INSERT INTO estoque (produto_id, quantidade) VALUES
  ((SELECT id FROM produtos WHERE nome_produto='Camiseta Básica'), 120),
  ((SELECT id FROM produtos WHERE nome_produto='Boné Preto'), 80),
  ((SELECT id FROM produtos WHERE nome_produto='Copo Térmico 500ml'), 45);

-- Pedidos (opcional, exemplo simples)
INSERT INTO pedidos (usuario_id, status, total) VALUES
  ((SELECT id FROM usuarios WHERE email='joao@exemplo.com'), 'CRIADO', 199.80);

-- Itens do pedido exemplo
INSERT INTO pedido_itens (pedido_id, produto_id, quantidade, preco_unitario) VALUES
  ((SELECT id FROM pedidos ORDER BY id DESC LIMIT 1), (SELECT id FROM produtos WHERE nome_produto='Camiseta Básica'), 2, 59.90);
