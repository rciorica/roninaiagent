package com.ronin.projects;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectMessageRepository extends JpaRepository<ProjectMessageEntity, Long> {
    List<ProjectMessageEntity> findByProjectIdOrderByCreatedAtAsc(Long projectId);
}
