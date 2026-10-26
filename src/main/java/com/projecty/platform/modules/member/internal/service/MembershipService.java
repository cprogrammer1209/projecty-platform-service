package com.projecty.platform.modules.member.internal.service;

import com.projecty.platform.common.service.PortalAccessService;
import com.projecty.platform.modules.member.api.MemberLookUpService;

/**
 * Read-only membership questions from other modules and from security (PortalContextFilter,
 * PermissionInterceptor). Kept apart from {@link MemberService} so it needs nothing from the account module,
 * which itself reads memberships for {@code /api/me}.
 */
public interface MembershipService extends MemberLookUpService, PortalAccessService {

}
