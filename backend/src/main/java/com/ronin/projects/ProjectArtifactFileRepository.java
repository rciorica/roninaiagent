package com.ronin.projects;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProjectArtifactFileRepository extends JpaRepository<ProjectArtifactFileEntity, Long> {
    List<ProjectArtifactFileEntity> findByProject_IdOrderByFilePathAsc(Long projectId);
    Optional<ProjectArtifactFileEntity> findByProject_IdAndFilePath(Long projectId, String filePath);
}
