package com.project.DomainRegistrationLive.repository;

import com.project.DomainRegistrationLive.entity.Domain;
import com.project.DomainRegistrationLive.enums.DomainStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DomainRepository extends JpaRepository<Domain, Long> {
    Optional<Domain> findByName(String name);

    List<Domain> findByNameIn(List<String> names);

    List<Domain> findTop100ByStatusOrderByCreatedAtAscIdAsc(DomainStatus status);

    List<Domain> findByIdGreaterThanAndStatusOrderByIdAsc(long lastDomainPassedInFeed, DomainStatus domainStatus);

    Optional<List<Domain>> findTop200BySldContainingIgnoreCaseAndRegisteredAtGreaterThanEqualAndRegisteredAtLessThanAndStatusOrderByIdDesc(
            String keyword,
            LocalDateTime from,
            LocalDateTime to,
            DomainStatus status
    );

    List<Domain> findTop200ByTldContainingIgnoreCaseAndRegisteredAtGreaterThanEqualAndRegisteredAtLessThanAndStatusOrderByIdDesc(
            String keyword,
            LocalDateTime from,
            LocalDateTime to,
            DomainStatus status
    );

    /** Newest first, for a brand-new session: ids up to and including toId. */
    List<Domain> findByStatusAndIdLessThanEqualOrderByIdDesc(DomainStatus status, Long toId, Pageable pageable);

    /** Newest first: ids in (fromId, toId]. */
    List<Domain> findByStatusAndIdGreaterThanAndIdLessThanEqualOrderByIdDesc(DomainStatus status,
                                                                             Long fromId,
                                                                             Long toId,
                                                                             Pageable pageable);





    // Counters.  One row: [h60, m10, m1]
    @Query("""
            SELECT COUNT(*),
                   COUNT(CASE WHEN d.registeredAt >= :m10Start THEN 1 END),
                   COUNT(CASE WHEN d.registeredAt >= :m1Start THEN 1 END)
            FROM Domain d
            WHERE d.status = :status
              AND d.registeredAt >= :h60Start
              AND d.registeredAt < :now
            """)
    List<Object[]> getWindowCounters(@Param("h60Start") LocalDateTime h60Start,
                                     @Param("m10Start") LocalDateTime m10Start,
                                     @Param("m1Start") LocalDateTime m1Start,
                                     @Param("now") LocalDateTime now,
                                     @Param("status") DomainStatus status);




    // TLDs.  Rows: [tld, count]

    @Query("""
            SELECT d.tld, COUNT(*)
            FROM Domain d
            WHERE d.status = :status
              AND d.registeredAt >= :windowStart
              AND d.registeredAt < :now
            GROUP BY d.tld
            ORDER BY COUNT(*) DESC, d.tld ASC
            """)
    List<Object[]> getTldCounts(@Param("windowStart") LocalDateTime windowStart,
                                @Param("now") LocalDateTime now,
                                @Param("status") DomainStatus status,
                                Pageable pageable);




    // Registrars.  Rows: [registrarId, count]
    @Query("""
            SELECT d.registrarId, COUNT(*)
            FROM Domain d
            WHERE d.status = :status
              AND d.registeredAt >= :windowStart
              AND d.registeredAt < :now
              AND d.registrarId IS NOT NULL
            GROUP BY d.registrarId
            ORDER BY COUNT(*) DESC, d.registrarId ASC
            """)
    List<Object[]> getRegistrarCounts(@Param("windowStart") LocalDateTime windowStart,
                                      @Param("now") LocalDateTime now,
                                      @Param("status") DomainStatus status,
                                      Pageable pageable);




    // Format.  One row: [total, hyphen, short5, numeric, multiword, oneword]
    @Query("""
            SELECT COUNT(*),
                   COUNT(CASE WHEN d.sld LIKE '%-%' THEN 1 END),
                   COUNT(CASE WHEN LENGTH(d.sld) <= 5 THEN 1 END),
                   COUNT(CASE WHEN LOWER(d.sld) = UPPER(d.sld) AND d.sld NOT LIKE '%-%' THEN 1 END),
                   COUNT(CASE WHEN d.keywordCount > 1 THEN 1 END),
                   COUNT(CASE WHEN d.keywordCount = 1 THEN 1 END)
            FROM Domain d
            WHERE d.status = :status
              AND d.registeredAt >= :windowStart
              AND d.registeredAt < :now
            """)
    List<Object[]> getFormatCounts(@Param("windowStart") LocalDateTime windowStart,
                                   @Param("now") LocalDateTime now,
                                   @Param("status") DomainStatus status);


    @Query("""
        SELECT d.registrarId,
               COUNT(d.id)
        FROM Domain d
        WHERE d.status = :status
          AND d.registeredAt >= :windowStart
          AND d.registeredAt < :now
          AND (:tld IS NULL OR d.tld = :tld)
          AND d.registrarId IS NOT NULL
        GROUP BY d.registrarId
        ORDER BY COUNT(d.id) DESC, d.registrarId ASC
        """)
    List<Object[]> getRegistrarCountsV2(
            @Param("windowStart") LocalDateTime windowStart,
            @Param("now") LocalDateTime now,
            @Param("tld") String tld,
            @Param("status") DomainStatus status
    );





    // Repeats.  Rows: [sld, distinctTldCount]   (grouped and limited in SQL)
    @Query("""
            SELECT d.sld, COUNT(DISTINCT d.tld)
            FROM Domain d
            WHERE d.status = :status
              AND d.registeredAt >= :windowStart
              AND d.registeredAt < :now
            GROUP BY d.sld
            HAVING COUNT(DISTINCT d.tld) >= :minTlds
            ORDER BY COUNT(DISTINCT d.tld) DESC, d.sld ASC
            """)
    List<Object[]> getRepeatedSlds(@Param("windowStart") LocalDateTime windowStart,
                                   @Param("now") LocalDateTime now,
                                   @Param("status") DomainStatus status,
                                   @Param("minTlds") long minTlds,
                                   Pageable pageable);

    /** Only for the few winning SLDs.  Rows: [sld, tld, domainId] */
    @Query("""
            SELECT d.sld, d.tld, d.id
            FROM Domain d
            WHERE d.sld IN :slds
              AND d.status = :status
              AND d.registeredAt >= :windowStart
              AND d.registeredAt < :now
            ORDER BY d.sld ASC, d.tld ASC
            """)
    List<Object[]> getSldTldsFor(@Param("slds") Collection<String> slds,
                                 @Param("windowStart") LocalDateTime windowStart,
                                 @Param("now") LocalDateTime now,
                                 @Param("status") DomainStatus status);

    // Hourly.  Rows: [year, month, day, hour, count]

    @Query("""
            SELECT EXTRACT(YEAR FROM d.registeredAt),
                   EXTRACT(MONTH FROM d.registeredAt),
                   EXTRACT(DAY FROM d.registeredAt),
                   EXTRACT(HOUR FROM d.registeredAt),
                   COUNT(*)
            FROM Domain d
            WHERE d.status = :status
              AND d.registeredAt >= :windowStart
              AND d.registeredAt < :windowEnd
            GROUP BY EXTRACT(YEAR FROM d.registeredAt),
                     EXTRACT(MONTH FROM d.registeredAt),
                     EXTRACT(DAY FROM d.registeredAt),
                     EXTRACT(HOUR FROM d.registeredAt)
            """)
    List<Object[]> getHourlyCounts(@Param("windowStart") LocalDateTime windowStart,
                                   @Param("windowEnd") LocalDateTime windowEnd,
                                   @Param("status") DomainStatus status);

    @Query("SELECT MAX(d.id) FROM Domain d WHERE d.status = :status")
    Long findMaxIdByStatus(@Param("status") DomainStatus status);




    @Query("SELECT MIN(d.registeredAt) FROM Domain d WHERE d.status = :status")
    LocalDateTime findEarliestRegisteredAt(@Param("status") DomainStatus status);

    @Query("SELECT MAX(d.createdAt) FROM Domain d")
    LocalDateTime findLatestCreatedAt();
}
