package com.ronin.llm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneratedImageRepository extends JpaRepository<GeneratedImageEntity, Long> {
    List<GeneratedImageEntity> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    List<GeneratedImageEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}
