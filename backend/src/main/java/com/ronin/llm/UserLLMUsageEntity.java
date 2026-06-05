package com.ronin.llm;

import com.ronin.common.BaseEntity;
import com.ronin.users.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "user_llm_usage")
@Getter
@Setter
public class UserLLMUsageEntity extends BaseEntity {

    @ManyToOne
    private UserEntity user;

    @ManyToOne
    private LLMProviderEntity provider;

    private LocalDate date;

    private int tokensUsed;

    private boolean limitReached;

public Long getId() { return id; }
}
