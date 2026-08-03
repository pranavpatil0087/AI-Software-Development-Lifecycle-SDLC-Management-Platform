package com.sdlcplatform.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddProjectMemberRequest {

    @NotNull(message = "userId is required")
    private UUID userId;

    /** One of LEAD, CONTRIBUTOR. Defaults to CONTRIBUTOR if omitted. */
    private String roleInProject;
}