package com.redditclone.repository;

import com.redditclone.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdAndParentIsNullOrderByCreatedAtDesc(Long postId);

    Page<Comment> findByAuthorIdAndIsDeletedFalse(Long authorId, Pageable pageable);

    long countByPostId(Long postId);
}
