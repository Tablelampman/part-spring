package com.agrisales.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public class FarmerStats {
    private BigDecimal totalRevenue;
    private Long totalSalesQuantity;
    private List<DailySales> recentSales;

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    public Long getTotalSalesQuantity() { return totalSalesQuantity; }
    public void setTotalSalesQuantity(Long totalSalesQuantity) { this.totalSalesQuantity = totalSalesQuantity; }
    public List<DailySales> getRecentSales() { return recentSales; }
    public void setRecentSales(List<DailySales> recentSales) { this.recentSales = recentSales; }

    public static class DailySales {
        private String date;
        private Long quantity;
        private BigDecimal revenue;

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public Long getQuantity() { return quantity; }
        public void setQuantity(Long quantity) { this.quantity = quantity; }
        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
    }
}
