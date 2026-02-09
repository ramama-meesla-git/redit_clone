package com.redditclone.service;

import com.redditclone.dto.CommentDto;
import com.redditclone.exception.BadRequestException;
import com.redditclone.exception.ResourceNotFoundException;
import com.redditclone.model.Comment;
import com.redditclone.model.Post;
import com.redditclone.model.User;
import com.redditclone.model.Vote;
import com.redditclone.repository.CommentRepository;
import com.redditclone.repository.PostRepository;
import com.redditclone.repository.VoteRepository;
import com.redditclone.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final VoteRepository voteRepository;
    private final CurrentUser currentUser;

    @Transactional
    public CommentDto.Response create(Long postId, CommentDto.CreateRequest request) {
        User author = currentUser.getUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        Comment parent = null;
        int depth = 0;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", request.getParentId()));
            depth = parent.getDepth() + 1;
        }

        Comment comment = Comment.builder()
                .content(request.getContent())
                .post(post)
                .author(author)
                .parent(parent)
                .depth(depth)
                .build();

        comment = commentRepository.save(comment);

        // Update post comment count
        post.setCommentCount((int) commentRepository.countByPostId(postId));
        postRepository.save(post);

        return mapToResponse(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentDto.Response> getByPostId(Long postId) {
        List<Comment> topLevelComments = commentRepository.findByPostIdAndParentIsNullOrderByCreatedAtDesc(postId);
        return topLevelComments.stream()
                .map(this::mapToResponseWithChildren)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<CommentDto.Response> getByAuthor(Long authorId, Pageable pageable) {
        return commentRepository.findByAuthorIdAndIsDeletedFalse(authorId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public CommentDto.Response update(Long commentId, CommentDto.UpdateRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        User user = currentUser.getUser();
        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new BadRequestException("You can only edit your own comments");
        }

        comment.setContent(request.getContent());
        comment = commentRepository.save(comment);
        return mapToResponse(comment);
    }

    @Transactional
    public void delete(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        User user = currentUser.getUser();
        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new BadRequestException("You can only delete your own comments");
        }

        comment.setIsDeleted(true);
        comment.setContent("[deleted]");
        commentRepository.save(comment);
    }

    private CommentDto.Response mapToResponseWithChildren(Comment comment) {
        CommentDto.Response response = mapToResponse(comment);
        if (comment.getChildren() != null && !comment.getChildren().isEmpty()) {
            response.setChildren(
                    comment.getChildren().stream()
                            .map(this::mapToResponseWithChildren)
                            .collect(Collectors.toList()));
        }
        return response;
    }

    private CommentDto.Response mapToResponse(Comment comment) {
        Integer userVote = 0;
        Long userId = currentUser.getUserId();
        if (userId != null) {
            userVote = voteRepository.findByUserIdAndCommentId(userId, comment.getId())
                    .map(Vote::getVoteType)
                    .map(Short::intValue)
                    .orElse(0);
        }

        return CommentDto.Response.builder()
                .id(comment.getId())
                .content(comment.getIsDeleted() ? "[deleted]" : comment.getContent())
                .voteCount(comment.getVoteCount())
                .depth(comment.getDepth())
                .authorUsername(comment.getIsDeleted() ? "[deleted]" : comment.getAuthor().getUsername())
                .authorId(comment.getIsDeleted() ? null : comment.getAuthor().getId())
                .authorAvatarUrl(comment.getIsDeleted() ? null : comment.getAuthor().getAvatarUrl())
                .postId(comment.getPost().getId())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .userVote(userVote)
                .createdAt(comment.getCreatedAt() != null ? comment.getCreatedAt().toString() : null)
                .updatedAt(comment.getUpdatedAt() != null ? comment.getUpdatedAt().toString() : null)
                .build();
    }
}
