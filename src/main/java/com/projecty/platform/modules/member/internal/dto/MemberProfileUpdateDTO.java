package com.projecty.platform.modules.member.internal.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record MemberProfileUpdateDTO(@NotNull UUID profileId) {
}
