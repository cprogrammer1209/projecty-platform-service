package com.projecty.platform.modules.portal.internal.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.projecty.platform.modules.portal.internal.constants.PortalConstants;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

/**
 * A tenant: an organization's isolated workspace.
 */
@Entity
@Table(name = "portal")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Portal {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	private UUID id;

	private String name;

	private String slug;

	private UUID ownerUserId;

	/**
	 * PTL-US-02 display settings. Dates are stored in UTC and shown in this zone unless the user set their own.
	 */
	private String timeZone;

	private String dateFormat;

	/**
	 * ISO 4217 code, stored as CHAR(3).
	 */
	@JdbcTypeCode(SqlTypes.CHAR)
	private String currency;

	@Enumerated(EnumType.STRING)
	private WeekStart weekStart;

	@Column(updatable = false)
	private Instant createdAt;

	private Instant updatedAt;

	public Portal(String name, String slug, UUID ownerUserId, String timeZone, Instant now) {
		this.name = name;
		this.slug = slug;
		this.ownerUserId = ownerUserId;
		this.timeZone = timeZone;
		this.dateFormat = PortalConstants.DEFAULT_DATE_FORMAT;
		this.currency = PortalConstants.DEFAULT_CURRENCY;
		this.weekStart = WeekStart.MONDAY;
		this.createdAt = now;
		this.updatedAt = now;
	}

	/**
	 * Changing the currency doesn't convert existing amounts (PTL-BR-04).
	 */
	public void updateSettings(String timeZone, String dateFormat, String currency, WeekStart weekStart,
			Instant now) {
		this.timeZone = timeZone;
		this.dateFormat = dateFormat;
		this.currency = currency;
		this.weekStart = weekStart;
		this.updatedAt = now;
	}

}
