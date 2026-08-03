package com.sdlcplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(max = 200, message = "Project name must not exceed 200 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    /** One of LOW, MEDIUM, HIGH, CRITICAL. Defaults to MEDIUM if omitted. */
    private String priority;

    private LocalDate startDate;

    private LocalDate endDate;

    /** Optional — can be assigned later via PATCH /{id}/manager. */
    private java.util.UUID managerId;
}