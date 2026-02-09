package com.redditclone.repository;

import com.redditclone.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findBySubredditIdAndIsDeletedFalse(Long subredditId, Pageable pageable);

    Page<Post> findByAuthorIdAndIsDeletedFalse(Long authorId, Pageable pageable);

    Page<Post> findByIsDeletedFalse(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isDeleted = false ORDER BY p.voteCount DESC, p.createdAt DESC")
    Page<Post> findHotPosts(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isDeleted = false ORDER BY p.createdAt DESC")
    Page<Post> findNewPosts(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isDeleted = false ORDER BY p.voteCount DESC")
    Page<Post> findTopPosts(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.subreddit.id = :subredditId AND p.isDeleted = false " +
            "ORDER BY p.voteCount DESC, p.createdAt DESC")
    Page<Post> findHotPostsBySubreddit(@Param("subredditId") Long subredditId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.subreddit.id = :subredditId AND p.isDeleted = false " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findNewPostsBySubreddit(@Param("subredditId") Long subredditId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.subreddit.id = :subredditId AND p.isDeleted = false " +
            "ORDER BY p.voteCount DESC")
    Page<Post> findTopPostsBySubreddit(@Param("subredditId") Long subredditId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isDeleted = false AND " +
            "(LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Post> searchPosts(@Param("query") String query, Pageable pageable);
}
