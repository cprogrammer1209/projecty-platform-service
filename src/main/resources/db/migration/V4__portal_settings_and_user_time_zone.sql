-- PTL-US-02: how a portal displays dates and money. PTL-US-12: a user's own time zone (null = the portal's).
ALTER TABLE portal ADD COLUMN time_zone   VARCHAR(64) NOT NULL DEFAULT 'UTC';
ALTER TABLE portal ADD COLUMN date_format VARCHAR(20) NOT NULL DEFAULT 'MMM d, yyyy';
ALTER TABLE portal ADD COLUMN currency    CHAR(3)     NOT NULL DEFAULT 'USD';
ALTER TABLE portal ADD COLUMN week_start  VARCHAR(10) NOT NULL DEFAULT 'MONDAY';

ALTER TABLE app_user ADD COLUMN time_zone VARCHAR(64);
