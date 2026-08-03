package com.sdlcplatform.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProjectRequest {

    @Size(max = 200, message = "Project name must not exceed 200 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    /** One of PLANNED, ACTIVE, ON_HOLD, COMPLETED, CANCELLED. */
    private String status;

    /** One of LOW, MEDIUM, HIGH, CRITICAL. */
    private String priority;

    private LocalDate startDate;

    private LocalDate endDate;
}