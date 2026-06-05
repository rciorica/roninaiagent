package com.ronin.llm;

import com.ronin.llm.LLMCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LLMProviderRepository extends JpaRepository<LLMProviderEntity, Long> {

    @Query("""
        SELECT p FROM LLMProviderEntity p
        WHERE p.active = true
          AND (p.category = :category OR p.category = com.ronin.llm.LLMCategory.GENERAL)
        ORDER BY p.priority ASC
    """)
    List<LLMProviderEntity> findByCategory(@Param("category") LLMCategory category);
}
