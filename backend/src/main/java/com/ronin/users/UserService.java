package com.ronin.users;

import com.ronin.auth.CurrentUserService;
import com.ronin.ranking.RankEntity;
import com.ronin.ranking.RankService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RankService rankService;
    private final CurrentUserService currentUserService;

    public UserEntity getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public RankEntity getRankForUser(Long id) {
        UserEntity user = getById(id);
        return rankService.getRankForCompletedProjects(user.getCompletedProjects());
    }

    public UserProfileResponse getCurrentUserProfile() {
        UserEntity user = currentUserService.get();
        RankEntity rank = rankService.getRankForCompletedProjects(user.getCompletedProjects());
        int projectsToNextRank = rankService.getProjectsToNextRank(user.getCompletedProjects());
        return new UserProfileResponse(user.getId(), user.getEmail(), user.getCompletedProjects(), rank, projectsToNextRank);
    }

    public RankEntity getCurrentUserRank() {
        UserEntity user = currentUserService.get();
        return rankService.getRankForCompletedProjects(user.getCompletedProjects());
    }
}
