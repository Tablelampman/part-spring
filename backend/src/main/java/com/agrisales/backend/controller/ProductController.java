package com.agrisales.backend.controller;

import com.agrisales.backend.dto.Result;
import com.agrisales.backend.entity.Product;
import com.agrisales.backend.mapper.ProductMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductMapper productMapper;

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

            // Return relative URL path
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

    // Admin: Get all pending products
    @GetMapping("/pending")
    public Result<List<Product>> getPendingProducts(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "Access denied");
        }
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PENDING");
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

    // Public/All: Get all approved products
    @GetMapping("/public")
    public Result<List<Product>> getApprovedProducts() {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "APPROVED");
        return Result.success(productMapper.selectList(wrapper));
    }

    // Farmer: Get my products
    @GetMapping("/my")
    public Result<List<Product>> getMyProducts(HttpServletRequest request) {
        Integer farmerId = (Integer) request.getAttribute("userId");
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("farmer_id", farmerId);
        return Result.success(productMapper.selectList(wrapper));
    }
}
