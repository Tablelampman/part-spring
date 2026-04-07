package com.agrisales.backend.controller;

import com.agrisales.backend.dto.Result;
import com.agrisales.backend.entity.Comment;
import com.agrisales.backend.entity.User;
import com.agrisales.backend.mapper.CommentMapper;
import com.agrisales.backend.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserMapper userMapper;

    // Public/All: Get comments for a product
    @GetMapping("/product/{productId}")
    public Result<List<Comment>> getComments(@PathVariable Integer productId) {
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId).orderByDesc("created_at");

        List<Comment> comments = commentMapper.selectList(wrapper);
        for (Comment comment : comments) {
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                user.setPassword(null); // Don't return password
                comment.setUser(user);
            }
        }
        return Result.success(comments);
    }

    // Authenticated: Post a comment
    @PostMapping
    public Result<Void> addComment(@RequestBody Comment comment, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "Please login first");
        }

        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            return Result.error(400, "Comment cannot be empty");
        }

        comment.setUserId(userId);
        commentMapper.insert(comment);
        return Result.success();
    }

    // Authenticated: Delete a comment (Author or Admin)
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@PathVariable Integer id, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");

        if (userId == null) {
            return Result.error(401, "Please login first");
        }

        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            return Result.error(404, "Comment not found");
        }

        if ("ADMIN".equals(role) || comment.getUserId().equals(userId)) {
            commentMapper.deleteById(id);
            return Result.success();
        } else {
            return Result.error(403, "Access denied. Cannot delete this comment.");
        }
    }
}
