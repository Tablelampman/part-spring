package com.agrisales.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import java.time.LocalDateTime;

@TableName("cart_items")
public class CartItem {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer consumerId;
    private Integer productId;
    private Integer quantity;
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private Product product;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getConsumerId() { return consumerId; }
    public void setConsumerId(Integer consumerId) { this.consumerId = consumerId; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}
