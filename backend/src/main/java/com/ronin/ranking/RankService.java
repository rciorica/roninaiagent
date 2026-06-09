package com.ronin.ranking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RankService {

    private final RankRepository rankRepository;

    public RankEntity getRankForCompletedProjects(int completed) {
        return rankRepository
                .findFirstByMinProjectsLessThanEqualAndMaxProjectsGreaterThanEqual(completed, completed)
                .orElseGet(() -> rankRepository.findFirstByMinProjectsGreaterThanOrderByMinProjectsAsc(completed)
                        .orElseThrow(() -> new RuntimeException("No rank found for completed projects: " + completed)));
    }

    public int getProjectsToNextRank(int completed) {
        return rankRepository.findFirstByMinProjectsGreaterThanOrderByMinProjectsAsc(completed)
                .map(nextRank -> Math.max(0, nextRank.getMinProjects() - completed))
                .orElse(0);
    }
}
