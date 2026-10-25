package com.projecty.platform.modules.account.internal.event;

import com.projecty.platform.modules.account.internal.entity.EmailTokenPurpose;

/**
 * Published when an email link is created. The email is sent once the transaction commits.
 */
public record EmailTokenIssuedEvent(String email, String fullName, EmailTokenPurpose purpose, String token) {
}
