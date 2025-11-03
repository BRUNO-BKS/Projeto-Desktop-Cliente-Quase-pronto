package com.buyo.adminfx.services;

import com.buyo.adminfx.dao.CouponDAO;
import com.buyo.adminfx.dao.OrderDAO;
import com.buyo.adminfx.model.Coupon;
import com.buyo.adminfx.model.OrderItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class CouponService {
    private final CouponDAO couponDAO = new CouponDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    public Result applyCoupon(int orderId, String code) {
        if (code == null || code.isBlank()) return Result.fail("Código inválido");
        Coupon c = couponDAO.getByCode(code.trim(), true);
        if (c == null) return Result.fail("Cupom não encontrado/expirado/inativo");

        List<OrderItem> items = orderDAO.listItems(orderId);
        if (items.isEmpty()) return Result.fail("Pedido sem itens");

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem it : items) {
            BigDecimal sub = (it.getUnitPrice() == null ? BigDecimal.ZERO : it.getUnitPrice()).multiply(new BigDecimal(it.getQuantity()));
            total = total.add(sub);
        }
        if (total.compareTo(BigDecimal.ZERO) <= 0) return Result.fail("Total do pedido é 0");
        if (c.getMinimum() != null && total.compareTo(c.getMinimum()) < 0) return Result.fail("Total não atinge o mínimo do cupom");

        BigDecimal discount = BigDecimal.ZERO;
        if (c.getPercent() != null && c.getPercent().compareTo(BigDecimal.ZERO) > 0) {
            discount = total.multiply(c.getPercent().divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP));
        }
        if (c.getAmount() != null && c.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            discount = discount.add(c.getAmount());
        }
        if (discount.compareTo(BigDecimal.ZERO) <= 0) return Result.fail("Cupom sem valor/percentual válido");
        if (discount.compareTo(total) > 0) discount = total; // não passar do total

        // Distribuir o desconto proporcionalmente nos itens ajustando o preço unitário (sem alterar schema)
        BigDecimal remaining = discount;
        for (int i = 0; i < items.size(); i++) {
            OrderItem it = items.get(i);
            BigDecimal itemSubtotal = (it.getUnitPrice() == null ? BigDecimal.ZERO : it.getUnitPrice()).multiply(new BigDecimal(it.getQuantity()));
            if (itemSubtotal.compareTo(BigDecimal.ZERO) <= 0) continue;
            BigDecimal share = (i == items.size() - 1)
                    ? remaining
                    : discount.multiply(itemSubtotal).divide(total, 6, RoundingMode.HALF_UP);
            if (share.compareTo(remaining) > 0) share = remaining;
            remaining = remaining.subtract(share);

            BigDecimal newSubtotal = itemSubtotal.subtract(share).max(BigDecimal.ZERO);
            BigDecimal newUnit = newSubtotal.divide(new BigDecimal(it.getQuantity()), 2, RoundingMode.HALF_UP);
            if (!orderDAO.updateItem(it.getId(), it.getQuantity(), newUnit)) {
                return Result.fail("Falha ao atualizar item #" + it.getId());
            }
        }
        orderDAO.recalcTotal(orderId);
        return Result.ok("Cupom aplicado: -R$ " + discount.setScale(2, RoundingMode.HALF_UP));
    }

    public static class Result {
        public final boolean ok; public final String message;
        private Result(boolean ok, String message) { this.ok = ok; this.message = message; }
        public static Result ok(String m) { return new Result(true, m); }
        public static Result fail(String m) { return new Result(false, m); }
    }
}
