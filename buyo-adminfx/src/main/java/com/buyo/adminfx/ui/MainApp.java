package com.buyo.adminfx.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class MainApp extends Application {
    
    private Stage primaryStage;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("StockRO AdminFX");
        
        // Configura o System.out para garantir que os logs sejam exibidos
        System.setOut(new java.io.PrintStream(System.out) {
            @Override
            public void println(String x) {
                super.println(x);
                flush();
            }
        });
        
        System.out.println("\n=== INICIANDO APLICAÇÃO ===\n");
        
        // Configurar tratamento de exceções não capturadas
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("ERRO NÃO TRATADO NA THREAD " + thread.getName() + ":");
            throwable.printStackTrace();
            showError("Erro inesperado", "Ocorreu um erro inesperado: " + throwable.getMessage());
        });
        
        // Inicializa a interface do usuário
        initRootLayout();
    }
    
    private void initRootLayout() {
        try {
            loadLoginScreen();
        } catch (Exception e) {
            System.err.println("ERRO ao inicializar a interface:");
            e.printStackTrace();
            showError("Erro ao iniciar", "Não foi possível carregar a interface do usuário: " + e.getMessage());
        }
    }
    
    private void loadLoginScreen() {
    try {
        // 1. Carrega o FXML
        String fxmlPath = "/com/buyo/adminfx/ui/LoginView.fxml";
        System.out.println("Carregando FXML de: " + fxmlPath);
        
        // Usa o FXMLLoader para carregar o FXML
        FXMLLoader loader = new FXMLLoader();
        URL fxmlUrl = getClass().getResource(fxmlPath);
        
        if (fxmlUrl == null) {
            throw new IOException("Arquivo FXML não encontrado: " + fxmlPath);
        }
        
        // Carrega o FXML usando a URL
        Parent root = loader.load(fxmlUrl);
        System.out.println("FXML carregado com sucesso!");
        
        // Resto do código permanece o mesmo...
        // Carrega o arquivo CSS
        String cssPath = "/com/buyo/adminfx/ui/styles.css";
        URL cssUrl = getClass().getResource(cssPath);
        
        // Cria a cena
        Scene scene = new Scene(root, 1000, 700);
        
        // Adiciona o CSS à cena
        if (cssUrl != null) {
            System.out.println("Carregando CSS de: " + cssUrl);
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("AVISO: Arquivo CSS não encontrado: " + cssPath);
        }
        
        // Aplica estilos inline adicionais
        applyInlineStyles(root);
        
        // Configura a janela
        primaryStage.setTitle("Login - StockRO AdminFX");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.centerOnScreen();
        primaryStage.show();
    } catch (Exception e) {
        System.err.println("Erro ao carregar a tela de login:");
        e.printStackTrace();
        throw new RuntimeException("Erro ao carregar o arquivo FXML: " + e.getMessage(), e);
    }
}
    
    /**
     * Aplica estilos adicionais diretamente nos componentes
     * @param root O nó raiz da cena
     */
    private void applyInlineStyles(Parent root) {
        // Estilos para botões
        root.lookupAll(".button").forEach(node -> 
            node.setStyle(
                "-fx-background-color: #4a6cf7; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 8px 16px; " +
                "-fx-background-radius: 4px;"
            )
        );
        
        // Estilos para campos de texto
        root.lookupAll(".text-field, .password-field").forEach(node -> 
            node.setStyle(
                "-fx-padding: 8px; " +
                "-fx-font-size: 14px; " +
                "-fx-background-radius: 4px; " +
                "-fx-border-color: #dee2e6; " +
                "-fx-border-radius: 4px;"
            )
        );
        
        // Estilos para o container principal
        javafx.scene.Node loginContainer = root.lookup(".login-container");
        if (loginContainer != null) {
            loginContainer.setStyle(
                "-fx-padding: 40px; " +
                "-fx-background-color: white; " +
                "-fx-background-radius: 8px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);"
            );
        }
    }
    
    private void showErrorScreen(Throwable e) {
        StackPane errorRoot = new StackPane();
        errorRoot.setStyle(
            "-fx-background-color: #f8d7da; " +
            "-fx-padding: 20; " +
            "-fx-alignment: center;"
        );
        
        Label errorLabel = new Label("Erro ao carregar a interface do usuário: " + e.getMessage());
        errorLabel.setStyle(
            "-fx-text-fill: #721c24; " +
            "-fx-font-size: 14px; " +
            "-fx-wrap-text: true; " +
            "-fx-max-width: 500px;"
        );
        
        errorRoot.getChildren().add(errorLabel);
        Scene errorScene = new Scene(errorRoot, 600, 400);
        
        // Configura a janela de erro
        primaryStage.setScene(errorScene);
        primaryStage.setTitle("Erro");
        primaryStage.show();
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
