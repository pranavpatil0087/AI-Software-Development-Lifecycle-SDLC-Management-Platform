package com.sdlcplatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponse {
    private UUID id;
    private String name;
    private String description;
    private String status;
    private String priority;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean archived;
    private UUID managerId;
    private String managerName;
    private long memberCount;
    private Instant createdAt;
}