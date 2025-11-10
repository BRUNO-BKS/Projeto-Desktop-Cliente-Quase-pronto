package com.buyo.adminfx.ui.controllers;

import com.buyo.adminfx.dao.CustomerDAO;
import com.buyo.adminfx.model.Customer;
import javafx.collections.transformation.FilteredList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TextField;

public class CustomerController implements SearchableController {
    @FXML private TableView<Customer> table;
    @FXML private TableColumn<Customer, Integer> colId;
    @FXML private TableColumn<Customer, String> colName;
    @FXML private TableColumn<Customer, String> colEmail;
    @FXML private TableColumn<Customer, String> colPhone;
    @FXML private TableColumn<Customer, String> colCreatedAt;
    @FXML private TableColumn<Customer, String> colLastActive;
    @FXML private TextField searchField;

    private ObservableList<Customer> masterData;
    private FilteredList<Customer> filtered;

    @FXML
    public void initialize() {
        System.out.println("\n=== Inicializando CustomerController ===");
        System.out.println("table é nulo? " + (table == null ? "SIM" : "não"));
        System.out.println("searchField é nulo? " + (searchField == null ? "SIM" : "não"));
        
        try {
            System.out.println("Configurando colunas da tabela...");
            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colName.setCellValueFactory(new PropertyValueFactory<>("name"));
            colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
            colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
            
            if (colCreatedAt != null) {
                colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
                System.out.println("Coluna 'Criado em' configurada");
            } else {
                System.err.println("AVISO: colCreatedAt é nulo");
            }
            
            if (colLastActive != null) {
                colLastActive.setCellValueFactory(new PropertyValueFactory<>("lastActive"));
                System.out.println("Coluna 'Último ativo' configurada");
            } else {
                System.err.println("AVISO: colLastActive é nulo");
            }

            System.out.println("Carregando dados dos clientes...");
            try {
                CustomerDAO dao = new CustomerDAO();
                System.out.println("DAO inicializado, buscando clientes...");
                masterData = FXCollections.observableArrayList(dao.listAll());
                System.out.println("Total de clientes carregados: " + masterData.size());
                
                filtered = new FilteredList<>(masterData, c -> true);
                System.out.println("Filtro aplicado");
                
                if (table != null) {
                    table.setItems(filtered);
                    System.out.println("Itens definidos na tabela");
                } else {
                    System.err.println("ERRO: table é nulo, não é possível definir os itens");
                }
                
                if (searchField != null) {
                    searchField.textProperty().addListener((obs, ov, nv) -> applySearch(nv));
                    System.out.println("Listener de pesquisa configurado");
                } else {
                    System.err.println("AVISO: searchField é nulo, não foi possível configurar o listener de pesquisa");
                }
                
            } catch (Exception e) {
                System.err.println("ERRO ao carregar clientes: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("ERRO na inicialização do CustomerController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void applySearch(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        if (filtered == null) return;
        if (q.isEmpty()) {
            filtered.setPredicate(c -> true);
            return;
        }
        filtered.setPredicate(c -> {
            if (c == null) return false;
            String id = String.valueOf(c.getId());
            String name = c.getName() == null ? "" : c.getName().toLowerCase();
            String email = c.getEmail() == null ? "" : c.getEmail().toLowerCase();
            String phone = c.getPhone() == null ? "" : c.getPhone().toLowerCase();
            String created = c.getCreatedAt() == null ? "" : c.getCreatedAt().toLowerCase();
            String last = c.getLastActive() == null ? "" : c.getLastActive().toLowerCase();
            return id.contains(q) || name.contains(q) || email.contains(q) || phone.contains(q) || created.contains(q) || last.contains(q);
        });
    }
}
