package com.sdlcplatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Deliberately minimal for now: sprint/task/bug counts are placeholders
 * (0) until Phases 4-6 (Sprints, Tasks, Bugs) exist. This endpoint's
 * shape is designed not to change once those are wired in — only the
 * values will start being real.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDashboardResponse {
    private ProjectResponse project;
    private long memberCount;
    private long activeSprintCount;
    private long openTaskCount;
    private long openBugCount;
}