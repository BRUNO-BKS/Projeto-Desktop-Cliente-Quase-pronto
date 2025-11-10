# Buyo AdminFX - Sistema de Administração

Bem-vindo ao Buyo AdminFX, um sistema de administração desenvolvido em JavaFX para gerenciar o e-commerce Buyo.

## 🚀 Funcionalidades Implementadas

### 1. Tela de Carregamento (Splash Screen)
- Animações suaves durante o carregamento
- Barra de progresso interativa
- Exibição de status em tempo real

### 2. Módulos Principais
- **Autenticação de Usuários**
  - Login seguro
  - Recuperação de senha
  - Controle de acesso baseado em perfis

- **Gerenciamento de Produtos**
  - Cadastro e edição de produtos
  - Controle de estoque
  - Categorias e subcategorias

- **Vendas e Pedidos**
  - Acompanhamento de pedidos
  - Histórico de status
  - Filtros avançados

- **Relatórios**
  - Vendas por período
  - Produtos mais vendidos
  - Métricas de desempenho

## 🛠️ Tecnologias Utilizadas

- **Linguagem**: Java 21
- **Interface Gráfica**: JavaFX 21
- **Banco de Dados**: MySQL
- **Bibliotecas Principais**:
  - JFoenix para componentes Material Design
  - Ikonli para ícones
  - JBCrypt para criptografia de senhas

## 📦 Pré-requisitos

- JDK 21 ou superior
- Maven 3.8+
- MySQL 8.0+

## 🔧 Instalação

1. Clone o repositório:
   ```bash
   git clone [URL_DO_REPOSITÓRIO]
   cd Projeto-Desktop-Cliente-Quase-pronto
   ```

2. Configure o banco de dados:
   - Crie um banco de dados MySQL chamado `buyo_admin`
   - Atualize as credenciais no arquivo `src/main/resources/db.properties`

3. Execute o script SQL de inicialização:
   ```sql
   mysql -u [usuario] -p buyo_admin < src/main/resources/db/schema.sql
   ```

4. Compile o projeto:
   ```bash
   mvn clean install
   ```

5. Execute a aplicação:
   ```bash
   mvn javafx:run
   ```

## 🎨 Personalização

### Tema
Você pode alterar o tema da aplicação editando o arquivo:
```
src/main/resources/com/buyo/adminfx/css/styles.css
```

### Logotipo
Substitua o arquivo:
```
src/main/resources/com/buyo/adminfx/images/logo.svg
```

## 📊 Gráficos de Vendas

A aplicação inclui um painel com gráficos interativos para análise de vendas:
- Gráfico de linhas para acompanhamento de vendas ao longo do tempo
- Gráfico de pizza para visualização de categorias mais vendidas
- Filtros por período e categoria

## 🚀 Melhorias Recentes

### v1.1.0 (2025-11-07)
- Adicionada tela de carregamento personalizada
- Melhorias na experiência do usuário
- Otimizações de desempenho
- Correção de bugs na interface

### v1.0.0 (2025-10-15)
- Versão inicial estável
- Módulos básicos implementados
- Documentação técnica completa

## 🤝 Contribuição

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/nova-feature`)
3. Commit suas alterações (`git commit -m 'Adiciona nova feature'`)
4. Faça push para a branch (`git push origin feature/nova-feature`)
5. Abra um Pull Request

## 📞 Suporte

Para suporte, entre em contato:
- Email: suporte@buyo.com.br
- Telefone: (00) 1234-5678
- Horário de atendimento: Seg-Sex, 9h-18h

## 📄 Licença

Este projeto está licenciado sob a licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

---

Desenvolvido com ❤️ pela Equipe Buyo © 2025
