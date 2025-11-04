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
    public void onLogin(ActionEvent e) {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String pass = passwordField.getText() == null ? "" : passwordField.getText();
        if (email.isBlank() || pass.isBlank()) {
            setError("Informe email e senha.");
            return;
        }
        User user = userDAO.authenticate(email, pass);
        if (user == null) {
            setError("Credenciais inválidas.");
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
            URL fxml = getClass().getResource("/com/buyo/adminfx/ui/MainView.fxml");
            if (fxml == null) {
                String userDir = System.getProperty("user.dir");
                java.nio.file.Path p1 = java.nio.file.Paths.get(userDir, "buyo-adminfx", "src", "main", "resources", "com", "buyo", "adminfx", "ui", "MainView.fxml");
                java.nio.file.Path p2 = java.nio.file.Paths.get(userDir, "src", "main", "resources", "com", "buyo", "adminfx", "ui", "MainView.fxml");
                java.nio.file.Path existing = java.nio.file.Files.exists(p1) ? p1 : (java.nio.file.Files.exists(p2) ? p2 : null);
                if (existing != null) {
                    fxml = existing.toUri().toURL();
                }
            }
            FXMLLoader loader = new FXMLLoader(fxml);
            Parent root = loader.load();
            Scene scene = new Scene(root, 1000, 650);
            URL css = getClass().getResource("/com/buyo/adminfx/ui/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            Stage stage = (Stage) ((javafx.scene.Node) e.getSource()).getScene().getWindow();
            stage.setTitle("byte forge AdminFX");
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            setError("Falha ao abrir a aplicação: " + ex.getMessage());
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
}
