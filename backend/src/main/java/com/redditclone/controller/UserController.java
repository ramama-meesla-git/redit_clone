package com.redditclone.controller;

import com.redditclone.dto.AuthDto;
import com.redditclone.dto.CommentDto;
import com.redditclone.dto.PostDto;
import com.redditclone.service.CommentService;
import com.redditclone.service.PostService;
import com.redditclone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;

    @GetMapping("/{username}")
    public ResponseEntity<AuthDto.UserResponse> getUser(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @GetMapping("/{username}/posts")
    public ResponseEntity<Page<PostDto.Response>> getUserPosts(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        AuthDto.UserResponse user = userService.getUserByUsername(username);
        return ResponseEntity.ok(postService.getByAuthor(user.getId(), PageRequest.of(page, size)));
    }

    @GetMapping("/{username}/comments")
    public ResponseEntity<Page<CommentDto.Response>> getUserComments(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        AuthDto.UserResponse user = userService.getUserByUsername(username);
        return ResponseEntity.ok(commentService.getByAuthor(user.getId(), PageRequest.of(page, size)));
    }

    @PutMapping("/profile")
    public ResponseEntity<AuthDto.UserResponse> updateProfile(
            @RequestParam(required = false) String displayName,
            @RequestParam(required = false) String bio,
            @RequestParam Long userId) {
        return ResponseEntity.ok(userService.updateProfile(userId, displayName, bio));
    }
}
