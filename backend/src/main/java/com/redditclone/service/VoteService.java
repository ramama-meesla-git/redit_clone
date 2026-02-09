package com.redditclone.service;

import com.redditclone.dto.VoteDto;
import com.redditclone.exception.BadRequestException;
import com.redditclone.exception.ResourceNotFoundException;
import com.redditclone.model.*;
import com.redditclone.repository.CommentRepository;
import com.redditclone.repository.PostRepository;
import com.redditclone.repository.VoteRepository;
import com.redditclone.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final CurrentUser currentUser;

    @Transactional
    public VoteDto.Response vote(VoteDto.Request request) {
        User user = currentUser.getUser();

        if (request.getPostId() != null) {
            return voteOnPost(user, request.getPostId(), request.getVoteType());
        } else if (request.getCommentId() != null) {
            return voteOnComment(user, request.getCommentId(), request.getVoteType());
        } else {
            throw new BadRequestException("Either postId or commentId must be provided");
        }
    }

    private VoteDto.Response voteOnPost(User user, Long postId, Short voteType) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        Optional<Vote> existingVote = voteRepository.findByUserIdAndPostId(user.getId(), postId);

        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();
            if (vote.getVoteType().equals(voteType)) {
                // Remove vote (toggle off)
                voteRepository.delete(vote);
                post.setVoteCount(post.getVoteCount() - voteType);
                userService.updateKarma(post.getAuthor().getId(), -voteType);
                postRepository.save(post);
                return VoteDto.Response.builder()
                        .entityId(postId)
                        .entityType("POST")
                        .voteCount(post.getVoteCount())
                        .userVote((short) 0)
                        .build();
            } else {
                // Change vote direction
                short oldVote = vote.getVoteType();
                vote.setVoteType(voteType);
                voteRepository.save(vote);
                post.setVoteCount(post.getVoteCount() - oldVote + voteType);
                userService.updateKarma(post.getAuthor().getId(), -oldVote + voteType);
                postRepository.save(post);
                return VoteDto.Response.builder()
                        .entityId(postId)
                        .entityType("POST")
                        .voteCount(post.getVoteCount())
                        .userVote(voteType)
                        .build();
            }
        } else {
            // New vote
            Vote vote = Vote.builder()
                    .voteType(voteType)
                    .user(user)
                    .post(post)
                    .build();
            voteRepository.save(vote);
            post.setVoteCount(post.getVoteCount() + voteType);
            userService.updateKarma(post.getAuthor().getId(), voteType);
            postRepository.save(post);
            return VoteDto.Response.builder()
                    .entityId(postId)
                    .entityType("POST")
                    .voteCount(post.getVoteCount())
                    .userVote(voteType)
                    .build();
        }
    }

    private VoteDto.Response voteOnComment(User user, Long commentId, Short voteType) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        Optional<Vote> existingVote = voteRepository.findByUserIdAndCommentId(user.getId(), commentId);

        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();
            if (vote.getVoteType().equals(voteType)) {
                voteRepository.delete(vote);
                comment.setVoteCount(comment.getVoteCount() - voteType);
                userService.updateKarma(comment.getAuthor().getId(), -voteType);
                commentRepository.save(comment);
                return VoteDto.Response.builder()
                        .entityId(commentId)
                        .entityType("COMMENT")
                        .voteCount(comment.getVoteCount())
                        .userVote((short) 0)
                        .build();
            } else {
                short oldVote = vote.getVoteType();
                vote.setVoteType(voteType);
                voteRepository.save(vote);
                comment.setVoteCount(comment.getVoteCount() - oldVote + voteType);
                userService.updateKarma(comment.getAuthor().getId(), -oldVote + voteType);
                commentRepository.save(comment);
                return VoteDto.Response.builder()
                        .entityId(commentId)
                        .entityType("COMMENT")
                        .voteCount(comment.getVoteCount())
                        .userVote(voteType)
                        .build();
            }
        } else {
            Vote vote = Vote.builder()
                    .voteType(voteType)
                    .user(user)
                    .comment(comment)
                    .build();
            voteRepository.save(vote);
            comment.setVoteCount(comment.getVoteCount() + voteType);
            userService.updateKarma(comment.getAuthor().getId(), voteType);
            commentRepository.save(comment);
            return VoteDto.Response.builder()
                    .entityId(commentId)
                    .entityType("COMMENT")
                    .voteCount(comment.getVoteCount())
                    .userVote(voteType)
                    .build();
        }
    }
}
