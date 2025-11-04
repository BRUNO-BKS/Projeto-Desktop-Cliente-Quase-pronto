package com.buyo.adminfx.ui.controllers;

import com.buyo.adminfx.auth.Session;
import com.buyo.adminfx.auth.RememberMe;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.scene.shape.Circle;
import javafx.scene.paint.ImagePattern;
import javafx.scene.image.Image;

public class MainController {
    @FXML
    private BorderPane rootPane;

    private boolean inProfile = false;
    private Node leftBackup;

    @FXML
    private Label userNameLabel;

    @FXML
    private Circle avatarCircle;

    @FXML private Button btnCustomers;
    @FXML private Button btnProducts;
    @FXML private Button btnOrders;
    @FXML private Button btnCategories;

    private static MainController lastInstance;

    @FXML
    public void initialize() {
        lastInstance = this;
        if (userNameLabel != null) {
            if (Session.getCurrentUser() != null) {
                userNameLabel.setText(Session.getCurrentUser().getName());
            } else if (userNameLabel.getText() == null || userNameLabel.getText().isBlank()) {
                userNameLabel.setText("Admin");
            }
        }
        // Role-based visibility (colaborador tem menu limitado)
        boolean isAdmin = Session.getCurrentUser() != null && Session.getCurrentUser().isAdmin();
        try {
            if (!isAdmin) {
                if (btnProducts != null) btnProducts.setVisible(false);
                if (btnCategories != null) btnCategories.setVisible(false);
                // Clientes e Pedidos permanecem visíveis por padrão
            }
        } catch (Exception ignore) {}
        // Atualiza avatar se houver foto
        try {
            if (avatarCircle != null && Session.getCurrentUser() != null) {
                String url = Session.getCurrentUser().getPhotoUrl();
                if (url != null && !url.isBlank()) {
                    avatarCircle.setFill(new ImagePattern(new Image(url, false)));
                }
            }
        } catch (Exception ignore) {}
    }

    public static void refreshTopBarFromSession() {
        if (lastInstance == null) return;
        try {
            if (lastInstance.userNameLabel != null && Session.getCurrentUser() != null) {
                lastInstance.userNameLabel.setText(Session.getCurrentUser().getName());
            }
            if (lastInstance.avatarCircle != null && Session.getCurrentUser() != null) {
                String url = Session.getCurrentUser().getPhotoUrl();
                if (url != null && !url.isBlank()) {
                    lastInstance.avatarCircle.setFill(new ImagePattern(new Image(url, false)));
                }
            }
        } catch (Exception ignore) {}
    }

    public static void goToDefaultList() {
        if (lastInstance == null) return;
        lastInstance.setCenterView("/com/buyo/adminfx/ui/CustomerView.fxml");
    }

    @FXML
    public void showCustomers(ActionEvent e) {
        setCenterView("/com/buyo/adminfx/ui/CustomerView.fxml");
    }

    @FXML
    public void showProducts(ActionEvent e) {
        setCenterView("/com/buyo/adminfx/ui/ProductView.fxml");
    }

    @FXML
    public void showOrders(ActionEvent e) {
        setCenterView("/com/buyo/adminfx/ui/OrderView.fxml");
    }

    @FXML
    public void showPayments(ActionEvent e) {
        setCenterView("/com/buyo/adminfx/ui/PaymentsView.fxml");
    }

    @FXML
    public void showProductLogs(ActionEvent e) {
        setCenterView("/com/buyo/adminfx/ui/ProductLogView.fxml");
    }

    @FXML
    public void showCategories(ActionEvent e) {
        setCenterView("/com/buyo/adminfx/ui/CategoryView.fxml");
    }

    @FXML
    public void showAddresses(ActionEvent e) {
        setCenterView("/com/buyo/adminfx/ui/AddressView.fxml");
    }

    @FXML
    public void showCoupons(ActionEvent e) {
        setCenterView("/com/buyo/adminfx/ui/CouponView.fxml");
    }

    @FXML
    public void showCarts(ActionEvent e) {
        setCenterView("/com/buyo/adminfx/ui/CartView.fxml");
    }

    @FXML
    public void showReviews(ActionEvent e) {
        setCenterView("/com/buyo/adminfx/ui/ReviewView.fxml");
    }

    

    @FXML
    public void onProfile(ActionEvent e) {
        try {
            URL fxml = getClass().getResource("/com/buyo/adminfx/ui/ProfileView.fxml");
            if (fxml == null) {
                String userDir = System.getProperty("user.dir");
                Path p1 = Paths.get(userDir, "buyo-adminfx", "src", "main", "resources", "com", "buyo", "adminfx", "ui", "ProfileView.fxml");
                Path p2 = Paths.get(userDir, "src", "main", "resources", "com", "buyo", "adminfx", "ui", "ProfileView.fxml");
                Path existing = Files.exists(p1) ? p1 : (Files.exists(p2) ? p2 : null);
                if (existing != null) {
                    fxml = existing.toUri().toURL();
                }
            }
            FXMLLoader loader = new FXMLLoader(fxml);
            Node content = loader.load();
            rootPane.setCenter(content);
            // Visual: esconde menu esquerdo enquanto no perfil
            if (!inProfile) {
                leftBackup = rootPane.getLeft();
            }
            rootPane.setLeft(null);
            inProfile = true;
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setTitle("Perfil");
            alert.setContentText("Falha ao abrir Perfil: " + ex.getMessage());
            alert.show();
        }
    }

    @FXML
    public void onLogout(ActionEvent e) {
        try {
            int uid = -1;
            if (Session.getCurrentUser() != null) {
                uid = Session.getCurrentUser().getId();
            }
            try {
                if (uid > 0) {
                    new com.buyo.adminfx.dao.UserDAO().setOnline(uid, false);
                }
            } catch (Exception ignore) {}
            Session.clear();
            try { RememberMe.clear(); } catch (Exception ignore) {}
            URL fxml = getClass().getResource("/com/buyo/adminfx/ui/LoginView.fxml");
            if (fxml == null) {
                String userDir = System.getProperty("user.dir");
                Path p1 = Paths.get(userDir, "buyo-adminfx", "src", "main", "resources", "com", "buyo", "adminfx", "ui", "LoginView.fxml");
                Path p2 = Paths.get(userDir, "src", "main", "resources", "com", "buyo", "adminfx", "ui", "LoginView.fxml");
                Path existing = Files.exists(p1) ? p1 : (Files.exists(p2) ? p2 : null);
                if (existing != null) {
                    fxml = existing.toUri().toURL();
                }
            }
            FXMLLoader loader = new FXMLLoader(fxml);
            Parent root = loader.load();
            Scene scene = new Scene(root, 1000, 650);
            var css = getClass().getResource("/com/buyo/adminfx/ui/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            var stage = (javafx.stage.Stage) rootPane.getScene().getWindow();
            stage.setTitle("ByteForge AdminFX - Login");
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setTitle("Sair");
            alert.setContentText("Falha ao voltar ao login: " + ex.getMessage());
            alert.show();
        }
    }

    private void setCenterView(String fxmlPath) {
        try {
            URL fxml = getClass().getResource(fxmlPath);
            if (fxml == null) {
                String userDir = System.getProperty("user.dir");
                // fxmlPath sempre começa com '/'
                String[] segs = fxmlPath.replaceFirst("^/", "").split("/");
                Path p1 = Paths.get(userDir, "buyo-adminfx", "src", "main", "resources", segs[0], segs[1], segs[2], segs[3], segs[4]);
                Path p2 = Paths.get(userDir, "src", "main", "resources", segs[0], segs[1], segs[2], segs[3], segs[4]);
                Path existing = Files.exists(p1) ? p1 : (Files.exists(p2) ? p2 : null);
                if (existing != null) {
                    fxml = existing.toUri().toURL();
                }
            }
            FXMLLoader loader = new FXMLLoader(fxml);
            Node content = loader.load();
            rootPane.setCenter(content);
            // Ao sair do perfil, restaura menu esquerdo
            if (inProfile) {
                if (leftBackup != null) {
                    rootPane.setLeft(leftBackup);
                }
                inProfile = false;
            }
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setTitle("Navegação");
            alert.setContentText("Falha ao abrir tela: " + ex.getMessage());
            alert.show();
        }
    }
}
