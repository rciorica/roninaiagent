package com.ronin.ranking;

import jakarta.persistence.*;

@Entity
@Table(name = "ranks")
public class RankEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int level;
    private int minProjects;
    private int maxProjects;
    private String beltColor;
    private String meaning;

    public Long getId() { return id; }
    public String getName() { return name; }
    public int getLevel() { return level; }
    public int getMinProjects() { return minProjects; }
    public int getMaxProjects() { return maxProjects; }
    public String getBeltColor() { return beltColor; }
    public String getMeaning() { return meaning; }
}
