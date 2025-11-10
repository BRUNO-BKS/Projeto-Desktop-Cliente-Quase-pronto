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
        System.out.println("\n=== Inicializando MainController ===");
        System.out.println("rootPane é nulo? " + (rootPane == null ? "SIM" : "não"));
        System.out.println("userNameLabel é nulo? " + (userNameLabel == null ? "SIM" : "não"));
        
        lastInstance = this;
        if (userNameLabel != null) {
            if (Session.getCurrentUser() != null) {
                System.out.println("Usuário atual: " + Session.getCurrentUser().getName());
                userNameLabel.setText(Session.getCurrentUser().getName());
            } else if (userNameLabel.getText() == null || userNameLabel.getText().isBlank()) {
                System.out.println("Nenhum usuário na sessão, definindo como 'Admin'");
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

    @FXML
    public void onOpenClientSignup(ActionEvent e) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setTitle("Cadastro de Cliente");
        a.setContentText("O cadastro de Cliente é externo a este sistema.");
        a.show();
    }

    @FXML
    public void onOpenCollaboratorSignup(ActionEvent e) {
        try {
            URL fxml = getClass().getResource("/com/buyo/adminfx/ui/ClientSignupView.fxml");
            if (fxml == null) {
                String userDir = System.getProperty("user.dir");
                Path p1 = Paths.get(userDir, "buyo-adminfx", "src", "main", "resources", "com", "buyo", "adminfx", "ui", "ClientSignupView.fxml");
                Path p2 = Paths.get(userDir, "src", "main", "resources", "com", "buyo", "adminfx", "ui", "ClientSignupView.fxml");
                Path existing = Files.exists(p1) ? p1 : (Files.exists(p2) ? p2 : null);
                if (existing != null) {
                    fxml = existing.toUri().toURL();
                }
            }
            FXMLLoader loader = new FXMLLoader(fxml);
            Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 600, 420);
            var css = getClass().getResource("/com/buyo/adminfx/ui/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            javafx.stage.Stage dlg = new javafx.stage.Stage();
            dlg.setTitle("Cadastrar Colaborador");
            dlg.initOwner(rootPane.getScene().getWindow());
            dlg.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dlg.setScene(scene);
            dlg.showAndWait();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setTitle("Cadastrar Colaborador");
            alert.setContentText("Falha ao abrir cadastro: " + ex.getMessage());
            alert.show();
        }
    }

    @FXML
    public void onOpenProductCreate(ActionEvent e) {
        try {
            URL fxml = getClass().getResource("/com/buyo/adminfx/ui/ProductForm.fxml");
            if (fxml == null) {
                String userDir = System.getProperty("user.dir");
                Path p1 = Paths.get(userDir, "buyo-adminfx", "src", "main", "resources", "com", "buyo", "adminfx", "ui", "ProductForm.fxml");
                Path p2 = Paths.get(userDir, "src", "main", "resources", "com", "buyo", "adminfx", "ui", "ProductForm.fxml");
                Path existing = Files.exists(p1) ? p1 : (Files.exists(p2) ? p2 : null);
                if (existing != null) {
                    fxml = existing.toUri().toURL();
                }
            }
            FXMLLoader loader = new FXMLLoader(fxml);
            Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 520, 380);
            var css = getClass().getResource("/com/buyo/adminfx/ui/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            javafx.stage.Stage dlg = new javafx.stage.Stage();
            dlg.setTitle("Cadastrar Produto");
            dlg.initOwner(rootPane.getScene().getWindow());
            dlg.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dlg.setScene(scene);
            dlg.showAndWait();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setTitle("Cadastrar Produto");
            alert.setContentText("Falha ao abrir formulário: " + ex.getMessage());
            alert.show();
        }
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
        System.out.println("Método goToDefaultList() chamado");
        if (lastInstance == null) {
            System.err.println("Erro: lastInstance é nulo em goToDefaultList()");
            return;
        }
        System.out.println("Chamando setCenterView para CustomerView.fxml");
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
        setCenterView("/com/buyo/adminfx/ui/ProdLogsView.fxml");
    }

    @FXML
    public void showProductChanges(ActionEvent e) {
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
    public void onOpenSignup(ActionEvent e) {
        try {
            URL fxml = getClass().getResource("/com/buyo/adminfx/ui/AdminSignupView.fxml");
            if (fxml == null) {
                String userDir = System.getProperty("user.dir");
                Path p1 = Paths.get(userDir, "buyo-adminfx", "src", "main", "resources", "com", "buyo", "adminfx", "ui", "AdminSignupView.fxml");
                Path p2 = Paths.get(userDir, "src", "main", "resources", "com", "buyo", "adminfx", "ui", "AdminSignupView.fxml");
                Path existing = Files.exists(p1) ? p1 : (Files.exists(p2) ? p2 : null);
                if (existing != null) {
                    fxml = existing.toUri().toURL();
                }
            }
            FXMLLoader loader = new FXMLLoader(fxml);
            Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 600, 420);
            var css = getClass().getResource("/com/buyo/adminfx/ui/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            javafx.stage.Stage dlg = new javafx.stage.Stage();
            dlg.setTitle("Cadastro");
            dlg.initOwner(rootPane.getScene().getWindow());
            dlg.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dlg.setScene(scene);
            dlg.showAndWait();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setTitle("Cadastro");
            alert.setContentText("Falha ao abrir cadastro: " + ex.getMessage());
            alert.show();
        }
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
            stage.setTitle("StockRO AdminFX - Login");
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
        System.out.println("\n=== setCenterView() chamado com: " + fxmlPath + " ===");
        System.out.println("rootPane é nulo? " + (rootPane == null ? "SIM" : "não"));
        
        try {
            // Tenta carregar o recurso do classpath
            URL fxml = getClass().getResource(fxmlPath);
            System.out.println("URL do recurso (classpath): " + fxml);
            
            // Se não encontrou no classpath, tenta encontrar no sistema de arquivos
            if (fxml == null) {
                System.err.println("Arquivo FXML não encontrado no classpath: " + fxmlPath);
                String userDir = System.getProperty("user.dir");
                System.out.println("Diretório atual: " + userDir);
                
                // fxmlPath sempre começa com '/'
                String[] segs = fxmlPath.replaceFirst("^/", "").split("/");
                if (segs.length < 5) {
                    System.err.println("Caminho FXML inválido. Esperado pelo menos 5 segmentos: " + fxmlPath);
                    return;
                }
                
                Path p1 = Paths.get(userDir, "buyo-adminfx", "src", "main", "resources", segs[0], segs[1], segs[2], segs[3], segs[4]);
                Path p2 = Paths.get(userDir, "src", "main", "resources", segs[0], segs[1], segs[2], segs[3], segs[4]);
                
                System.out.println("Procurando arquivo em: " + p1);
                System.out.println("Procurando arquivo em: " + p2);
                
                Path existing = Files.exists(p1) ? p1 : (Files.exists(p2) ? p2 : null);
                if (existing != null) {
                    System.out.println("Arquivo encontrado em: " + existing);
                    fxml = existing.toUri().toURL();
                    System.out.println("Nova URL do arquivo: " + fxml);
                } else {
                    System.err.println("ERRO: Arquivo FXML não encontrado em nenhum dos caminhos esperados");
                    return;
                }
            }
            
            // Tenta carregar o FXML
            System.out.println("Carregando FXML...");
            FXMLLoader loader = new FXMLLoader(fxml);
            
            try {
                Node content = loader.load();
                System.out.println("FXML carregado com sucesso");
                
                if (rootPane == null) {
                    System.err.println("ERRO CRÍTICO: rootPane é nulo! Não é possível definir o conteúdo.");
                    return;
                }
                
                System.out.println("Definindo conteúdo no centro do rootPane...");
                rootPane.setCenter(content);
                System.out.println("Conteúdo definido com sucesso no centro");
                
                // Ao sair do perfil, restaura menu esquerdo
                if (inProfile) {
                    System.out.println("Restaurando menu esquerdo...");
                    if (leftBackup != null) {
                        rootPane.setLeft(leftBackup);
                        System.out.println("Menu esquerdo restaurado");
                    } else {
                        System.err.println("AVISO: leftBackup é nulo, não foi possível restaurar o menu esquerdo");
                    }
                    inProfile = false;
                }
                
            } catch (Exception e) {
                System.err.println("ERRO ao carregar o FXML: " + e.getMessage());
                e.printStackTrace();
                throw e; // Re-lança a exceção para ser tratada no bloco catch externo
            }
            
        } catch (Exception ex) {
            String errorMsg = "Falha ao abrir tela: " + ex.getMessage();
            System.err.println(errorMsg);
            ex.printStackTrace();
            
            // Mostra uma mensagem de erro mais detalhada
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Erro ao carregar a tela");
            alert.setTitle("Erro de Navegação");
            alert.setContentText(errorMsg);
            alert.showAndWait();
        }
    }
}
