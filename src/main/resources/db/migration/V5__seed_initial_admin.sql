-- Bootstraps the very first ADMIN account. Without this, no user could ever
-- be granted the ADMIN role, since role assignment itself requires ADMIN.
--
-- SECURITY: this default password must be changed immediately after first
-- login in any real environment (use PATCH /api/v1/auth/change-password).
-- Password: ChangeMe@Admin123

DO $$
DECLARE
    admin_role_id UUID;
    new_admin_id UUID;
BEGIN
    SELECT id INTO admin_role_id FROM roles WHERE name = 'ADMIN';

    INSERT INTO users (email, password_hash, full_name, department, is_active, email_verified)
    VALUES (
        'admin@sdlcplatform.com',
        '$2b$12$UTePYfcXCy3yDPJOZlhaJO.huuUP6JY1cli2WqT5vhHm.wRmmRT8e',
        'System Administrator',
        'Platform Administration',
        true,
        true
    )
    RETURNING id INTO new_admin_id;

    INSERT INTO user_roles (user_id, role_id) VALUES (new_admin_id, admin_role_id);
END $$;