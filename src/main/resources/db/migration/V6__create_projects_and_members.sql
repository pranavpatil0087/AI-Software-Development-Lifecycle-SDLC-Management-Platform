CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED'
        CHECK (status IN ('PLANNED', 'ACTIVE', 'ON_HOLD', 'COMPLETED', 'CANCELLED')),
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM'
        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    start_date DATE,
    end_date DATE,
    is_archived BOOLEAN NOT NULL DEFAULT false,
    manager_id UUID REFERENCES users (id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_projects_status ON projects (status);
CREATE INDEX idx_projects_manager_id ON projects (manager_id);

CREATE TABLE project_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_in_project VARCHAR(20) NOT NULL DEFAULT 'CONTRIBUTOR'
        CHECK (role_in_project IN ('LEAD', 'CONTRIBUTOR')),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_project_member UNIQUE (project_id, user_id)
);

CREATE INDEX idx_project_members_project_id ON project_members (project_id);
CREATE INDEX idx_project_members_user_id ON project_members (user_id);