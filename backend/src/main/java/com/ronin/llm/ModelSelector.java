package com.ronin.llm;

import com.ronin.llm.LLMCategory;
import com.ronin.projects.enums.ProjectPhase;
import com.ronin.users.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModelSelector {

    private final LLMProviderRepository providerRepo;
    private final UserLLMUsageRepository usageRepo;

    public ModelSelectionResult select(UserEntity user, ProjectPhase phase) {

        LLMCategory category = mapPhaseToCategory(phase);
        List<LLMProviderEntity> providers = providerRepo.findByCategory(category);
        LocalDate today = LocalDate.now();

        LLMProviderEntity preferred = providers.get(0);
        LLMProviderEntity chosen = null;

        for (LLMProviderEntity provider : providers) {
            UserLLMUsageEntity usage = usageRepo
                    .findByUserIdAndProviderIdAndDate(user.getId(), provider.getId(), today)
                    .orElseGet(() -> initUsage(user, provider, today));

            if (usage.getTokensUsed() < provider.getDailyFreeTokensLimit()) {
                chosen = provider;
                break;
            }
        }

        if (chosen == null) {
            throw new RuntimeException("No free LLM tokens available today");
        }

        boolean switched = !chosen.getId().equals(preferred.getId());
        log.debug("ModelSelector selected provider {} (preferred {}) for user {} in phase {}. switched={}",
                chosen.getName(), preferred.getName(), user.getEmail(), phase, switched);

        return new ModelSelectionResult(chosen, preferred, switched);
    }

    private UserLLMUsageEntity initUsage(UserEntity user, LLMProviderEntity provider, LocalDate date) {
        UserLLMUsageEntity usage = new UserLLMUsageEntity();
        usage.setUser(user);
        usage.setProvider(provider);
        usage.setDate(date);
        usage.setTokensUsed(0);
        usage.setLimitReached(false);
        return usageRepo.save(usage);
    }

    private LLMCategory mapPhaseToCategory(ProjectPhase phase) {
        if (phase == null) {
            return LLMCategory.GENERAL;
        }

        try {
            return LLMCategory.valueOf(phase.name());
        } catch (IllegalArgumentException e) {
            return LLMCategory.GENERAL;
        }
    }
}
