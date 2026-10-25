package com.projecty.platform.modules.portal.internal.helper;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.projecty.platform.modules.portal.internal.constants.PortalConstants;

/**
 * Portal slug rules (PTL-BR-02) and slug suggestions.
 */
public final class SlugRulesHelper {

	private SlugRulesHelper() {
	}

	/**
	 * Returns why the slug is invalid, or empty if it is valid.
	 */
	public static Optional<String> problem(String slug) {
		if (slug == null || slug.length() < PortalConstants.SLUG_MIN_LENGTH
				|| slug.length() > PortalConstants.SLUG_MAX_LENGTH) {
			return Optional.of(PortalConstants.MESSAGE_SLUG_LENGTH);
		}
		if (!PortalConstants.SLUG_PATTERN.matcher(slug).matches()) {
			return Optional.of(PortalConstants.MESSAGE_SLUG_FORMAT);
		}
		if (PortalConstants.RESERVED_SLUGS.contains(slug)) {
			return Optional.of(PortalConstants.MESSAGE_SLUG_RESERVED);
		}
		return Optional.empty();
	}

	/**
	 * Valid alternatives for a slug, before checking which are taken.
	 */
	public static Set<String> candidates(String slug) {
		String base = sanitize(slug);
		int maxStemLength = PortalConstants.SLUG_MAX_LENGTH - 4;
		String stem = (base.length() > maxStemLength) ? base.substring(0, maxStemLength).replaceAll("-+$", "") : base;
		Set<String> candidates = new LinkedHashSet<>();
		for (int i = PortalConstants.SUGGESTION_NUMBERED_FROM; i <= PortalConstants.SUGGESTION_NUMBERED_TO; i++) {
			candidates.add(stem + "-" + i);
		}
		candidates.add(stem + "-" + ThreadLocalRandom.current()
			.nextInt(PortalConstants.SUGGESTION_RANDOM_MIN, PortalConstants.SUGGESTION_RANDOM_MAX));
		candidates.removeIf((candidate) -> problem(candidate).isPresent());
		return candidates;
	}

	private static String sanitize(String slug) {
		String cleaned = (slug == null) ? "" : slug.toLowerCase(Locale.ROOT)
			.replaceAll("[^a-z0-9-]+", "-")
			.replaceAll("-{2,}", "-")
			.replaceAll("^-|-$", "");
		return (cleaned.length() < PortalConstants.SLUG_MIN_LENGTH) ? PortalConstants.FALLBACK_SLUG : cleaned;
	}

}
