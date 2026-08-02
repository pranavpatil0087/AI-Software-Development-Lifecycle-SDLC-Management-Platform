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

import java.time.LocalDate;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, of = "name")
public class Project extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = Status.PLANNED.name();

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String priority = Priority.MEDIUM.name();

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_archived", nullable = false)
    @Builder.Default
    private boolean archived = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    public enum Status {
        PLANNED, ACTIVE, ON_HOLD, COMPLETED, CANCELLED
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}