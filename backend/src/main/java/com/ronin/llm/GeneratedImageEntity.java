package com.ronin.llm;

import com.ronin.common.BaseEntity;
import com.ronin.projects.ProjectEntity;
import com.ronin.users.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "generated_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GeneratedImageEntity extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "project_id")
    private ProjectEntity project;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "prompt", columnDefinition = "TEXT")
    private String prompt;

    @Column(name = "revised_prompt", columnDefinition = "TEXT")
    private String revisedPrompt;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "provider")
    private String provider;

    @Column(name = "model")
    private String model;

    @Column(name = "size")
    private String size;

    @Column(name = "format")
    private String format;

    @Column(name = "generation_time_ms")
    private Long generationTimeMs;
}
