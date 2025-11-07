package com.buyo.adminfx.ui.controllers;

import com.buyo.adminfx.dao.ProductDAO;
import com.buyo.adminfx.dao.CategoryDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.io.File;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.text.NumberFormat;
import java.util.Locale;

public class ProductFormController {
    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField stockField;
    @FXML private TextArea imageUrlField;
    @FXML private javafx.scene.control.CheckBox activeCheck;
    @FXML private Label statusLabel;
    @FXML private javafx.scene.control.CheckBox createCategoryCheck;
    @FXML private javafx.scene.layout.VBox newCategoryBox;
    @FXML private TextField newCategoryName;
    @FXML private javafx.scene.control.TextArea newCategoryDesc;

    private Integer editingId = null;
    private final Map<String, Integer> categoryIndex = new LinkedHashMap<>();
    private boolean saved = false;

    public boolean isSaved() { return saved; }

    @FXML
    public void initialize() {
        // Carrega categorias no combobox
        loadCategories(null);
    }

    public void setProduct(com.buyo.adminfx.model.Product p) {
        if (p == null) return;
        editingId = p.getId();
        nameField.setText(p.getName());
        if (p.getPrice() == null) {
            priceField.setText("");
        } else {
            try {
                NumberFormat nf = NumberFormat.getNumberInstance(Locale.of("pt","BR"));
                nf.setGroupingUsed(true);
                nf.setMinimumFractionDigits(2);
                nf.setMaximumFractionDigits(2);
                priceField.setText(nf.format(p.getPrice()));
            } catch (Exception ignore) {
                priceField.setText(p.getPrice().toPlainString());
            }
        }
        stockField.setText(String.valueOf(p.getStock()));
        imageUrlField.setText(p.getImageUrl());
        if (p.getCategoryName() != null) {
            categoryCombo.getSelectionModel().select(p.getCategoryName());
        }
        activeCheck.setSelected(p.isActive());
    }

    @FXML
    public void onPickImage(ActionEvent e) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Escolher imagem do produto");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File f = fc.showOpenDialog(nameField.getScene().getWindow());
        if (f != null) {
            String cur = trim(imageUrlField.getText());
            String add = f.toURI().toString();
            imageUrlField.setText(cur.isBlank() ? add : (cur + "\n" + add));
            setStatus("Imagem selecionada.");
        }
    }

    @FXML
    public void onSave(ActionEvent e) {
        String name = trim(nameField.getText());
        String priceStr = trim(priceField.getText());
        String stockStr = trim(stockField.getText());
        String imageUrl = trim(imageUrlField.getText());
        boolean active = activeCheck.isSelected();

        if (name.isBlank() || priceStr.isBlank()) {
            setStatus("Preencha nome e preço.");
            return;
        }
        BigDecimal price;
        try {
            String normalized;
            if (priceStr.contains(",")) {
                // Formato BR: 5.890,00 -> 5890.00
                normalized = priceStr.replace(".", "").replace(",", ".");
            } else {
                // Já está em formato com ponto decimal (ex.: 5890.00)
                normalized = priceStr;
            }
            price = new BigDecimal(normalized.trim());
        } catch (Exception ex) {
            setStatus("Preço inválido.");
            return;
        }
        int stock = 0;
        try { stock = Integer.parseInt(stockStr.isBlank() ? "0" : stockStr); } catch (Exception ignore) {}
        Integer categoryId = null;
        String sel = categoryCombo.getSelectionModel().getSelectedItem();
        if (sel != null) categoryId = categoryIndex.get(sel);

        var dao = new ProductDAO();
        boolean ok;
        if (editingId == null) {
            Integer newId = dao.createProduct(name, price, categoryId, imageUrl, stock, active);
            ok = newId != null;
        } else {
            ok = dao.updateProduct(editingId, name, price, categoryId, imageUrl, stock, active);
        }
        if (!ok) {
            setStatus("Falha ao salvar produto.");
            return;
        }
        saved = true;
        close();
    }

    @FXML
    public void onCancel(ActionEvent e) { close(); }

    @FXML
    public void onManageCategories(ActionEvent e) {
        // Guarda seleção atual para tentar preservar após gerenciamento
        Integer selectedId = null;
        String selName = categoryCombo.getSelectionModel().getSelectedItem();
        if (selName != null) {
            selectedId = categoryIndex.get(selName);
        }

        try {
            URL fxml = getClass().getResource("/com/buyo/adminfx/ui/CategoryView.fxml");
            if (fxml == null) {
                String userDir = System.getProperty("user.dir");
                Path p1 = Paths.get(userDir, "buyo-adminfx", "src", "main", "resources", "com", "buyo", "adminfx", "ui", "CategoryView.fxml");
                Path p2 = Paths.get(userDir, "src", "main", "resources", "com", "buyo", "adminfx", "ui", "CategoryView.fxml");
                Path existing = Files.exists(p1) ? p1 : (Files.exists(p2) ? p2 : null);
                if (existing != null) {
                    fxml = existing.toUri().toURL();
                }
            }
            FXMLLoader loader = new FXMLLoader(fxml);
            Parent root = loader.load();
            Stage dlg = new Stage();
            dlg.setTitle("Gerenciar Categorias");
            dlg.initModality(Modality.APPLICATION_MODAL);
            dlg.setScene(new Scene(root, 700, 480));
            dlg.showAndWait();
        } catch (Exception ex) {
            setStatus("Falha ao abrir categorias: " + ex.getMessage());
        }

        // Recarrega categorias e tenta restaurar seleção
        loadCategories(selectedId);
    }

    @FXML
    public void onToggleCreateCategory(ActionEvent e) {
        boolean show = createCategoryCheck != null && createCategoryCheck.isSelected();
        if (newCategoryBox != null) {
            newCategoryBox.setVisible(show);
            newCategoryBox.setManaged(show);
        }
        if (show) {
            if (newCategoryName != null) newCategoryName.requestFocus();
        }
    }

    @FXML
    public void onSaveCategory(ActionEvent e) {
        String cname = trim(newCategoryName != null ? newCategoryName.getText() : "");
        String cdesc = trim(newCategoryDesc != null ? newCategoryDesc.getText() : "");
        if (cname.isBlank()) {
            setStatus("Informe o nome da categoria.");
            return;
        }
        var cdao = new CategoryDAO();
        Integer newId = cdao.createCategory(cname, cdesc);
        if (newId == null) {
            setStatus("Falha ao criar categoria.");
            return;
        }
        // Recarrega e seleciona a recém-criada
        loadCategories(newId);
        // Limpa campos e esconde box
        if (newCategoryName != null) newCategoryName.clear();
        if (newCategoryDesc != null) newCategoryDesc.clear();
        if (createCategoryCheck != null) createCategoryCheck.setSelected(false);
        onToggleCreateCategory(null);
        setStatus("Categoria criada com sucesso.");
    }

    private void close() {
        Stage st = (Stage) nameField.getScene().getWindow();
        st.close();
    }

    private String trim(String s) { return s == null ? "" : s.trim(); }
    private void setStatus(String s) { if (statusLabel != null) statusLabel.setText(s); }

    private void loadCategories(Integer preferSelectId) {
        categoryIndex.clear();
        var catDao = new CategoryDAO();
        ObservableList<String> items = FXCollections.observableArrayList();
        catDao.listAll().forEach(c -> {
            items.add(c.getName());
            categoryIndex.put(c.getName(), c.getId());
        });
        categoryCombo.setItems(items);
        if (preferSelectId != null) {
            // procura o nome que corresponde ao ID preferido
            for (Map.Entry<String, Integer> e : categoryIndex.entrySet()) {
                if (preferSelectId.equals(e.getValue())) {
                    categoryCombo.getSelectionModel().select(e.getKey());
                    break;
                }
            }
        }
    }
}
