package com.agrisales.backend.controller;

import com.agrisales.backend.dto.LoginRequest;
import com.agrisales.backend.dto.LoginResponse;
import com.agrisales.backend.dto.Result;
import com.agrisales.backend.entity.User;
import com.agrisales.backend.mapper.UserMapper;
import com.agrisales.backend.utils.JwtUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", request.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null || !user.getPassword().equals(request.getPassword())) {
            return Result.error(400, "Invalid username or password");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        return Result.success(new LoginResponse(token, user.getId(), user.getUsername(), user.getRole()));
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody User user) {
        if (user.getUsername() == null || user.getPassword() == null || user.getRole() == null) {
            return Result.error(400, "Username, password and role are required");
        }

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());
        if (userMapper.selectOne(wrapper) != null) {
            return Result.error(400, "Username already exists");
        }

        // Check valid roles
        if (!user.getRole().equals("CONSUMER") && !user.getRole().equals("FARMER") && !user.getRole().equals("ADMIN")) {
            return Result.error(400, "Invalid role");
        }

        userMapper.insert(user);
        return Result.success();
    }
}
