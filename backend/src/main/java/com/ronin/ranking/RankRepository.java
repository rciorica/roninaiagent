package com.ronin.ranking;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RankRepository extends JpaRepository<RankEntity, Long> {
    Optional<RankEntity> findFirstByMinProjectsLessThanEqualAndMaxProjectsGreaterThanEqual(int minProjects, int maxProjects);
    Optional<RankEntity> findFirstByMinProjectsGreaterThanOrderByMinProjectsAsc(int minProjects);
}
