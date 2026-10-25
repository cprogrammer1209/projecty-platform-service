-- Identity: one row per person, shared across portals.
CREATE TABLE app_user (
    id                    UUID PRIMARY KEY,
    email                 VARCHAR(320) NOT NULL,
    full_name             VARCHAR(200) NOT NULL,
    password_hash         VARCHAR(255) NOT NULL,
    email_verified_at     TIMESTAMPTZ,
    status                VARCHAR(20)  NOT NULL,
    failed_login_count    INT          NOT NULL DEFAULT 0,
    first_failed_login_at TIMESTAMPTZ,
    locked_until          TIMESTAMPTZ,
    last_login_at         TIMESTAMPTZ,
    created_at            TIMESTAMPTZ  NOT NULL,
    updated_at            TIMESTAMPTZ  NOT NULL
);
CREATE UNIQUE INDEX ux_app_user_email ON app_user (lower(email));

-- A login on one device. Refresh tokens rotate within a session.
CREATE TABLE auth_session (
    id                  UUID PRIMARY KEY,
    user_id             UUID        NOT NULL REFERENCES app_user (id),
    user_agent          VARCHAR(512),
    ip                  VARCHAR(64),
    created_at          TIMESTAMPTZ NOT NULL,
    last_seen_at        TIMESTAMPTZ NOT NULL,
    idle_expires_at     TIMESTAMPTZ NOT NULL,
    absolute_expires_at TIMESTAMPTZ NOT NULL,
    revoked_at          TIMESTAMPTZ
);
CREATE INDEX ix_auth_session_user ON auth_session (user_id);

-- Only the SHA-256 hash of a refresh token is stored.
CREATE TABLE refresh_token (
    id         UUID PRIMARY KEY,
    session_id UUID        NOT NULL REFERENCES auth_session (id),
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL,
    used_at    TIMESTAMPTZ
);
CREATE INDEX ix_refresh_token_session ON refresh_token (session_id);

-- Single-use links sent by email (verify email, reset password).
CREATE TABLE email_token (
    id         UUID PRIMARY KEY,
    user_id    UUID        NOT NULL REFERENCES app_user (id),
    purpose    VARCHAR(30) NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at    TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX ix_email_token_user ON email_token (user_id, purpose);

-- RSA keys used to sign access tokens. Public halves are published at /.well-known/jwks.json.
CREATE TABLE signing_key (
    kid         VARCHAR(64) PRIMARY KEY,
    public_key  TEXT        NOT NULL,
    private_key TEXT        NOT NULL,
    active      BOOLEAN     NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL
);

CREATE TABLE portal (
    id            UUID PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    slug          VARCHAR(30)  NOT NULL UNIQUE,
    owner_user_id UUID         NOT NULL REFERENCES app_user (id),
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL
);

CREATE TABLE portal_member (
    portal_id UUID        NOT NULL REFERENCES portal (id),
    user_id   UUID        NOT NULL REFERENCES app_user (id),
    profile   VARCHAR(30) NOT NULL,
    status    VARCHAR(20) NOT NULL,
    joined_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (portal_id, user_id)
);
CREATE INDEX ix_portal_member_user ON portal_member (user_id);
