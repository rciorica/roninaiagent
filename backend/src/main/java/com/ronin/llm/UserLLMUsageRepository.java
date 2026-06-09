package com.ronin.llm;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface UserLLMUsageRepository extends JpaRepository<UserLLMUsageEntity, Long> {

    Optional<UserLLMUsageEntity> findByUserIdAndProviderIdAndDate(Long userId, Long providerId, LocalDate date);
}
