package com.buyo.adminfx.ui.controllers;

import com.buyo.adminfx.dao.CouponDAO;
import com.buyo.adminfx.model.Coupon;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CouponsController implements SearchableController {
    @FXML private TextField codeFilter;
    @FXML private CheckBox activeOnly;

    @FXML private TableView<Coupon> table;
    @FXML private TableColumn<Coupon, Integer> colId;
    @FXML private TableColumn<Coupon, String> colCode;
    @FXML private TableColumn<Coupon, Boolean> colActive;
    @FXML private TableColumn<Coupon, LocalDate> colExpires;
    @FXML private TableColumn<Coupon, BigDecimal> colPercent;
    @FXML private TableColumn<Coupon, BigDecimal> colAmount;
    @FXML private TableColumn<Coupon, BigDecimal> colMinimum;

    private final CouponDAO dao = new CouponDAO();
    private final ObservableList<Coupon> rows = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colCode != null) colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        if (colActive != null) colActive.setCellValueFactory(new PropertyValueFactory<>("active"));
        if (colExpires != null) colExpires.setCellValueFactory(new PropertyValueFactory<>("expiresAt"));
        if (colPercent != null) colPercent.setCellValueFactory(new PropertyValueFactory<>("percent"));
        if (colAmount != null) colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        if (colMinimum != null) colMinimum.setCellValueFactory(new PropertyValueFactory<>("minimum"));
        if (table != null) table.setItems(rows);
        refresh();
    }

    @FXML public void onApplyFilters(ActionEvent e) { refresh(); }
    @FXML public void onClearFilters(ActionEvent e) { if (codeFilter != null) codeFilter.clear(); if (activeOnly != null) activeOnly.setSelected(true); refresh(); }
    @FXML public void onRefresh(ActionEvent e) { refresh(); }

    @FXML
    public void onNewCoupon(ActionEvent e) {
        TextInputDialog codeDlg = new TextInputDialog("");
        codeDlg.setTitle("Cupom"); codeDlg.setHeaderText(null); codeDlg.setContentText("Código:");
        var codeRes = codeDlg.showAndWait(); if (codeRes.isEmpty()) return; String code = codeRes.get().trim(); if (code.isBlank()) { error("Cupom", "Código inválido."); return; }

        ChoiceDialog<String> activeDlg = new ChoiceDialog<>("Ativo", FXCollections.observableArrayList("Ativo", "Inativo"));
        activeDlg.setTitle("Cupom"); activeDlg.setHeaderText(null); activeDlg.setContentText("Status:");
        var activeRes = activeDlg.showAndWait(); if (activeRes.isEmpty()) return; boolean active = "Ativo".equalsIgnoreCase(activeRes.get());

        TextInputDialog expDlg = new TextInputDialog("");
        expDlg.setTitle("Cupom"); expDlg.setHeaderText(null); expDlg.setContentText("Expira (YYYY-MM-DD) ou vazio:");
        var expRes = expDlg.showAndWait(); if (expRes.isEmpty()) return; LocalDate exp = null; try { String s = expRes.get().trim(); if (!s.isEmpty()) exp = LocalDate.parse(s); } catch (Exception ex) { error("Cupom", "Data inválida."); return; }

        TextInputDialog pctDlg = new TextInputDialog("");
        pctDlg.setTitle("Cupom"); pctDlg.setHeaderText(null); pctDlg.setContentText("Percentual (%) opcional:");
        var pctRes = pctDlg.showAndWait(); if (pctRes.isEmpty()) return; BigDecimal pct = null; try { String s = pctRes.get().trim().replace(',', '.'); if (!s.isEmpty()) pct = new BigDecimal(s); } catch (Exception ex) { error("Cupom", "% inválido."); return; }

        TextInputDialog valDlg = new TextInputDialog("");
        valDlg.setTitle("Cupom"); valDlg.setHeaderText(null); valDlg.setContentText("Valor (fixo) opcional:");
        var valRes = valDlg.showAndWait(); if (valRes.isEmpty()) return; BigDecimal val = null; try { String s = valRes.get().trim().replace(',', '.'); if (!s.isEmpty()) val = new BigDecimal(s); } catch (Exception ex) { error("Cupom", "Valor inválido."); return; }

        TextInputDialog minDlg = new TextInputDialog("");
        minDlg.setTitle("Cupom"); minDlg.setHeaderText(null); minDlg.setContentText("Mínimo opcional:");
        var minRes = minDlg.showAndWait(); if (minRes.isEmpty()) return; BigDecimal min = null; try { String s = minRes.get().trim().replace(',', '.'); if (!s.isEmpty()) min = new BigDecimal(s); } catch (Exception ex) { error("Cupom", "Mínimo inválido."); return; }

        boolean ok = dao.insert(code, active, exp, pct, val, min);
        if (!ok) { error("Cupom", "Falha ao criar cupom."); return; }
        info("Cupom", "Cupom criado.");
        refresh();
    }

    private void refresh() {
        String codeLike = codeFilter != null ? codeFilter.getText() : null;
        Boolean active = activeOnly != null && activeOnly.isSelected();
        List<Coupon> list = dao.list(codeLike, active);
        rows.setAll(list);
    }

    private void info(String title, String msg) { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setHeaderText(null); a.setTitle(title); a.setContentText(msg); a.show(); }
    private void error(String title, String msg) { Alert a = new Alert(Alert.AlertType.ERROR); a.setHeaderText(null); a.setTitle(title); a.setContentText(msg); a.show(); }

    @Override public void applySearch(String query) { }
}
