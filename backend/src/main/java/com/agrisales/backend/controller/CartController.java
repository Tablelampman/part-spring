package com.agrisales.backend.controller;

import com.agrisales.backend.dto.Result;
import com.agrisales.backend.entity.CartItem;
import com.agrisales.backend.entity.Product;
import com.agrisales.backend.mapper.CartItemMapper;
import com.agrisales.backend.mapper.ProductMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartItemMapper cartItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @GetMapping
    public Result<List<CartItem>> getCartItems(HttpServletRequest request) {
        Integer consumerId = (Integer) request.getAttribute("userId");
        QueryWrapper<CartItem> wrapper = new QueryWrapper<>();
        wrapper.eq("consumer_id", consumerId);
        List<CartItem> items = cartItemMapper.selectList(wrapper);

        for (CartItem item : items) {
            item.setProduct(productMapper.selectById(item.getProductId()));
        }
        return Result.success(items);
    }

    @PostMapping
    public Result<Void> addToCart(@RequestBody CartItem cartItem, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"CONSUMER".equals(role)) {
            return Result.error(403, "Only consumers can add to cart");
        }

        Integer consumerId = (Integer) request.getAttribute("userId");

        QueryWrapper<CartItem> wrapper = new QueryWrapper<>();
        wrapper.eq("consumer_id", consumerId).eq("product_id", cartItem.getProductId());
        CartItem existingItem = cartItemMapper.selectOne(wrapper);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + cartItem.getQuantity());
            cartItemMapper.updateById(existingItem);
        } else {
            cartItem.setConsumerId(consumerId);
            cartItemMapper.insert(cartItem);
        }

        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> removeFromCart(@PathVariable Integer id, HttpServletRequest request) {
        Integer consumerId = (Integer) request.getAttribute("userId");
        QueryWrapper<CartItem> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id).eq("consumer_id", consumerId);
        cartItemMapper.delete(wrapper);
        return Result.success();
    }
}
