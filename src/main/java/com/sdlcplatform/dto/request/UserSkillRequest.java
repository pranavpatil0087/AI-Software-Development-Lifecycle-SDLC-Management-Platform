package com.sdlcplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSkillRequest {

    @NotBlank(message = "Skill name is required")
    private String skillName;

    private String proficiency;
}