package com.buyo.adminfx.ui.controllers;

import com.buyo.adminfx.dao.BannerDAO;
import com.buyo.adminfx.model.Banner;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.time.LocalDateTime;
import java.util.Optional;

public class BannersController implements SearchableController {

    @FXML private TableView<Banner> table;
    @FXML private TableColumn<Banner, Integer> colId;
    @FXML private TableColumn<Banner, String> colTitle;
    @FXML private TableColumn<Banner, String> colSubtitle;
    @FXML private TableColumn<Banner, String> colImageUrl;
    @FXML private TableColumn<Banner, String> colLinkUrl;
    @FXML private TableColumn<Banner, Integer> colOrder;
    @FXML private TableColumn<Banner, Boolean> colActive;
    @FXML private TableColumn<Banner, LocalDateTime> colStartAt;
    @FXML private TableColumn<Banner, LocalDateTime> colEndAt;
    @FXML private TableColumn<Banner, LocalDateTime> colCreatedAt;
    @FXML private TableColumn<Banner, LocalDateTime> colUpdatedAt;

    private final BannerDAO dao = new BannerDAO();
    private final ObservableList<Banner> rows = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colTitle != null) colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (colSubtitle != null) colSubtitle.setCellValueFactory(new PropertyValueFactory<>("subtitle"));
        if (colImageUrl != null) colImageUrl.setCellValueFactory(new PropertyValueFactory<>("imageUrl"));
        if (colLinkUrl != null) colLinkUrl.setCellValueFactory(new PropertyValueFactory<>("linkUrl"));
        if (colOrder != null) colOrder.setCellValueFactory(new PropertyValueFactory<>("order"));
        if (colActive != null) colActive.setCellValueFactory(new PropertyValueFactory<>("active"));
        if (colStartAt != null) colStartAt.setCellValueFactory(new PropertyValueFactory<>("startAt"));
        if (colEndAt != null) colEndAt.setCellValueFactory(new PropertyValueFactory<>("endAt"));
        if (colCreatedAt != null) colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        if (colUpdatedAt != null) colUpdatedAt.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));

        if (colImageUrl != null) {
            colImageUrl.setCellFactory(col -> new TableCell<Banner, String>() {
                private final ImageView imageView = new ImageView();
                {
                    imageView.setFitWidth(80);
                    imageView.setFitHeight(40);
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);
                }

                @Override
                protected void updateItem(String url, boolean empty) {
                    super.updateItem(url, empty);
                    if (empty || url == null || url.trim().isEmpty()) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        try {
                            imageView.setImage(new Image(url, true));
                            setGraphic(imageView);
                            setText(null);
                        } catch (Exception ex) {
                            setGraphic(null);
                            setText(url);
                        }
                    }
                }
            });
        }

        if (table != null) table.setItems(rows);
        refresh();
    }

    private void refresh() {
        rows.setAll(dao.listAll());
    }

    @Override
    public void applySearch(String query) {
        // Por enquanto, nenhuma busca global específica para banners.
    }

    @FXML
    public void onNewBanner(ActionEvent e) {
        TextInputDialog titleDlg = new TextInputDialog("");
        titleDlg.setTitle("Banners");
        titleDlg.setHeaderText(null);
        titleDlg.setContentText("Título:");
        Optional<String> titleRes = titleDlg.showAndWait();
        if (titleRes.isEmpty()) return;
        String title = titleRes.get().trim();
        if (title.isEmpty()) { error("Banners", "Título é obrigatório."); return; }

        TextInputDialog subDlg = new TextInputDialog("");
        subDlg.setTitle("Banners");
        subDlg.setHeaderText(null);
        subDlg.setContentText("Subtítulo (opcional):");
        Optional<String> subRes = subDlg.showAndWait();
        String subtitle = subRes.isEmpty() ? null : subRes.get().trim();
        if (subtitle != null && subtitle.isEmpty()) subtitle = null;

        TextInputDialog imgDlg = new TextInputDialog("");
        imgDlg.setTitle("Banners");
        imgDlg.setHeaderText(null);
        imgDlg.setContentText("Imagem URL:");
        Optional<String> imgRes = imgDlg.showAndWait();
        if (imgRes.isEmpty()) return;
        String imageUrl = imgRes.get().trim();
        if (imageUrl.isEmpty()) { error("Banners", "Imagem URL é obrigatória."); return; }

        TextInputDialog linkDlg = new TextInputDialog("");
        linkDlg.setTitle("Banners");
        linkDlg.setHeaderText(null);
        linkDlg.setContentText("Link URL (opcional):");
        Optional<String> linkRes = linkDlg.showAndWait();
        String linkUrl = linkRes.isEmpty() ? null : linkRes.get().trim();
        if (linkUrl != null && linkUrl.isEmpty()) linkUrl = null;

        TextInputDialog orderDlg = new TextInputDialog("0");
        orderDlg.setTitle("Banners");
        orderDlg.setHeaderText(null);
        orderDlg.setContentText("Ordem (número):");
        Optional<String> orderRes = orderDlg.showAndWait();
        if (orderRes.isEmpty()) return;
        int order;
        try {
            order = Integer.parseInt(orderRes.get().trim());
        } catch (Exception ex) {
            error("Banners", "Ordem inválida.");
            return;
        }

        ChoiceDialog<String> activeDlg = new ChoiceDialog<>("Ativo", FXCollections.observableArrayList("Ativo", "Inativo"));
        activeDlg.setTitle("Banners");
        activeDlg.setHeaderText(null);
        activeDlg.setContentText("Status:");
        Optional<String> activeRes = activeDlg.showAndWait();
        if (activeRes.isEmpty()) return;
        boolean active = "Ativo".equalsIgnoreCase(activeRes.get());

        boolean ok = dao.insert(title, subtitle, imageUrl, linkUrl, order, active);
        if (!ok) { error("Banners", "Falha ao criar banner."); return; }
        info("Banners", "Banner criado com sucesso.");
        refresh();
    }

    @FXML
    public void onEditBanner(ActionEvent e) {
        Banner sel = table != null ? table.getSelectionModel().getSelectedItem() : null;
        if (sel == null) { error("Banners", "Selecione um banner para editar."); return; }

        TextInputDialog titleDlg = new TextInputDialog(sel.getTitle());
        titleDlg.setTitle("Banners");
        titleDlg.setHeaderText(null);
        titleDlg.setContentText("Título:");
        Optional<String> titleRes = titleDlg.showAndWait();
        if (titleRes.isEmpty()) return;
        String title = titleRes.get().trim();
        if (title.isEmpty()) { error("Banners", "Título é obrigatório."); return; }

        TextInputDialog subDlg = new TextInputDialog(sel.getSubtitle() == null ? "" : sel.getSubtitle());
        subDlg.setTitle("Banners");
        subDlg.setHeaderText(null);
        subDlg.setContentText("Subtítulo (opcional):");
        Optional<String> subRes = subDlg.showAndWait();
        String subtitle = subRes.isEmpty() ? null : subRes.get().trim();
        if (subtitle != null && subtitle.isEmpty()) subtitle = null;

        TextInputDialog imgDlg = new TextInputDialog(sel.getImageUrl());
        imgDlg.setTitle("Banners");
        imgDlg.setHeaderText(null);
        imgDlg.setContentText("Imagem URL:");
        Optional<String> imgRes = imgDlg.showAndWait();
        if (imgRes.isEmpty()) return;
        String imageUrl = imgRes.get().trim();
        if (imageUrl.isEmpty()) { error("Banners", "Imagem URL é obrigatória."); return; }

        TextInputDialog linkDlg = new TextInputDialog(sel.getLinkUrl() == null ? "" : sel.getLinkUrl());
        linkDlg.setTitle("Banners");
        linkDlg.setHeaderText(null);
        linkDlg.setContentText("Link URL (opcional):");
        Optional<String> linkRes = linkDlg.showAndWait();
        String linkUrl = linkRes.isEmpty() ? null : linkRes.get().trim();
        if (linkUrl != null && linkUrl.isEmpty()) linkUrl = null;

        TextInputDialog orderDlg = new TextInputDialog(String.valueOf(sel.getOrder()));
        orderDlg.setTitle("Banners");
        orderDlg.setHeaderText(null);
        orderDlg.setContentText("Ordem (número):");
        Optional<String> orderRes = orderDlg.showAndWait();
        if (orderRes.isEmpty()) return;
        int order;
        try {
            order = Integer.parseInt(orderRes.get().trim());
        } catch (Exception ex) {
            error("Banners", "Ordem inválida.");
            return;
        }

        ChoiceDialog<String> activeDlg = new ChoiceDialog<>(sel.isActive() ? "Ativo" : "Inativo", FXCollections.observableArrayList("Ativo", "Inativo"));
        activeDlg.setTitle("Banners");
        activeDlg.setHeaderText(null);
        activeDlg.setContentText("Status:");
        Optional<String> activeRes = activeDlg.showAndWait();
        if (activeRes.isEmpty()) return;
        boolean active = "Ativo".equalsIgnoreCase(activeRes.get());

        boolean ok = dao.update(sel.getId(), title, subtitle, imageUrl, linkUrl, order, active);
        if (!ok) { error("Banners", "Falha ao atualizar banner."); return; }
        info("Banners", "Banner atualizado com sucesso.");
        refresh();
    }

    @FXML
    public void onDeleteBanner(ActionEvent e) {
        Banner sel = table != null ? table.getSelectionModel().getSelectedItem() : null;
        if (sel == null) { error("Banners", "Selecione um banner para remover."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Banners");
        confirm.setHeaderText(null);
        confirm.setContentText("Remover o banner '" + sel.getTitle() + "'?");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK) return;

        boolean ok = dao.delete(sel.getId());
        if (!ok) { error("Banners", "Falha ao remover banner."); return; }
        info("Banners", "Banner removido.");
        refresh();
    }

    private void info(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setTitle(title);
        a.setContentText(msg);
        a.show();
    }

    private void error(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setTitle(title);
        a.setContentText(msg);
        a.show();
    }
}
