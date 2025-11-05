package com.buyo.adminfx.services;

import com.buyo.adminfx.auth.Session;
import com.buyo.adminfx.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PaymentService {
    private String lastError;
    public String getLastError() { return lastError; }

    public boolean onPaymentConfirmed(int orderId) {
        Integer adminId = Session.getCurrentUser() != null ? Session.getCurrentUser().getId() : null;
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            // 1) Atualiza status do pedido para PAGO e registra histórico (tenta nova tabela; se falhar, legacy)
            String prev = null;
            try (PreparedStatement ps = conn.prepareStatement("SELECT status FROM pedidos WHERE id = ?")) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) prev = rs.getString(1);
                }
            }
            if (prev == null) { conn.rollback(); lastError = "Pedido não encontrado"; return false; }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE pedidos SET status = 'PAGO' WHERE id = ?")) {
                ps.setInt(1, orderId);
                if (ps.executeUpdate() == 0) { conn.rollback(); lastError = "Falha ao atualizar status do pedido"; return false; }
            }
            boolean logged = false;
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO historico_status_pedido (pedido_id, admin_id, status_anterior, status_novo) VALUES (?,?,?,?)")) {
                ps.setInt(1, orderId);
                if (adminId == null) ps.setNull(2, java.sql.Types.INTEGER); else ps.setInt(2, adminId);
                ps.setString(3, prev);
                ps.setString(4, "PAGO");
                ps.executeUpdate();
                logged = true;
            } catch (Exception ignore) {
                // fallback legacy
            }
            if (!logged) {
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO pedido_status_log (pedido_id, admin_id, status_anterior, status_novo) VALUES (?,?,?,?)")) {
                    ps.setInt(1, orderId);
                    if (adminId == null) ps.setNull(2, java.sql.Types.INTEGER); else ps.setInt(2, adminId);
                    ps.setString(3, prev);
                    ps.setString(4, "PAGO");
                    ps.executeUpdate();
                }
            }

            // 2) Carrega itens do pedido
            int processed = 0;
            // Descobre nomes para produto e quantidade em pedido_itens e usa alias fixo
            String itemProdCol = resolveColumn(conn, "pedido_itens", "produto_id", "product_id", "id_produto", "produto");
            String itemQtyCol = resolveColumn(conn, "pedido_itens", "quantidade", "qtd", "quant", "qte", "quantidade_produto");
            if (itemProdCol == null || itemQtyCol == null) { conn.rollback(); lastError = "Colunas de itens do pedido não encontradas"; return false; }
            String sqlItems = "SELECT " + itemProdCol + " AS produto_id, " + itemQtyCol + " AS quantidade FROM pedido_itens WHERE pedido_id = ? ORDER BY id";
            try (PreparedStatement psItems = conn.prepareStatement(sqlItems)) {
                psItems.setInt(1, orderId);
                try (ResultSet rs = psItems.executeQuery()) {
                    // Descobre nomes das colunas de log_produtos somente uma vez
                    String productCol = resolveColumn(conn, "log_produtos", "produto_id", "product_id", "id_produto", "produto");
                    String qtyCol = resolveColumn(conn, "log_produtos", "quantidade", "qtd", "quantidade_alterada", "quant", "qte");
                    String actionCol = resolveColumn(conn, "log_produtos", "tipo_acao", "acao", "tipo");
                    String adminCol = resolveColumn(conn, "log_produtos", "admin_id", "usuario_id");
                    String orderCol = resolveColumn(conn, "log_produtos", "pedido_id", "ordem_id");
                    String noteCol = resolveColumn(conn, "log_produtos", "observacao", "obs", "nota", "comentario");
                    String createdCol = resolveColumn(conn, "log_produtos", "criado_em", "created_at", "data_criacao", "data");
                    // Descobre coluna de quantidade no estoque
                    String estQtyCol = resolveColumn(conn, "estoque", "quantidade", "qtd", "quant", "qte");
                    if (estQtyCol == null) { conn.rollback(); lastError = "Coluna de quantidade do estoque não encontrada"; return false; }
                    while (rs.next()) {
                        int pid = rs.getInt("produto_id");
                        int qty = rs.getInt("quantidade");

                        // 3) Debita estoque: exige quantidade suficiente (evita negativos)
                        Integer cur = null;
                        String sqlGet = "SELECT " + estQtyCol + " FROM estoque WHERE produto_id = ?";
                        try (PreparedStatement psGet = conn.prepareStatement(sqlGet)) {
                            psGet.setInt(1, pid);
                            try (ResultSet rsq = psGet.executeQuery()) {
                                if (rsq.next()) cur = (Integer) rsq.getObject(1);
                            }
                        }
                        if (cur == null) { conn.rollback(); lastError = "Sem estoque cadastrado para o produto ID " + pid; return false; }
                        if (cur < qty) { conn.rollback(); lastError = "Estoque insuficiente para o produto ID " + pid + " (atual=" + cur + ", pedido=" + qty + ")"; return false; }
                        int updated;
                        String sqlUpd = "UPDATE estoque SET " + estQtyCol + " = " + estQtyCol + " - ? WHERE produto_id = ? AND " + estQtyCol + " >= ?";
                        try (PreparedStatement psUpd = conn.prepareStatement(sqlUpd)) {
                            psUpd.setInt(1, qty);
                            psUpd.setInt(2, pid);
                            psUpd.setInt(3, qty);
                            updated = psUpd.executeUpdate();
                        }
                        if (updated == 0) { conn.rollback(); lastError = "Falha ao debitar estoque do produto ID " + pid; return false; }

                        // 4) Log de produto (dinâmico e tolerante)
                        if (productCol != null && qtyCol != null) {
                            StringBuilder cols = new StringBuilder(productCol + ", " + qtyCol);
                            StringBuilder vals = new StringBuilder("?,?");
                            if (actionCol != null) { cols.append(", ").append(actionCol); vals.append(",?"); }
                            if (adminCol != null) { cols.append(", ").append(adminCol); vals.append(",?"); }
                            if (orderCol != null) { cols.append(", ").append(orderCol); vals.append(",?"); }
                            if (noteCol != null) { cols.append(", ").append(noteCol); vals.append(",?"); }
                            boolean includeCreatedNow = createdCol != null;
                            if (includeCreatedNow) { cols.append(", ").append(createdCol); vals.append(", CURRENT_TIMESTAMP"); }

                            String sqlLog = "INSERT INTO log_produtos (" + cols + ") VALUES (" + vals + ")";
                            try (PreparedStatement psLog = conn.prepareStatement(sqlLog)) {
                                int k = 1;
                                psLog.setInt(k++, pid);
                                psLog.setInt(k++, qty);
                                if (actionCol != null) psLog.setString(k++, "VENDA");
                                if (adminCol != null) {
                                    if (adminId == null) psLog.setNull(k++, java.sql.Types.INTEGER); else psLog.setInt(k++, adminId);
                                }
                                if (orderCol != null) psLog.setInt(k++, orderId);
                                if (noteCol != null) psLog.setString(k++, "");
                                psLog.executeUpdate();
                            } catch (Exception ignoreLog) {
                                // não falha confirmação por causa de log
                            }
                        }
                        processed++;
                    }
                }
            }
            if (processed == 0) { conn.rollback(); lastError = "Pedido sem itens"; return false; }

            conn.commit();
            return true;
        } catch (Exception ex) {
            lastError = ex.getMessage();
            return false;
        }
    }

    private String resolveColumn(Connection c, String table, String... candidates) {
        final String sql = "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ? LIMIT 1";
        for (String cand : candidates) {
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, table);
                ps.setString(2, cand);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return cand;
                }
            } catch (Exception ignore) {}
        }
        return null;
    }
}
