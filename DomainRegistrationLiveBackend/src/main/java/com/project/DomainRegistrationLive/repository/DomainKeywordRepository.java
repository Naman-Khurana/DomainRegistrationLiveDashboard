package com.project.DomainRegistrationLive.repository;

import com.project.DomainRegistrationLive.entity.DomainKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DomainKeywordRepository extends JpaRepository<DomainKeyword, UUID> {
}
