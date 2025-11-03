package com.buyo.adminfx.ui.controllers;

import com.buyo.adminfx.auth.Session;
import com.buyo.adminfx.dao.UserDAO;
import com.buyo.adminfx.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.event.ActionEvent;

import java.io.File;

public class ProfileController {
    @FXML private ImageView photoView;
    @FXML private Label photoPathLabel;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Label statusLabel;

    private final UserDAO userDAO = new UserDAO();
    private String selectedPhotoPath;

    @FXML
    public void initialize() {
        try {
            userDAO.ensureUserProfileColumns();
        } catch (Exception ignore) {}

        User u = Session.getCurrentUser();
        if (u == null) {
            setStatus("Nenhum usuário logado.");
            return;
        }
        // Recarrega do banco para trazer telefone/foto
        User dbUser = userDAO.getById(u.getId());
        if (dbUser != null) u = dbUser;

        nameField.setText(safe(u.getName()));
        emailField.setText(safe(u.getEmail()));
        phoneField.setText(safe(u.getPhone()));
        if (u.getPhotoUrl() != null && !u.getPhotoUrl().isBlank()) {
            try {
                photoView.setImage(new Image(u.getPhotoUrl(), true));
                photoPathLabel.setText(u.getPhotoUrl());
                selectedPhotoPath = u.getPhotoUrl();
            } catch (Exception ignore) {
                photoPathLabel.setText("(não foi possível carregar a foto)");
            }
        }
    }

    @FXML
    public void onPickPhoto(ActionEvent e) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Escolher foto de perfil");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File f = fc.showOpenDialog(photoView.getScene().getWindow());
        if (f != null) {
            selectedPhotoPath = f.toURI().toString();
            try {
                photoView.setImage(new Image(selectedPhotoPath, true));
                photoPathLabel.setText(selectedPhotoPath);
                setStatus("Foto selecionada. Não esqueça de salvar.");
            } catch (Exception ex) {
                setStatus("Falha ao carregar a imagem selecionada.");
            }
        }
    }

    @FXML
    public void onSave(ActionEvent e) {
        User u = Session.getCurrentUser();
        if (u == null) {
            setStatus("Sessão expirada. Faça login novamente.");
            return;
        }
        // Garante que as colunas existem antes de salvar (compatibilidade MySQL)
        try { userDAO.ensureUserProfileColumns(); } catch (Exception ignore) {}
        String name = trim(nameField.getText());
        String phone = trim(phoneField.getText());
        String photo = selectedPhotoPath;
        if (name.isBlank()) {
            setStatus("Nome não pode ser vazio.");
            return;
        }
        boolean ok = userDAO.updateProfile(u.getId(), name, phone, photo);
        if (!ok) {
            String reason = userDAO.getLastError();
            setStatus("Falha ao salvar: " + (reason == null ? "" : reason));
            return;
        }
        // Atualiza sessão
        u.setName(name);
        u.setPhone(phone);
        u.setPhotoUrl(photo);
        Session.setCurrentUser(u);
        MainController.refreshTopBarFromSession();
        setStatus("Perfil atualizado.");
    }

    @FXML
    public void onBack(ActionEvent e) {
        MainController.goToDefaultList();
    }

    private String safe(String s) { return s == null ? "" : s; }
    private String trim(String s) { return s == null ? "" : s.trim(); }
    private void setStatus(String s) { if (statusLabel != null) statusLabel.setText(s); }
}
