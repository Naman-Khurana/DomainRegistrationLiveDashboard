package com.project.DomainRegistrationLive.repository;

import com.project.DomainRegistrationLive.entity.Domain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DomainRepository extends JpaRepository<Domain, UUID> {
}
