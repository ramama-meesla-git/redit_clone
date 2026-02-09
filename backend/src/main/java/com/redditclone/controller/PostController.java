package com.redditclone.controller;

import com.redditclone.dto.PostDto;
import com.redditclone.service.FileStorageService;
import com.redditclone.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final FileStorageService fileStorageService;

    @PostMapping
    public ResponseEntity<PostDto.Response> create(@Valid @RequestBody PostDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.create(request));
    }

    @PostMapping(value = "/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostDto.Response> createWithImage(
            @RequestParam String title,
            @RequestParam Long subredditId,
            @RequestParam(required = false) String content,
            @RequestPart MultipartFile image) {
        String imageUrl = fileStorageService.uploadFile(image, "posts");
        PostDto.CreateRequest request = PostDto.CreateRequest.builder()
                .title(title)
                .content(content)
                .imageUrl(imageUrl)
                .postType(com.redditclone.model.PostType.IMAGE)
                .subredditId(subredditId)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<PostDto.Response>> getFeed(
            @RequestParam(defaultValue = "hot") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(postService.getFeed(sort, PageRequest.of(page, size)));
    }

    @GetMapping("/subreddit/{subredditId}")
    public ResponseEntity<Page<PostDto.Response>> getBySubreddit(
            @PathVariable Long subredditId,
            @RequestParam(defaultValue = "hot") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(postService.getBySubreddit(subredditId, sort, PageRequest.of(page, size)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDto.Response> update(
            @PathVariable Long id, @Valid @RequestBody PostDto.UpdateRequest request) {
        return ResponseEntity.ok(postService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
