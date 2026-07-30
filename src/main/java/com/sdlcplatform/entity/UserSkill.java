package com.sdlcplatform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, of = {"user", "skillName"})
public class UserSkill extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "skill_name", nullable = false, length = 100)
    private String skillName;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String proficiency = Proficiency.INTERMEDIATE.name();

    public enum Proficiency {
        BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    }
}