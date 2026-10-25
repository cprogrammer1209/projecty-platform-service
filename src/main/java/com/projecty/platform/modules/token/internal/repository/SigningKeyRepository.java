package com.projecty.platform.modules.token.internal.repository;

import java.util.List;

import com.projecty.platform.modules.token.internal.entity.SigningKey;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SigningKeyRepository extends JpaRepository<SigningKey, String> {

	List<SigningKey> findByActiveTrueOrderByCreatedAtDesc();

}
