-- M1 Step 4 (Projects): the PROJECTS_* permissions, and member events that carry permissions so the Work service
-- can keep its own copy of who may do what.

-- A. Grant the new permissions to every existing portal's system profiles (same as MemberConstants.SYSTEM_PROFILES).
INSERT INTO profile_permission (profile_id, permission)
SELECT pr.id, p.permission
FROM profile pr
CROSS JOIN (VALUES ('PROJECTS_VIEW'), ('PROJECTS_CREATE'), ('PROJECTS_EDIT'), ('PROJECTS_DELETE')) AS p(permission)
WHERE pr.profile_key IN ('ADMIN', 'MANAGER');

INSERT INTO profile_permission (profile_id, permission)
SELECT pr.id, 'PROJECTS_VIEW' FROM profile pr WHERE pr.profile_key IN ('EMPLOYEE', 'CLIENT');

-- B. Announce every active member once more, now with profileKey and permissions (and the owner, who has no
-- UserActivated of their own). Same shape as the live event plus "backfill": true. A service started later reads
-- these after the older UserActivated rows, so the permissions win.
INSERT INTO outbox_event (id, portal_id, aggregate_type, aggregate_id, event_type, payload, created_at)
SELECT gen_random_uuid(), m.portal_id, 'MEMBER', m.user_id, 'UserActivated',
       jsonb_build_object('portalId', m.portal_id, 'userId', m.user_id, 'profileId', m.profile_id,
                          'profileKey', pr.profile_key,
                          'permissions', COALESCE((SELECT jsonb_agg(pp.permission ORDER BY pp.permission)
                                                   FROM profile_permission pp WHERE pp.profile_id = pr.id), '[]'::jsonb),
                          'backfill', true),
       now()
FROM portal_member m
JOIN profile pr ON pr.id = m.profile_id
WHERE m.status = 'ACTIVE';
