package com.agrisales.backend.controller;

import com.agrisales.backend.dto.CheckoutRequest;
import com.agrisales.backend.dto.Result;
import com.agrisales.backend.entity.CartItem;
import com.agrisales.backend.entity.Order;
import com.agrisales.backend.entity.OrderItem;
import com.agrisales.backend.entity.Product;
import com.agrisales.backend.mapper.CartItemMapper;
import com.agrisales.backend.mapper.OrderItemMapper;
import com.agrisales.backend.mapper.OrderMapper;
import com.agrisales.backend.mapper.ProductMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private CartItemMapper cartItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @PostMapping("/checkout")
    @Transactional
    public Result<Order> checkout(@RequestBody CheckoutRequest request, HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"CONSUMER".equals(role)) {
            return Result.error(403, "Only consumers can checkout");
        }
        Integer consumerId = (Integer) httpRequest.getAttribute("userId");

        if (request.getCartItemIds() == null || request.getCartItemIds().isEmpty()) {
            return Result.error(400, "Cart is empty");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (Integer cartItemId : request.getCartItemIds()) {
            CartItem cartItem = cartItemMapper.selectById(cartItemId);
            if (cartItem == null || !cartItem.getConsumerId().equals(consumerId)) {
                continue;
            }

            Product product = productMapper.selectById(cartItem.getProductId());
            if (product == null || product.getStock() < cartItem.getQuantity()) {
                return Result.error(400, "Insufficient stock for product: " + (product != null ? product.getName() : cartItem.getProductId()));
            }

            product.setStock(product.getStock() - cartItem.getQuantity());
            productMapper.updateById(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItems.add(orderItem);

            totalAmount = totalAmount.add(product.getPrice().multiply(new BigDecimal(cartItem.getQuantity())));

            cartItemMapper.deleteById(cartItemId);
        }

        Order order = new Order();
        order.setConsumerId(consumerId);
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING_PAYMENT");
        order.setShippingAddress(request.getShippingAddress());
        orderMapper.insert(order);

        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        return Result.success(order);
    }

    @GetMapping("/my")
    public Result<List<Order>> getMyOrders(HttpServletRequest request) {
        Integer consumerId = (Integer) request.getAttribute("userId");
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("consumer_id", consumerId).orderByDesc("created_at");
        List<Order> orders = orderMapper.selectList(wrapper);

        for (Order order : orders) {
            QueryWrapper<OrderItem> itemWrapper = new QueryWrapper<>();
            itemWrapper.eq("order_id", order.getId());
            List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
            for (OrderItem item : items) {
                item.setProduct(productMapper.selectById(item.getProductId()));
            }
            order.setItems(items);
        }

        return Result.success(orders);
    }

    @PostMapping("/{id}/pay")
    public Result<Void> payOrder(@PathVariable Integer id, HttpServletRequest request) {
        Integer consumerId = (Integer) request.getAttribute("userId");
        Order order = orderMapper.selectById(id);
        if (order == null || !order.getConsumerId().equals(consumerId)) {
            return Result.error(404, "Order not found");
        }
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            return Result.error(400, "Order is not pending payment");
        }

        order.setStatus("PAID");
        orderMapper.updateById(order);
        return Result.success();
    }

    @GetMapping("/farmer")
    public Result<List<OrderItem>> getFarmerOrders(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"FARMER".equals(role)) {
            return Result.error(403, "Access denied");
        }
        Integer farmerId = (Integer) request.getAttribute("userId");

        QueryWrapper<Product> productWrapper = new QueryWrapper<>();
        productWrapper.eq("farmer_id", farmerId);
        List<Product> products = productMapper.selectList(productWrapper);
        List<Integer> productIds = products.stream().map(Product::getId).toList();

        if (productIds.isEmpty()) return Result.success(new ArrayList<>());

        QueryWrapper<OrderItem> itemWrapper = new QueryWrapper<>();
        itemWrapper.in("product_id", productIds);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

        for (OrderItem item : items) {
            item.setProduct(productMapper.selectById(item.getProductId()));
        }

        return Result.success(items);
    }

    // Consumer or Admin: Delete an order
    @DeleteMapping("/{id}")
    @Transactional
    public Result<Void> deleteOrder(@PathVariable Integer id, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        Integer userId = (Integer) request.getAttribute("userId");

        Order order = orderMapper.selectById(id);
        if (order == null) {
            return Result.error(404, "Order not found");
        }

        if ("ADMIN".equals(role) || ("CONSUMER".equals(role) && order.getConsumerId().equals(userId))) {
            // Delete order items first if DB doesn't cascade, but since DB cascades, deleting order is enough.
            orderMapper.deleteById(id);
            return Result.success();
        } else {
            return Result.error(403, "Access denied. Cannot delete this order.");
        }
    }
}
