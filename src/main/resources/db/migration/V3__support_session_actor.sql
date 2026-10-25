-- A support session names its acting admin itself, so the session module needn't ask the impersonation module.
ALTER TABLE auth_session ADD COLUMN impersonator_admin_id UUID REFERENCES admin_user (id);
ALTER TABLE auth_session ADD COLUMN impersonator_name VARCHAR(200);
