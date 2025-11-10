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

public class MainApp_Fixed extends Application {
    
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
            
            // Carrega o FXML diretamente do classpath
            try (InputStream fxmlStream = getClass().getResourceAsStream(fxmlPath)) {
                if (fxmlStream == null) {
                    throw new IOException("Arquivo FXML não encontrado: " + fxmlPath);
                }
                
                Parent root = loader.load(fxmlStream);
                System.out.println("FXML carregado com sucesso!");
                
                // Cria a cena
                Scene scene = new Scene(root, 1000, 700);
                
                // Aplica estilos inline diretamente
                applyInlineStyles(root);
                
                // Configura a janela
                primaryStage.setTitle("Login - StockRO AdminFX");
                primaryStage.setScene(scene);
                primaryStage.setMinWidth(1000);
                primaryStage.setMinHeight(700);
                primaryStage.centerOnScreen();
                primaryStage.show();
                
            } catch (IOException e) {
                throw new RuntimeException("Erro ao carregar o arquivo FXML: " + e.getMessage(), e);
            }
            
        } catch (Exception e) {
            System.err.println("Erro ao carregar a tela de login:");
            e.printStackTrace();
            showErrorScreen(e);
        }
    }
    
    private void applyInlineStyles(Parent root) {
        // Aplica estilos diretamente no root
        String inlineCss = ""
            + "-fx-font-family: 'Segoe UI', Arial, sans-serif;"
            + "-fx-base: #4a6cf7;"
            + "-fx-accent: #4a6cf7;"
            + "-fx-default-button: #4a6cf7;"
            + "-fx-focus-color: #4a6cf7;"
            + "-fx-faint-focus-color: #4a6cf722;";
            
        // Aplica estilos gerais
        root.setStyle(inlineCss);
        
        // Aplica estilos específicos para os componentes
        root.lookupAll(".button").forEach(node -> 
            node.setStyle(
                "-fx-background-color: #4a6cf7; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 8px 16px; " +
                "-fx-background-radius: 4px;"
            )
        );
        
        root.lookupAll(".text-field, .password-field").forEach(node -> 
            node.setStyle(
                "-fx-padding: 8px; " +
                "-fx-font-size: 14px; " +
                "-fx-background-radius: 4px; " +
                "-fx-border-color: #dee2e6; " +
                "-fx-border-radius: 4px;"
            )
        );
        
        // Aplica estilos no container de login se existir
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
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @Override
    public void stop() {
        System.out.println("Aplicação encerrada");
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
