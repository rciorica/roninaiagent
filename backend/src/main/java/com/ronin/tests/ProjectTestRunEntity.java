package com.ronin.tests;

import com.ronin.common.BaseEntity;
import com.ronin.projects.ProjectEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "project_test_runs")
public class ProjectTestRunEntity extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "project_id")
    private ProjectEntity project;

    @Column(length = 50)
    private String status;

    @Lob
    @Column(columnDefinition = "text")
    private String logs;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
