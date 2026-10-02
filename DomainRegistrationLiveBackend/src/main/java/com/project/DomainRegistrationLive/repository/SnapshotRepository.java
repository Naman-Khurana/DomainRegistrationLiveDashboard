package com.project.DomainRegistrationLive.repository;

import com.project.DomainRegistrationLive.entity.Snapshot;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface SnapshotRepository extends JpaRepository<Snapshot, Long> {
    Optional<Snapshot> findTopByOrderByIdDesc();

    @Modifying
    @Transactional
    @Query("DELETE FROM Snapshot s WHERE s.id < :id")
    int deleteOlderThan(@Param("id") Long id);
}
