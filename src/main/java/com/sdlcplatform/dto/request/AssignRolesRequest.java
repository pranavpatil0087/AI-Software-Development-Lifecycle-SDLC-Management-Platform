package com.sdlcplatform.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignRolesRequest {

    @NotEmpty(message = "At least one role must be provided")
    private Set<String> roles;
}