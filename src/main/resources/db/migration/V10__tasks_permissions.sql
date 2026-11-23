-- M1 Step 8 (tasks): TASKS_VIEW for every profile, TASKS_CREATE and TASKS_EDIT for Admin, Manager and Employee,
-- TASKS_DELETE for Admin and Manager, and member events that carry them so the Work service's copy is up to date
-- (same approach as V8 and V9).

-- A. Grant the permissions to every existing portal's system profiles (same as MemberConstants.SYSTEM_PROFILES).
INSERT INTO profile_permission (profile_id, permission)
SELECT pr.id, 'TASKS_VIEW'
FROM profile pr
WHERE pr.profile_key IN ('ADMIN', 'MANAGER', 'EMPLOYEE', 'CLIENT');

INSERT INTO profile_permission (profile_id, permission)
SELECT pr.id, p.permission
FROM profile pr
CROSS JOIN (VALUES ('TASKS_CREATE'), ('TASKS_EDIT')) AS p (permission)
WHERE pr.profile_key IN ('ADMIN', 'MANAGER', 'EMPLOYEE');

INSERT INTO profile_permission (profile_id, permission)
SELECT pr.id, 'TASKS_DELETE'
FROM profile pr
WHERE pr.profile_key IN ('ADMIN', 'MANAGER');

-- B. Announce every active member once more with their current profileKey and permissions. Same shape as the live
-- event plus "backfill": true. A consumer applies a repeated UserActivated safely (it only sets the same state).
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
