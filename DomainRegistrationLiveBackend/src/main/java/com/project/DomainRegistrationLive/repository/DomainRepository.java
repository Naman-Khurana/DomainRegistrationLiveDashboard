package com.project.DomainRegistrationLive.repository;

import com.project.DomainRegistrationLive.entity.Domain;
import com.project.DomainRegistrationLive.enums.DomainStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DomainRepository extends JpaRepository<Domain, UUID> {
    Optional<Domain> findByName(String name);

    List<Domain> findByNameIn(List<String> names);

    List<Domain> findTop100ByStatusOrderByCreatedAtAscIdAsc(DomainStatus status);
}
