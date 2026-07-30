package com.sdlcplatform.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(max = 150, message = "Full name must not exceed 150 characters")
    private String fullName;

    private String department;

    private String jobTitle;
}