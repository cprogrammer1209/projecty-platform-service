package com.projecty.platform.modules.account.internal.repository;

import java.util.Optional;
import java.util.UUID;

import com.projecty.platform.modules.account.internal.entity.AppUser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

	@Query("select u from AppUser u where lower(u.email) = lower(:email)")
	Optional<AppUser> findByEmail(String email);

	@Query("select count(u) > 0 from AppUser u where lower(u.email) = lower(:email)")
	boolean existsByEmail(String email);

}
