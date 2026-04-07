package com.agrisales.backend.mapper;

import com.agrisales.backend.dto.FarmerStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface StatsMapper {

    @Select("SELECT SUM(total_amount) FROM orders WHERE status != 'PENDING_PAYMENT' AND DATE(created_at AT TIME ZONE 'UTC') = CURRENT_DATE")
    BigDecimal getTodayRevenue();

    @Select("SELECT COALESCE(SUM(oi.quantity * oi.unit_price), 0) FROM order_items oi JOIN orders o ON oi.order_id = o.id JOIN products p ON oi.product_id = p.id WHERE p.farmer_id = #{farmerId} AND o.status != 'PENDING_PAYMENT'")
    BigDecimal getFarmerTotalRevenue(@Param("farmerId") Integer farmerId);

    @Select("SELECT COALESCE(SUM(oi.quantity), 0) FROM order_items oi JOIN orders o ON oi.order_id = o.id JOIN products p ON oi.product_id = p.id WHERE p.farmer_id = #{farmerId} AND o.status != 'PENDING_PAYMENT'")
    Long getFarmerTotalSalesQuantity(@Param("farmerId") Integer farmerId);

    @Select("SELECT TO_CHAR(o.created_at AT TIME ZONE 'UTC', 'YYYY-MM-DD') as date, " +
            "COALESCE(SUM(oi.quantity), 0) as quantity, " +
            "COALESCE(SUM(oi.quantity * oi.unit_price), 0) as revenue " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.id " +
            "JOIN products p ON oi.product_id = p.id " +
            "WHERE p.farmer_id = #{farmerId} " +
            "AND o.status != 'PENDING_PAYMENT' " +
            "AND o.created_at >= (CURRENT_DATE - INTERVAL '6 days') " +
            "GROUP BY TO_CHAR(o.created_at AT TIME ZONE 'UTC', 'YYYY-MM-DD') " +
            "ORDER BY date ASC")
    List<FarmerStats.DailySales> getFarmerRecentSales(@Param("farmerId") Integer farmerId);
}
