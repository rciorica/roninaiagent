package com.ronin.projects;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProjectMessageAttachmentRepository extends JpaRepository<ProjectMessageAttachmentEntity, Long> {
    Optional<ProjectMessageAttachmentEntity> findByIdAndMessage_Id(Long id, Long messageId);
}
