package com.projecty.platform.modules.session.api.event;

import java.util.UUID;

/**
 * Published inside the refresh transaction when a support session is found past its end time. Listeners run
 * in the same transaction (the impersonation module records the end in the audit log).
 */
public record SupportSessionExpiredEvent(UUID impersonationId) {
}
