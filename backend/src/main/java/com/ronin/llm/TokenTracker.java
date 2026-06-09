package com.ronin.llm;

import com.ronin.users.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TokenTracker {

    private final UserLLMUsageRepository usageRepo;

    public void recordUsage(UserEntity user, LLMProviderEntity provider, int tokensUsedNow) {
        var today = LocalDate.now();

        UserLLMUsageEntity usage = usageRepo
                .findByUserIdAndProviderIdAndDate(user.getId(), provider.getId(), today)
                .orElseGet(() -> {
                    UserLLMUsageEntity u = new UserLLMUsageEntity();
                    u.setUser(user);
                    u.setProvider(provider);
                    u.setDate(today);
                    u.setTokensUsed(0);
                    u.setLimitReached(false);
                    return u;
                });

        int newTotal = usage.getTokensUsed() + tokensUsedNow;
        usage.setTokensUsed(newTotal);
        if (newTotal >= provider.getDailyFreeTokensLimit()) {
            usage.setLimitReached(true);
        }

        usageRepo.save(usage);
    }
}
