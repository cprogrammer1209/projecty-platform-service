-- Platform administrators: separate identities from customer users (app_user).
CREATE TABLE admin_user (
    id                    UUID PRIMARY KEY,
    email                 VARCHAR(320) NOT NULL,
    full_name             VARCHAR(200) NOT NULL,
    password_hash         VARCHAR(255) NOT NULL,
    status                VARCHAR(20)  NOT NULL,
    failed_login_count    INT          NOT NULL DEFAULT 0,
    first_failed_login_at TIMESTAMPTZ,
    locked_until          TIMESTAMPTZ,
    last_login_at         TIMESTAMPTZ,
    created_at            TIMESTAMPTZ  NOT NULL,
    updated_at            TIMESTAMPTZ  NOT NULL
);
CREATE UNIQUE INDEX ux_admin_user_email ON admin_user (lower(email));

-- An admin console login. The refresh token rotates in place: only the current hash is kept.
CREATE TABLE admin_session (
    id                  UUID PRIMARY KEY,
    admin_user_id       UUID        NOT NULL REFERENCES admin_user (id),
    refresh_token_hash  VARCHAR(64) NOT NULL UNIQUE,
    user_agent          VARCHAR(512),
    ip                  VARCHAR(64),
    created_at          TIMESTAMPTZ NOT NULL,
    last_seen_at        TIMESTAMPTZ NOT NULL,
    idle_expires_at     TIMESTAMPTZ NOT NULL,
    absolute_expires_at TIMESTAMPTZ NOT NULL,
    revoked_at          TIMESTAMPTZ
);
CREATE INDEX ix_admin_session_admin ON admin_session (admin_user_id);

-- A support "sign in as" request. The one-time launch code is stored as a hash.
CREATE TABLE impersonation (
    id               UUID PRIMARY KEY,
    admin_user_id    UUID          NOT NULL REFERENCES admin_user (id),
    portal_id        UUID          NOT NULL REFERENCES portal (id),
    target_user_id   UUID          NOT NULL REFERENCES app_user (id),
    reason           VARCHAR(50)   NOT NULL,
    comment          VARCHAR(1000) NOT NULL,
    duration_minutes INT           NOT NULL,
    code_hash        VARCHAR(64)   NOT NULL UNIQUE,
    code_expires_at  TIMESTAMPTZ   NOT NULL,
    redeemed_at      TIMESTAMPTZ,
    expires_at       TIMESTAMPTZ   NOT NULL,
    ended_at         TIMESTAMPTZ,
    created_at       TIMESTAMPTZ   NOT NULL
);
CREATE INDEX ix_impersonation_portal ON impersonation (portal_id);

-- A customer session opened through impersonation carries who is really acting.
ALTER TABLE auth_session ADD COLUMN impersonation_id UUID REFERENCES impersonation (id);

-- Append-only record of security and administrative actions.
CREATE TABLE audit_event (
    id             UUID PRIMARY KEY,
    occurred_at    TIMESTAMPTZ   NOT NULL,
    category       VARCHAR(20)   NOT NULL,
    action         VARCHAR(50)   NOT NULL,
    actor_type     VARCHAR(20)   NOT NULL,
    actor_id       UUID          NOT NULL,
    actor_name     VARCHAR(200)  NOT NULL,
    portal_id      UUID,
    target_user_id UUID,
    target_name    VARCHAR(200),
    detail         VARCHAR(2000)
);
CREATE INDEX ix_audit_event_occurred ON audit_event (occurred_at DESC);
CREATE INDEX ix_audit_event_portal ON audit_event (portal_id, occurred_at DESC);
