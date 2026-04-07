package com.agrisales.backend.controller;

import com.agrisales.backend.dto.Result;
import com.agrisales.backend.entity.User;
import com.agrisales.backend.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    // Admin: List users
    @GetMapping
    public Result<List<User>> getUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role,
            HttpServletRequest request) {

        String userRole = (String) request.getAttribute("role");
        if (!"ADMIN".equals(userRole)) {
            return Result.error(403, "Access denied");
        }

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like("username", username);
        }
        if (StringUtils.hasText(role)) {
            wrapper.eq("role", role);
        }

        // Hide passwords
        List<User> users = userMapper.selectList(wrapper);
        for (User user : users) {
            user.setPassword(null);
        }

        return Result.success(users);
    }

    // Admin: Delete user
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Integer id, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "Access denied");
        }

        Integer adminId = (Integer) request.getAttribute("userId");
        if (adminId.equals(id)) {
            return Result.error(400, "Cannot delete yourself");
        }

        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(404, "User not found");
        }

        userMapper.deleteById(id);
        return Result.success();
    }
}
