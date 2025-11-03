package com.buyo.adminfx.services;

import com.buyo.adminfx.auth.Session;
import com.buyo.adminfx.dao.OrderDAO;
import com.buyo.adminfx.dao.ProductDAO;
import com.buyo.adminfx.dao.ProductLogDAO;
import com.buyo.adminfx.model.OrderItem;

import java.util.List;

public class PaymentService {
    private final OrderDAO orderDAO = new OrderDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final ProductLogDAO productLogDAO = new ProductLogDAO();

    public void onPaymentConfirmed(int orderId) {
        Integer adminId = Session.getCurrentUser() != null ? Session.getCurrentUser().getId() : null;
        orderDAO.updateStatus(orderId, "PAGO", adminId);
        List<OrderItem> items = orderDAO.listItems(orderId);
        for (OrderItem it : items) {
            productDAO.adjustStock(it.getProductId(), -it.getQuantity());
            productLogDAO.log(it.getProductId(), "VENDA", it.getQuantity(), adminId, orderId, "");
        }
    }
}
