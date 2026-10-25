package com.projecty.platform.modules.account.api.event;

import java.util.UUID;

/**
 * Published inside the reset transaction after a user's password is reset. Listeners run in the same
 * transaction (for example, the session module revokes every session, SEC-BR-03).
 */
public record UserPasswordResetEvent(UUID userId) {
}
