package com.project.DomainRegistrationLive.repository;

import com.project.DomainRegistrationLive.entity.Domain;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DomainRepository extends JpaRepository<Domain, UUID> {
    boolean findByName(String name);
}
