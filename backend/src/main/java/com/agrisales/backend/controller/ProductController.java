package com.agrisales.backend.controller;

import com.agrisales.backend.dto.Result;
import com.agrisales.backend.entity.Product;
import com.agrisales.backend.entity.User;
import com.agrisales.backend.mapper.ProductMapper;
import com.agrisales.backend.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostMapping("/upload")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "Please select a file to upload");
        }
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + extension;

            File destDir = new File(uploadDir);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            File destFile = new File(destDir.getAbsolutePath() + File.separator + fileName);
            file.transferTo(destFile);

            return Result.success("/uploads/" + fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(500, "Failed to upload file");
        }
    }

    // Farmer: Add a product
    @PostMapping
    public Result<Void> addProduct(@RequestBody Product product, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"FARMER".equals(role)) {
            return Result.error(403, "Access denied. Only farmers can add products.");
        }

        Integer farmerId = (Integer) request.getAttribute("userId");
        product.setFarmerId(farmerId);
        product.setStatus("PENDING");
        productMapper.insert(product);
        return Result.success();
    }

    // Admin: Get all products with filters
    @GetMapping("/admin/list")
    public Result<List<Product>> getAdminProducts(
            @RequestParam(required = false) String farmerName,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {

        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "Access denied");
        }

        QueryWrapper<Product> wrapper = new QueryWrapper<>();

        if (StringUtils.hasText(name)) {
            wrapper.like("name", name);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq("status", status);
        }

        // Handle farmerName search
        if (StringUtils.hasText(farmerName)) {
            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            userWrapper.like("username", farmerName).eq("role", "FARMER");
            List<User> farmers = userMapper.selectList(userWrapper);
            if (farmers.isEmpty()) {
                return Result.success(List.of()); // No matching farmers
            }
            List<Integer> farmerIds = farmers.stream().map(User::getId).collect(Collectors.toList());
            wrapper.in("farmer_id", farmerIds);
        }

        return Result.success(productMapper.selectList(wrapper));
    }

    // Admin: Approve/Reject product
    @PutMapping("/{id}/status")
    public Result<Void> updateProductStatus(@PathVariable Integer id, @RequestParam String status, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "Access denied");
        }
        if (!status.equals("APPROVED") && !status.equals("REJECTED")) {
            return Result.error(400, "Invalid status");
        }

        Product product = new Product();
        product.setId(id);
        product.setStatus(status);
        productMapper.updateById(product);
        return Result.success();
    }

    // Public/All: Get all approved products with filters
    @GetMapping("/public")
    public Result<List<Product>> getApprovedProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {

        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "APPROVED");

        if (StringUtils.hasText(name)) {
            wrapper.like("name", name);
        }
        if (StringUtils.hasText(category) && !category.equals("All")) {
            wrapper.eq("category", category);
        }
        if (minPrice != null) {
            wrapper.ge("price", minPrice);
        }
        if (maxPrice != null) {
            wrapper.le("price", maxPrice);
        }

        return Result.success(productMapper.selectList(wrapper));
    }

    // Farmer: Get my products with filters
    @GetMapping("/my")
    public Result<List<Product>> getMyProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {

        Integer farmerId = (Integer) request.getAttribute("userId");
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("farmer_id", farmerId);

        if (StringUtils.hasText(name)) {
            wrapper.like("name", name);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq("status", status);
        }

        return Result.success(productMapper.selectList(wrapper));
    }
}
