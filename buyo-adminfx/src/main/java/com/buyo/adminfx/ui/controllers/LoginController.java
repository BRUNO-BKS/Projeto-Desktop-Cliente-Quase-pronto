package com.buyo.adminfx.ui.controllers;

import com.buyo.adminfx.auth.Session;
import com.buyo.adminfx.auth.RememberMe;
import com.buyo.adminfx.dao.UserDAO;
import com.buyo.adminfx.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;

import java.net.URL;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private CheckBox rememberCheck;

    private final UserDAO userDAO = new UserDAO();
    
    @FXML
    public void initialize() {
        // Verifica se há um usuário lembrado
        User rememberedUser = RememberMe.tryRestore();
        if (rememberedUser != null) {
            emailField.setText(rememberedUser.getEmail());
            rememberCheck.setSelected(true);
            // Foca no campo de senha para o usuário digitar a senha
            passwordField.requestFocus();
        }
        // Bootstrap do banco e usuário admin padrão
        try {
            userDAO.ensureUsersTable();
            userDAO.ensureUserProfileColumns();
            if (!userDAO.hasAnyAdmin()) {
                boolean created = userDAO.createAdmin("Admin", "admin@local", "admin123");
                if (created) {
                    System.out.println("Administrador padrão criado: admin@local / admin123");
                    if (errorLabel != null) errorLabel.setText("Admin inicial criado: admin@local / admin123");
                }
            }
        } catch (Exception ignore) {}
    }

    @FXML
    public void onLogin(ActionEvent e) {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String pass = passwordField.getText() == null ? "" : passwordField.getText();
        if (email.isBlank() || pass.isBlank()) {
            setError("Informe email e senha.");
            return;
        }
        User user = userDAO.authenticate(email, pass);
        if (user == null) {
            String detail = userDAO.getLastError();
            setError("Credenciais inválidas." + (detail != null && !detail.isBlank() ? " (" + detail + ")" : ""));
            return;
        }
        Session.setCurrentUser(user);
        try {
            userDAO.setOnline(user.getId(), true);
            userDAO.updateLastActive(user.getId());
        } catch (Exception ignore) {}
        try {
            if (rememberCheck != null && rememberCheck.isSelected()) {
                RememberMe.save(user.getId());
            } else {
                RememberMe.clear();
            }
        } catch (Exception ignore) {}
        navigateToMain(e);
    }

    private void setError(String msg) {
        if (errorLabel != null) {
            errorLabel.setText(msg);
        }
    }

    private void navigateToMain(ActionEvent e) {
        try {
            System.out.println("Iniciando navegação para MainView...");
            
            // Tenta carregar o FXML do classpath
            String fxmlPath = "/com/buyo/adminfx/ui/MainView.fxml";
            URL fxml = getClass().getResource(fxmlPath);
            System.out.println("Tentando carregar FXML do classpath: " + fxmlPath);
            
            if (fxml == null) {
                System.out.println("Arquivo não encontrado no classpath. Procurando no sistema de arquivos...");
                String userDir = System.getProperty("user.dir");
                System.out.println("Diretório atual: " + userDir);
                
                java.nio.file.Path p1 = java.nio.file.Paths.get(userDir, "buyo-adminfx", "src", "main", "resources", "com", "buyo", "adminfx", "ui", "MainView.fxml");
                java.nio.file.Path p2 = java.nio.file.Paths.get(userDir, "src", "main", "resources", "com", "buyo", "adminfx", "ui", "MainView.fxml");
                
                System.out.println("Procurando em: " + p1);
                System.out.println("Procurando em: " + p2);
                
                java.nio.file.Path existing = java.nio.file.Files.exists(p1) ? p1 : (java.nio.file.Files.exists(p2) ? p2 : null);
                
                if (existing != null) {
                    System.out.println("Arquivo encontrado em: " + existing);
                    fxml = existing.toUri().toURL();
                } else {
                    System.err.println("ERRO: Arquivo MainView.fxml não encontrado em nenhum dos locais esperados");
                    setError("Erro: Arquivo de interface não encontrado");
                    return;
                }
            } else {
                System.out.println("Arquivo FXML encontrado no classpath: " + fxml);
            }
            
            System.out.println("Carregando FXML...");
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(fxml);
            Parent root = loader.load();
            
            System.out.println("Criando cena...");
            Scene scene = new Scene(root, 1000, 650);
            
            // Tenta carregar o CSS
            String cssPath = "/com/buyo/adminfx/ui/styles.css";
            URL css = getClass().getResource(cssPath);
            if (css != null) {
                System.out.println("CSS encontrado: " + css);
                scene.getStylesheets().add(css.toExternalForm());
            } else {
                System.err.println("AVISO: Arquivo CSS não encontrado: " + cssPath);
            }
            
            Stage stage = (Stage) ((javafx.scene.Node) e.getSource()).getScene().getWindow();
            stage.setTitle("byte forge AdminFX");
            stage.setScene(scene);
            System.out.println("Mostrando a cena...");
            stage.show();
            System.out.println("Navegação concluída com sucesso!");
            
        } catch (Exception ex) {
            String errorMsg = "Falha ao abrir a aplicação: " + ex.getMessage();
            System.err.println(errorMsg);
            ex.printStackTrace();
            setError(errorMsg);
        }
    }

    @FXML
    public void onOpenSignup(ActionEvent e) {
        try {
            URL fxml = getClass().getResource("/com/buyo/adminfx/ui/AdminSignupView.fxml");
            if (fxml == null) {
                // Fallback: tentar localizar pelo filesystem
                String userDir = System.getProperty("user.dir");
                java.nio.file.Path p1 = java.nio.file.Paths.get(userDir, "buyo-adminfx", "src", "main", "resources", "com", "buyo", "adminfx", "ui", "AdminSignupView.fxml");
                java.nio.file.Path p2 = java.nio.file.Paths.get(userDir, "src", "main", "resources", "com", "buyo", "adminfx", "ui", "AdminSignupView.fxml");
                java.nio.file.Path existing = java.nio.file.Files.exists(p1) ? p1 : (java.nio.file.Files.exists(p2) ? p2 : null);
                if (existing != null) {
                    fxml = existing.toUri().toURL();
                }
            }
            FXMLLoader loader = new FXMLLoader(fxml);
            Parent root = loader.load();
            Scene scene = new Scene(root, 600, 420);
            URL css = getClass().getResource("/com/buyo/adminfx/ui/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            Stage stage = (Stage) ((javafx.scene.Node) e.getSource()).getScene().getWindow();
            stage.setTitle("Criar Administrador");
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            setError("Falha ao abrir cadastro de admin: " + ex.getMessage());
        }
    }

    @FXML
    public void onOpenClientSignup(ActionEvent e) {
        try {
            URL fxml = getClass().getResource("/com/buyo/adminfx/ui/ClientSignupView.fxml");
            if (fxml == null) {
                String userDir = System.getProperty("user.dir");
                java.nio.file.Path p1 = java.nio.file.Paths.get(userDir, "buyo-adminfx", "src", "main", "resources", "com", "buyo", "adminfx", "ui", "ClientSignupView.fxml");
                java.nio.file.Path p2 = java.nio.file.Paths.get(userDir, "src", "main", "resources", "com", "buyo", "adminfx", "ui", "ClientSignupView.fxml");
                java.nio.file.Path existing = java.nio.file.Files.exists(p1) ? p1 : (java.nio.file.Files.exists(p2) ? p2 : null);
                if (existing != null) {
                    fxml = existing.toUri().toURL();
                }
            }
            FXMLLoader loader = new FXMLLoader(fxml);
            Parent root = loader.load();
            Scene scene = new Scene(root, 600, 420);
            URL css = getClass().getResource("/com/buyo/adminfx/ui/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            Stage stage = (Stage) ((javafx.scene.Node) e.getSource()).getScene().getWindow();
            stage.setTitle("Criar Cliente");
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            setError("Falha ao abrir cadastro de cliente: " + ex.getMessage());
        }
    }
    
    /**
     * Manipula a ação do checkbox Lembre-me
     */
    @FXML
    public void onRememberMeAction() {
        // Se o usuário desmarcar o checkbox, removemos o lembrete
        if (!rememberCheck.isSelected()) {
            RememberMe.clear();
        }
        // Se o usuário marcar o checkbox e já estiver logado, salvamos o lembrete
        else if (Session.getCurrentUser() != null) {
            RememberMe.save(Session.getCurrentUser().getId());
        }
    }
}
