package com.redditclone.controller;

import com.redditclone.dto.SubredditDto;
import com.redditclone.security.CurrentUser;
import com.redditclone.service.SubredditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subreddits")
@RequiredArgsConstructor
public class SubredditController {

    private final SubredditService subredditService;
    private final CurrentUser currentUser;

    @PostMapping
    public ResponseEntity<SubredditDto.Response> create(@Valid @RequestBody SubredditDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subredditService.create(request));
    }

    @GetMapping("/{name}")
    public ResponseEntity<SubredditDto.Response> getByName(@PathVariable String name) {
        return ResponseEntity.ok(subredditService.getByName(name));
    }

    @GetMapping("/top")
    public ResponseEntity<List<SubredditDto.Response>> getTopSubreddits(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(subredditService.getTopSubreddits(limit));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<SubredditDto.Response>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(subredditService.search(q, PageRequest.of(page, size)));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<SubredditDto.Response>> getMySubreddits() {
        Long userId = currentUser.getUserId();
        return ResponseEntity.ok(subredditService.getUserSubreddits(userId));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<Void> join(@PathVariable Long id) {
        subredditService.join(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<Void> leave(@PathVariable Long id) {
        subredditService.leave(id);
        return ResponseEntity.ok().build();
    }
}
