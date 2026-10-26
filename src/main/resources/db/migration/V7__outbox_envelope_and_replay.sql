-- Kafka as the event backbone: the outbox row carries the whole envelope, relay order is by creation time,
-- events already "published" by the logging publisher are re-sent, and data created before the outbox existed
-- is announced once.

ALTER TABLE outbox_event ADD COLUMN version SMALLINT NOT NULL DEFAULT 1;
ALTER TABLE outbox_event ADD COLUMN actor_user_id UUID;
ALTER TABLE outbox_event ADD COLUMN actor_name VARCHAR(200);

-- The relay reads the oldest unpublished events by (created_at, id). Backfilled rows carry their original
-- timestamps, so they sort before live ones.
DROP INDEX ix_outbox_event_unpublished;
CREATE INDEX ix_outbox_event_unpublished ON outbox_event (created_at, id) WHERE published_at IS NULL;

-- A. Nothing was delivered while the broker seam only logged, so send the existing events again. They keep
-- their ids, which consumers use to drop duplicates.
UPDATE outbox_event SET published_at = NULL WHERE published_at IS NOT NULL;

-- B. Snapshot of state from before the outbox existed. Shaped like the live events plus "backfill": true.
-- Anything that already has an event of its kind is skipped. Profile change history can't be recovered, so
-- UserActivated carries the member's current profile. The owner is announced by PortalCreated only, as live.
INSERT INTO outbox_event (id, portal_id, aggregate_type, aggregate_id, event_type, payload, created_at,
                          actor_user_id, actor_name)
SELECT gen_random_uuid(), p.id, 'PORTAL', p.id, 'PortalCreated',
       jsonb_build_object('portalId', p.id, 'slug', p.slug, 'ownerUserId', p.owner_user_id, 'backfill', true),
       p.created_at, u.id, u.full_name
FROM portal p
JOIN app_user u ON u.id = p.owner_user_id
WHERE NOT EXISTS (SELECT 1 FROM outbox_event o
                  WHERE o.event_type = 'PortalCreated' AND o.aggregate_id = p.id);

INSERT INTO outbox_event (id, portal_id, aggregate_type, aggregate_id, event_type, payload, created_at)
SELECT gen_random_uuid(), m.portal_id, 'MEMBER', m.user_id, 'UserActivated',
       jsonb_build_object('portalId', m.portal_id, 'userId', m.user_id, 'profileId', m.profile_id,
                          'backfill', true),
       m.joined_at
FROM portal_member m
JOIN portal p ON p.id = m.portal_id
WHERE m.user_id <> p.owner_user_id
  AND NOT EXISTS (SELECT 1 FROM outbox_event o
                  WHERE o.event_type = 'UserActivated' AND o.portal_id = m.portal_id
                    AND o.aggregate_id = m.user_id);

INSERT INTO outbox_event (id, portal_id, aggregate_type, aggregate_id, event_type, payload, created_at)
SELECT gen_random_uuid(), m.portal_id, 'MEMBER', m.user_id, 'UserDeactivated',
       jsonb_build_object('portalId', m.portal_id, 'userId', m.user_id, 'backfill', true),
       GREATEST(COALESCE(m.deactivated_at, m.joined_at), m.joined_at + INTERVAL '1 millisecond')
FROM portal_member m
WHERE m.status = 'DEACTIVATED'
  AND NOT EXISTS (SELECT 1 FROM outbox_event o
                  WHERE o.event_type = 'UserDeactivated' AND o.portal_id = m.portal_id
                    AND o.aggregate_id = m.user_id);

INSERT INTO outbox_event (id, portal_id, aggregate_type, aggregate_id, event_type, payload, created_at,
                          actor_user_id, actor_name)
SELECT gen_random_uuid(), i.portal_id, 'INVITATION', i.id, 'UserInvited',
       jsonb_build_object('portalId', i.portal_id, 'invitationId', i.id, 'profileId', i.profile_id,
                          'invitedByUserId', i.invited_by_user_id, 'backfill', true),
       i.created_at, u.id, u.full_name
FROM invitation i
JOIN app_user u ON u.id = i.invited_by_user_id
WHERE NOT EXISTS (SELECT 1 FROM outbox_event o
                  WHERE o.event_type = 'UserInvited' AND o.aggregate_id = i.id);

-- Revocation time wasn't stored, so the event is placed just after the invitation.
INSERT INTO outbox_event (id, portal_id, aggregate_type, aggregate_id, event_type, payload, created_at)
SELECT gen_random_uuid(), i.portal_id, 'INVITATION', i.id, 'InvitationRevoked',
       jsonb_build_object('portalId', i.portal_id, 'invitationId', i.id, 'backfill', true),
       i.created_at + INTERVAL '1 millisecond'
FROM invitation i
WHERE i.status = 'REVOKED'
  AND NOT EXISTS (SELECT 1 FROM outbox_event o
                  WHERE o.event_type = 'InvitationRevoked' AND o.aggregate_id = i.id);
