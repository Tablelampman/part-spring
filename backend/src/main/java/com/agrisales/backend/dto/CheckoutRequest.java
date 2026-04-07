package com.agrisales.backend.dto;

import java.util.List;

public class CheckoutRequest {
    private List<Integer> cartItemIds;
    private String shippingAddress;

    public List<Integer> getCartItemIds() { return cartItemIds; }
    public void setCartItemIds(List<Integer> cartItemIds) { this.cartItemIds = cartItemIds; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
}
