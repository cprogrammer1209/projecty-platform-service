package com.projecty.platform.modules.account.internal.dto;

/**
 * @param timeZone an IANA zone such as {@code Asia/Kolkata}, or null to use each portal's time zone
 */
public record UserPreferencesUpdateDTO(String timeZone) {
}
