package com.ronin.llm;

import com.ronin.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "llm_providers")
@Getter
@Setter
public class LLMProviderEntity extends BaseEntity {

    private String name;

    @Enumerated(EnumType.STRING)
    private LLMCategory category;

    private int priority;

    private int dailyFreeTokensLimit;

    private boolean active;

public Long getId() { return id; }
}
