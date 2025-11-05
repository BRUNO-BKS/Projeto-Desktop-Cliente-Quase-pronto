package com.buyo.adminfx.ui.controllers;

import com.buyo.adminfx.dao.UserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;

public class ClientSignupController {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void onCreateClient(ActionEvent e) {
        String name = val(nameField);
        String email = val(emailField);
        String pass = val(passwordField);
        String confirm = val(confirmField);
        if (name.isBlank() || email.isBlank() || pass.isBlank() || confirm.isBlank()) {
            setError("Preencha todos os campos.");
            return;
        }
        if (!email.matches("^.+@.+\\..+$")) {
            setError("Email inválido.");
            return;
        }
        if (!pass.equals(confirm)) {
            setError("Senhas não conferem.");
            return;
        }
        boolean ok = userDAO.createClient(name, email, pass);
        if (!ok) {
            String reason = userDAO.getLastError();
            setError("Falha ao criar/atualizar cliente" + (reason != null && !reason.isBlank() ? (": " + reason) : ". Verifique o email."));
            return;
        }
        setSuccess("Cliente criado. Redirecionando para login...");
        onBackToLogin(e);
    }

    @FXML
    public void onBackToLogin(ActionEvent e) {
        try {
            URL fxml = getClass().getResource("/com/buyo/adminfx/ui/LoginView.fxml");
            if (fxml == null) {
                String userDir = System.getProperty("user.dir");
                java.nio.file.Path p1 = java.nio.file.Paths.get(userDir, "buyo-adminfx", "src", "main", "resources", "com", "buyo", "adminfx", "ui", "LoginView.fxml");
                java.nio.file.Path p2 = java.nio.file.Paths.get(userDir, "src", "main", "resources", "com", "buyo", "adminfx", "ui", "LoginView.fxml");
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
            stage.setTitle("StockRO AdminFX - Login");
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            setError("Falha ao abrir login.");
        }
    }

    private String val(TextField f) { return f.getText() == null ? "" : f.getText().trim(); }
    private String val(PasswordField f) { return f.getText() == null ? "" : f.getText(); }
    private void setError(String s) { if (errorLabel != null) errorLabel.setText(s); if (successLabel != null) successLabel.setText(""); }
    private void setSuccess(String s) { if (successLabel != null) successLabel.setText(s); if (errorLabel != null) errorLabel.setText(""); }
}
