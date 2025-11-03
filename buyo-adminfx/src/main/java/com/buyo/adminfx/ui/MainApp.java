package com.buyo.adminfx.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.buyo.adminfx.dao.UserDAO;
import com.buyo.adminfx.auth.RememberMe;
import com.buyo.adminfx.auth.Session;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Tenta restaurar sessão
        var remembered = RememberMe.tryRestore();

        URL fxmlUrl;
        if (remembered != null) {
            Session.setCurrentUser(remembered);
            fxmlUrl = MainApp.class.getResource("/com/buyo/adminfx/ui/MainView.fxml");
            if (fxmlUrl == null) {
                String userDir = System.getProperty("user.dir");
                Path p1 = Paths.get(userDir, "buyo-adminfx", "src", "main", "resources", "com", "buyo", "adminfx", "ui", "MainView.fxml");
                Path p2 = Paths.get(userDir, "src", "main", "resources", "com", "buyo", "adminfx", "ui", "MainView.fxml");
                Path existing = Files.exists(p1) ? p1 : (Files.exists(p2) ? p2 : null);
                if (existing != null) {
                    fxmlUrl = existing.toUri().toURL();
                }
            }
        } else {
            fxmlUrl = MainApp.class.getResource("LoginView.fxml");
            if (fxmlUrl == null) {
                fxmlUrl = MainApp.class.getResource("/com/buyo/adminfx/ui/LoginView.fxml");
            }
            if (fxmlUrl == null) {
                String userDir = System.getProperty("user.dir");
                Path candidate1 = Paths.get(userDir, "buyo-adminfx", "src", "main", "resources", "com", "buyo", "adminfx", "ui", "LoginView.fxml");
                Path candidate2 = Paths.get(userDir, "src", "main", "resources", "com", "buyo", "adminfx", "ui", "LoginView.fxml");
                Path existing = Files.exists(candidate1) ? candidate1 : (Files.exists(candidate2) ? candidate2 : null);
                if (existing != null) {
                    fxmlUrl = existing.toUri().toURL();
                }
            }
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();
        Scene scene = new Scene(root, 1000, 650);
        URL css = MainApp.class.getResource("styles.css");
        if (css == null) {
            css = MainApp.class.getResource("/com/buyo/adminfx/ui/styles.css");
        }
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        // Bootstrap: cria um admin padrão se não existir nenhum
        try {
            UserDAO dao = new UserDAO();
            dao.ensureUsersTable();
            dao.ensureUserProfileColumns();
            if (!dao.hasAnyAdmin()) {
                dao.createAdmin("Admin", "admin@buyo.local", "12345678");
            }
            // Garante/atualiza o admin informado pelo usuário
            dao.createAdmin("Bruno", "bks886644@gmail.com", "12345678");
        } catch (Exception ignore) {
            // se falhar, apenas continua; o app ainda pode iniciar para cadastro manual
        }
        primaryStage.setTitle(remembered != null ? "Buyo AdminFX" : "Buyo AdminFX - Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

