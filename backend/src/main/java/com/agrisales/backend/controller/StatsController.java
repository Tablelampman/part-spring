package com.agrisales.backend.controller;

import com.agrisales.backend.dto.AdminStats;
import com.agrisales.backend.dto.FarmerStats;
import com.agrisales.backend.dto.Result;
import com.agrisales.backend.entity.Order;
import com.agrisales.backend.entity.Product;
import com.agrisales.backend.entity.User;
import com.agrisales.backend.mapper.OrderMapper;
import com.agrisales.backend.mapper.ProductMapper;
import com.agrisales.backend.mapper.StatsMapper;
import com.agrisales.backend.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private StatsMapper statsMapper;

    @GetMapping("/admin")
    public Result<AdminStats> getAdminStats(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "Access denied");
        }

        AdminStats stats = new AdminStats();
        stats.setTotalUsers(userMapper.selectCount(null));
        stats.setTotalProducts(productMapper.selectCount(null));
        stats.setTotalOrders(orderMapper.selectCount(null));

        BigDecimal todayRevenue = statsMapper.getTodayRevenue();
        stats.setTodayRevenue(todayRevenue != null ? todayRevenue : BigDecimal.ZERO);

        return Result.success(stats);
    }

    @GetMapping("/farmer")
    public Result<FarmerStats> getFarmerStats(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"FARMER".equals(role)) {
            return Result.error(403, "Access denied");
        }

        Integer farmerId = (Integer) request.getAttribute("userId");

        FarmerStats stats = new FarmerStats();
        stats.setTotalRevenue(statsMapper.getFarmerTotalRevenue(farmerId));
        stats.setTotalSalesQuantity(statsMapper.getFarmerTotalSalesQuantity(farmerId));
        stats.setRecentSales(statsMapper.getFarmerRecentSales(farmerId));

        return Result.success(stats);
    }
}
