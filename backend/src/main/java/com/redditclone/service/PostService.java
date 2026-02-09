package com.redditclone.service;

import com.redditclone.dto.PostDto;
import com.redditclone.exception.BadRequestException;
import com.redditclone.exception.ResourceNotFoundException;
import com.redditclone.model.Post;
import com.redditclone.model.Subreddit;
import com.redditclone.model.User;
import com.redditclone.model.Vote;
import com.redditclone.repository.PostRepository;
import com.redditclone.repository.SubredditRepository;
import com.redditclone.repository.VoteRepository;
import com.redditclone.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final SubredditRepository subredditRepository;
    private final VoteRepository voteRepository;
    private final CurrentUser currentUser;

    @Transactional
    public PostDto.Response create(PostDto.CreateRequest request) {
        User author = currentUser.getUser();
        Subreddit subreddit = subredditRepository.findById(request.getSubredditId())
                .orElseThrow(() -> new ResourceNotFoundException("Subreddit", "id", request.getSubredditId()));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .url(request.getUrl())
                .imageUrl(request.getImageUrl())
                .postType(request.getPostType())
                .subreddit(subreddit)
                .author(author)
                .build();

        post = postRepository.save(post);
        return mapToResponse(post);
    }

    @Transactional(readOnly = true)
    public PostDto.Response getById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
        if (post.getIsDeleted()) {
            throw new ResourceNotFoundException("Post", "id", id);
        }
        return mapToResponse(post);
    }

    @Transactional(readOnly = true)
    public Page<PostDto.Response> getFeed(String sort, Pageable pageable) {
        Page<Post> posts = switch (sort != null ? sort.toLowerCase() : "hot") {
            case "new" -> postRepository.findNewPosts(pageable);
            case "top" -> postRepository.findTopPosts(pageable);
            default -> postRepository.findHotPosts(pageable);
        };
        return posts.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<PostDto.Response> getBySubreddit(Long subredditId, String sort, Pageable pageable) {
        Page<Post> posts = switch (sort != null ? sort.toLowerCase() : "hot") {
            case "new" -> postRepository.findNewPostsBySubreddit(subredditId, pageable);
            case "top" -> postRepository.findTopPostsBySubreddit(subredditId, pageable);
            default -> postRepository.findHotPostsBySubreddit(subredditId, pageable);
        };
        return posts.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<PostDto.Response> getByAuthor(Long authorId, Pageable pageable) {
        return postRepository.findByAuthorIdAndIsDeletedFalse(authorId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<PostDto.Response> search(String query, Pageable pageable) {
        return postRepository.searchPosts(query, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public PostDto.Response update(Long id, PostDto.UpdateRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));

        User user = currentUser.getUser();
        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new BadRequestException("You can only edit your own posts");
        }

        if (request.getContent() != null)
            post.setContent(request.getContent());
        if (request.getUrl() != null)
            post.setUrl(request.getUrl());

        post = postRepository.save(post);
        return mapToResponse(post);
    }

    @Transactional
    public void delete(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));

        User user = currentUser.getUser();
        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new BadRequestException("You can only delete your own posts");
        }

        post.setIsDeleted(true);
        postRepository.save(post);
    }

    private PostDto.Response mapToResponse(Post post) {
        Integer userVote = 0;
        Long userId = currentUser.getUserId();
        if (userId != null) {
            userVote = voteRepository.findByUserIdAndPostId(userId, post.getId())
                    .map(Vote::getVoteType)
                    .map(Short::intValue)
                    .orElse(0);
        }

        return PostDto.Response.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .url(post.getUrl())
                .imageUrl(post.getImageUrl())
                .postType(post.getPostType())
                .voteCount(post.getVoteCount())
                .commentCount(post.getCommentCount())
                .isLocked(post.getIsLocked())
                .subredditName(post.getSubreddit().getName())
                .subredditId(post.getSubreddit().getId())
                .authorUsername(post.getAuthor().getUsername())
                .authorId(post.getAuthor().getId())
                .authorAvatarUrl(post.getAuthor().getAvatarUrl())
                .userVote(userVote)
                .createdAt(post.getCreatedAt() != null ? post.getCreatedAt().toString() : null)
                .updatedAt(post.getUpdatedAt() != null ? post.getUpdatedAt().toString() : null)
                .build();
    }
}
