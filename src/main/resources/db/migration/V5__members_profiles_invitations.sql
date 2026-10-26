-- Slice 4: profiles with permissions, member status, invitations (USR-US-01 to 06).

-- A portal's profiles. The four system profiles are seeded for every portal and can't be deleted.
CREATE TABLE profile (
    id             UUID PRIMARY KEY,
    portal_id      UUID         NOT NULL REFERENCES portal (id),
    profile_key    VARCHAR(30)  NOT NULL,
    name           VARCHAR(60)  NOT NULL,
    description    VARCHAR(300),
    system_profile BOOLEAN      NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL,
    UNIQUE (portal_id, profile_key)
);

CREATE TABLE profile_permission (
    profile_id UUID        NOT NULL REFERENCES profile (id),
    permission VARCHAR(60) NOT NULL,
    PRIMARY KEY (profile_id, permission)
);

-- Seed the system profiles for portals that already exist (new portals get them on creation).
INSERT INTO profile (id, portal_id, profile_key, name, description, system_profile, created_at)
SELECT gen_random_uuid(), p.id, d.profile_key, d.name, d.description, TRUE, now()
FROM portal p
CROSS JOIN (VALUES
    ('ADMIN',    'Admin',    'Everything, including portal settings and users.'),
    ('MANAGER',  'Manager',  'Runs projects. Sees the people in the portal.'),
    ('EMPLOYEE', 'Employee', 'Works on assigned items. Sees the people in the portal.'),
    ('CLIENT',   'Client',   'External user. Sees only what is shared with them.')
) AS d (profile_key, name, description);

INSERT INTO profile_permission (profile_id, permission)
SELECT pr.id, x.permission
FROM profile pr
JOIN (VALUES
    ('ADMIN', 'PORTAL_SETTINGS_MANAGE'), ('ADMIN', 'USERS_VIEW'), ('ADMIN', 'USERS_MANAGE'),
    ('MANAGER', 'USERS_VIEW'),
    ('EMPLOYEE', 'USERS_VIEW')
) AS x (profile_key, permission) ON x.profile_key = pr.profile_key;

-- Members point at a profile; existing owners become Admins (the Owner itself is portal.owner_user_id).
ALTER TABLE portal_member ADD COLUMN profile_id UUID REFERENCES profile (id);
UPDATE portal_member m
SET profile_id = (SELECT pr.id FROM profile pr WHERE pr.portal_id = m.portal_id AND pr.profile_key = 'ADMIN');
ALTER TABLE portal_member ALTER COLUMN profile_id SET NOT NULL;
ALTER TABLE portal_member DROP COLUMN profile;
ALTER TABLE portal_member ADD COLUMN deactivated_at TIMESTAMPTZ;

-- An emailed invitation to join a portal with a profile. Only the token's hash is stored.
CREATE TABLE invitation (
    id                 UUID PRIMARY KEY,
    portal_id          UUID         NOT NULL REFERENCES portal (id),
    email              VARCHAR(320) NOT NULL,
    profile_id         UUID         NOT NULL REFERENCES profile (id),
    invited_by_user_id UUID         NOT NULL REFERENCES app_user (id),
    token_hash         VARCHAR(64)  NOT NULL UNIQUE,
    status             VARCHAR(20)  NOT NULL,
    expires_at         TIMESTAMPTZ  NOT NULL,
    created_at         TIMESTAMPTZ  NOT NULL,
    accepted_at        TIMESTAMPTZ,
    accepted_user_id   UUID REFERENCES app_user (id)
);
CREATE INDEX ix_invitation_portal ON invitation (portal_id, status);
