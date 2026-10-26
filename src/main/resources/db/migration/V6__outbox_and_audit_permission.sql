-- Slice 5: transactional outbox (SEC-US-06), audit log permission and append-only guard (SEC-BR-06).

CREATE TABLE outbox_event (
    id             UUID PRIMARY KEY,
    portal_id      UUID,
    aggregate_type VARCHAR(40)  NOT NULL,
    aggregate_id   UUID         NOT NULL,
    event_type     VARCHAR(60)  NOT NULL,
    payload        JSONB        NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL,
    published_at   TIMESTAMPTZ
);
CREATE INDEX ix_outbox_event_unpublished ON outbox_event (id) WHERE published_at IS NULL;
CREATE INDEX ix_outbox_event_portal ON outbox_event (portal_id);

-- Existing portals: Admins may read the portal audit log.
INSERT INTO profile_permission (profile_id, permission)
SELECT pr.id, 'AUDIT_VIEW'
FROM profile pr
WHERE pr.profile_key = 'ADMIN';

-- Audit entries are never edited or deleted, not even by SQL from the application's own connection.
CREATE FUNCTION reject_audit_event_change() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'audit_event is append-only';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER audit_event_append_only
    BEFORE UPDATE OR DELETE ON audit_event
    FOR EACH ROW EXECUTE FUNCTION reject_audit_event_change();
