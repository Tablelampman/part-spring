package com.agrisales.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer consumerId;
    private BigDecimal totalAmount;
    private String status; // PENDING_PAYMENT, PAID, SHIPPED, COMPLETED, CANCELLED
    private String shippingAddress;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @TableField(exist = false)
    private List<OrderItem> items;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getConsumerId() { return consumerId; }
    public void setConsumerId(Integer consumerId) { this.consumerId = consumerId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}
