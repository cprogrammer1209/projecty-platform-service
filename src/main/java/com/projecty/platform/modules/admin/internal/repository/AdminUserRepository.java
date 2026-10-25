package com.projecty.platform.modules.admin.internal.repository;

import java.util.Optional;
import java.util.UUID;

import com.projecty.platform.modules.admin.internal.entity.AdminUser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AdminUserRepository extends JpaRepository<AdminUser, UUID> {

	@Query("select a from AdminUser a where lower(a.email) = lower(:email)")
	Optional<AdminUser> findByEmail(String email);

}
