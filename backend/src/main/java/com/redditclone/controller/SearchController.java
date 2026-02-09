package com.redditclone.controller;

import com.redditclone.dto.PostDto;
import com.redditclone.dto.SubredditDto;
import com.redditclone.service.PostService;
import com.redditclone.service.SubredditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final PostService postService;
    private final SubredditService subredditService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Map<String, Object> results = new HashMap<>();

        if ("all".equals(type) || "posts".equals(type)) {
            Page<PostDto.Response> posts = postService.search(q, PageRequest.of(page, size));
            results.put("posts", posts);
        }

        if ("all".equals(type) || "communities".equals(type)) {
            Page<SubredditDto.Response> subreddits = subredditService.search(q, PageRequest.of(page, size));
            results.put("communities", subreddits);
        }

        return ResponseEntity.ok(results);
    }
}
