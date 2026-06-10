package com.ronin.llm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneratedImageRepository extends JpaRepository<GeneratedImageEntity, Long> {
    @Query("SELECT gi FROM GeneratedImageEntity gi WHERE gi.project.id = :projectId ORDER BY gi.id DESC")
    List<GeneratedImageEntity> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    
    @Query("SELECT gi FROM GeneratedImageEntity gi WHERE gi.user.id = :userId ORDER BY gi.id DESC")
    List<GeneratedImageEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}
