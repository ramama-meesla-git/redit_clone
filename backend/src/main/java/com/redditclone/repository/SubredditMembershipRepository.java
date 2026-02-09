package com.redditclone.repository;

import com.redditclone.model.SubredditMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubredditMembershipRepository extends JpaRepository<SubredditMembership, Long> {
    Optional<SubredditMembership> findByUserIdAndSubredditId(Long userId, Long subredditId);

    List<SubredditMembership> findByUserId(Long userId);

    List<SubredditMembership> findBySubredditId(Long subredditId);

    Boolean existsByUserIdAndSubredditId(Long userId, Long subredditId);

    void deleteByUserIdAndSubredditId(Long userId, Long subredditId);
}
