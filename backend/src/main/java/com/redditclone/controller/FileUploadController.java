package com.redditclone.controller;

import com.redditclone.security.CurrentUser;
import com.redditclone.service.FileStorageService;
import com.redditclone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;
    private final UserService userService;
    private final CurrentUser currentUser;

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadAvatar(@RequestPart MultipartFile file) {
        String url = fileStorageService.uploadFile(file, "avatars");
        userService.updateAvatar(currentUser.getUserId(), url);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadImage(@RequestPart MultipartFile file) {
        String url = fileStorageService.uploadFile(file, "images");
        return ResponseEntity.ok(Map.of("url", url));
    }
}
