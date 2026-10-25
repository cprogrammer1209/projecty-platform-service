package com.projecty.platform.modules.portal.internal.helper;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class SlugRulesHelperTests {

	@ParameterizedTest
	@ValueSource(strings = { "acme", "acme-corp", "a1b", "123", "abcdefghijabcdefghijabcdefghij" })
	void acceptsValidSlugs(String slug) {
		assertThat(SlugRulesHelper.problem(slug)).isEmpty();
	}

	@ParameterizedTest
	@ValueSource(strings = { "ab", "abcdefghijabcdefghijabcdefghijk", "-acme", "acme-", "Acme", "acme_corp",
			"acme corp", "api", "www", "admin" })
	void rejectsInvalidSlugs(String slug) {
		assertThat(SlugRulesHelper.problem(slug)).isPresent();
	}

	@ParameterizedTest
	@ValueSource(strings = { "acme", "Acme Corp!!", "-", "abcdefghijabcdefghijabcdefghij" })
	void suggestionsAreAlwaysValid(String slug) {
		assertThat(SlugRulesHelper.candidates(slug)).isNotEmpty()
			.allSatisfy((candidate) -> assertThat(SlugRulesHelper.problem(candidate)).isEmpty());
	}

}
