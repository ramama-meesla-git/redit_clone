package com.redditclone.service;

import com.redditclone.dto.SubredditDto;
import com.redditclone.exception.BadRequestException;
import com.redditclone.exception.ResourceNotFoundException;
import com.redditclone.model.Subreddit;
import com.redditclone.model.SubredditMembership;
import com.redditclone.model.User;
import com.redditclone.repository.SubredditMembershipRepository;
import com.redditclone.repository.SubredditRepository;
import com.redditclone.repository.UserRepository;
import com.redditclone.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubredditService {

    private final SubredditRepository subredditRepository;
    private final SubredditMembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;

    @Transactional
    public SubredditDto.Response create(SubredditDto.CreateRequest request) {
        if (subredditRepository.existsByName(request.getName())) {
            throw new BadRequestException("Subreddit name already exists");
        }

        User creator = currentUser.getUser();
        Subreddit subreddit = Subreddit.builder()
                .name(request.getName())
                .description(request.getDescription())
                .creator(creator)
                .memberCount(1)
                .build();

        subreddit = subredditRepository.save(subreddit);

        // Creator auto-joins as moderator
        SubredditMembership membership = SubredditMembership.builder()
                .user(creator)
                .subreddit(subreddit)
                .role("MODERATOR")
                .build();
        membershipRepository.save(membership);

        return mapToResponse(subreddit, true);
    }

    @Transactional(readOnly = true)
    public SubredditDto.Response getByName(String name) {
        Subreddit subreddit = subredditRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Subreddit", "name", name));
        boolean isMember = checkMembership(subreddit.getId());
        return mapToResponse(subreddit, isMember);
    }

    @Transactional(readOnly = true)
    public SubredditDto.Response getById(Long id) {
        Subreddit subreddit = subredditRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subreddit", "id", id));
        boolean isMember = checkMembership(subreddit.getId());
        return mapToResponse(subreddit, isMember);
    }

    @Transactional(readOnly = true)
    public List<SubredditDto.Response> getTopSubreddits(int limit) {
        return subredditRepository.findTopByMemberCount(PageRequest.of(0, limit)).stream()
                .map(s -> mapToResponse(s, checkMembership(s.getId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<SubredditDto.Response> search(String query, Pageable pageable) {
        return subredditRepository.searchByNameOrDescription(query, pageable)
                .map(s -> mapToResponse(s, checkMembership(s.getId())));
    }

    @Transactional(readOnly = true)
    public List<SubredditDto.Response> getUserSubreddits(Long userId) {
        return membershipRepository.findByUserId(userId).stream()
                .map(m -> mapToResponse(m.getSubreddit(), true))
                .collect(Collectors.toList());
    }

    @Transactional
    public void join(Long subredditId) {
        User user = currentUser.getUser();
        Subreddit subreddit = subredditRepository.findById(subredditId)
                .orElseThrow(() -> new ResourceNotFoundException("Subreddit", "id", subredditId));

        if (membershipRepository.existsByUserIdAndSubredditId(user.getId(), subredditId)) {
            throw new BadRequestException("Already a member");
        }

        SubredditMembership membership = SubredditMembership.builder()
                .user(user)
                .subreddit(subreddit)
                .role("MEMBER")
                .build();
        membershipRepository.save(membership);

        subreddit.setMemberCount(subreddit.getMemberCount() + 1);
        subredditRepository.save(subreddit);
    }

    @Transactional
    public void leave(Long subredditId) {
        User user = currentUser.getUser();
        Subreddit subreddit = subredditRepository.findById(subredditId)
                .orElseThrow(() -> new ResourceNotFoundException("Subreddit", "id", subredditId));

        membershipRepository.deleteByUserIdAndSubredditId(user.getId(), subredditId);
        subreddit.setMemberCount(Math.max(0, subreddit.getMemberCount() - 1));
        subredditRepository.save(subreddit);
    }

    private boolean checkMembership(Long subredditId) {
        Long userId = currentUser.getUserId();
        if (userId == null)
            return false;
        return membershipRepository.existsByUserIdAndSubredditId(userId, subredditId);
    }

    private SubredditDto.Response mapToResponse(Subreddit subreddit, boolean isMember) {
        return SubredditDto.Response.builder()
                .id(subreddit.getId())
                .name(subreddit.getName())
                .description(subreddit.getDescription())
                .bannerUrl(subreddit.getBannerUrl())
                .iconUrl(subreddit.getIconUrl())
                .memberCount(subreddit.getMemberCount())
                .creatorUsername(subreddit.getCreator().getUsername())
                .creatorId(subreddit.getCreator().getId())
                .isMember(isMember)
                .createdAt(subreddit.getCreatedAt() != null ? subreddit.getCreatedAt().toString() : null)
                .build();
    }
}
