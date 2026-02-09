package com.redditclone.controller;

import com.redditclone.dto.VoteDto;
import com.redditclone.service.VoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/votes")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    @PostMapping
    public ResponseEntity<VoteDto.Response> vote(@Valid @RequestBody VoteDto.Request request) {
        return ResponseEntity.ok(voteService.vote(request));
    }
}
