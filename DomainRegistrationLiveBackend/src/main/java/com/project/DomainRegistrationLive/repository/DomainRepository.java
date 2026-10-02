package com.project.DomainRegistrationLive.repository;

import com.project.DomainRegistrationLive.dto.*;
import com.project.DomainRegistrationLive.dto.projection.FeedDomainProjection;
import com.project.DomainRegistrationLive.entity.Domain;
import com.project.DomainRegistrationLive.enums.DomainStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
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

    /** Newest first, for a brand-new session: ids up to and including toId. */
    List<Domain> findByStatusAndIdLessThanEqualOrderByIdDesc(DomainStatus status, Long toId, Pageable pageable);

    /** Newest first: ids in (fromId, toId]. */
    List<Domain> findByStatusAndIdGreaterThanAndIdLessThanEqualOrderByIdDesc(DomainStatus status,
                                                                             Long fromId,
                                                                             Long toId,
                                                                             Pageable pageable);



//    @Query("""
//    SELECT new com.project.DomainRegistrationLive.dto.DomainStatsProjection(
//        COALESCE(SUM(
//            CASE
//                WHEN d.registeredAt >= :h60Start THEN 1
//                ELSE 0
//            END
//        ), 0),
//
//        COALESCE(SUM(
//            CASE
//                WHEN d.registeredAt >= :m10Start THEN 1
//                ELSE 0
//            END
//        ), 0),
//
//        COALESCE(SUM(
//            CASE
//                WHEN d.registeredAt >= :m1Start THEN 1
//                ELSE 0
//            END
//        ), 0)
//    )
//    FROM Domain d
//    WHERE d.status = :status
//      AND d.registeredAt >= :h60Start
//      AND d.registeredAt < :now
//    """)
//    DomainStatsProjection getDomainStats(
//            @Param("h60Start") LocalDateTime h60Start,
//            @Param("m10Start") LocalDateTime m10Start,
//            @Param("m1Start") LocalDateTime m1Start,
//            @Param("now") LocalDateTime now,
//            @Param("status") DomainStatus status
//    );

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


//
//    @Query("""
//    SELECT new com.project.DomainRegistrationLive.dto.TldCountProjection(
//        d.tld,
//        COUNT(d.id)
//    )
//    FROM Domain d
//    WHERE d.status = :status
//      AND d.registeredAt >= :windowStart
//      AND d.registeredAt < :now
//    GROUP BY d.tld
//    ORDER BY COUNT(d.id) DESC, d.tld ASC
//    """)
//    List<TldCountProjection> getTldCounts(
//            @Param("windowStart") LocalDateTime windowStart,
//            @Param("now") LocalDateTime now,
//            @Param("status") DomainStatus status
//    );


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


//    @Query("""
//    SELECT new com.project.DomainRegistrationLive.dto.RegistrarCountProjection(
//        d.registrarId,
//        COUNT(d.id)
//    )
//    FROM Domain d
//    WHERE d.status = :status
//      AND d.registeredAt >= :windowStart
//      AND d.registeredAt < :now
//      AND d.registrarId IS NOT NULL
//    GROUP BY d.registrarId
//    ORDER BY COUNT(d.id) DESC, d.registrarId ASC
//    """)
//    List<RegistrarCountProjection> getRegistrarCounts(
//            @Param("windowStart") LocalDateTime windowStart,
//            @Param("now") LocalDateTime now,
//            @Param("status") DomainStatus status
//    );


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

//
//    @Query("""
//    SELECT new com.project.DomainRegistrationLive.dto.DomainFormatProjection(
//        COUNT(d.id),
//
//        SUM(
//            CASE
//                WHEN d.sld LIKE '%-%' THEN 1
//                ELSE 0
//            END
//        ),
//
//        SUM(
//            CASE
//                WHEN LENGTH(d.sld) <= 5 THEN 1
//                ELSE 0
//            END
//        )
//    )
//    FROM Domain d
//    WHERE d.status = :status
//      AND d.registeredAt >= :windowStart
//      AND d.registeredAt < :now
//    """)
//    DomainFormatProjection getDomainFormat(
//            @Param("windowStart") LocalDateTime windowStart,
//            @Param("now") LocalDateTime now,
//            @Param("status") DomainStatus status
//    );


    // Format.  One row: [total, hyphen, short5, numeric]
    @Query("""
            SELECT COUNT(*),
                   COUNT(CASE WHEN d.sld LIKE '%-%' THEN 1 END),
                   COUNT(CASE WHEN LENGTH(d.sld) <= 5 THEN 1 END),
                   COUNT(CASE WHEN LOWER(d.sld) = UPPER(d.sld) AND d.sld NOT LIKE '%-%' THEN 1 END)
            FROM Domain d
            WHERE d.status = :status
              AND d.registeredAt >= :windowStart
              AND d.registeredAt < :now
            """)
    List<Object[]> getFormatCounts(@Param("windowStart") LocalDateTime windowStart,
                                   @Param("now") LocalDateTime now,
                                   @Param("status") DomainStatus status);


//    @Query("""
//    SELECT new com.project.DomainRegistrationLive.dto.RepeatedSldProjection(
//        d.sld,
//        COUNT(DISTINCT d.tld)
//    )
//    FROM Domain d
//    WHERE d.status = :status
//      AND d.registeredAt >= :windowStart
//      AND d.registeredAt < :now
//    GROUP BY d.sld
//    HAVING COUNT(DISTINCT d.tld) > 1
//    ORDER BY COUNT(DISTINCT d.tld) DESC, d.sld ASC
//    """)
//    List<RepeatedSldProjection> getRepeatedSlds(
//            @Param("windowStart") LocalDateTime windowStart,
//            @Param("now") LocalDateTime now,
//            @Param("status") DomainStatus status
//    );



//    @Query("""
//        SELECT new com.project.DomainRegistrationLive.dto.SldTldProjection(
//            d.sld,
//            d.tld
//        )
//        FROM Domain d
//        WHERE d.status = :status
//          AND d.registeredAt >= :windowStart
//          AND d.registeredAt < :now
//        ORDER BY d.sld ASC, d.tld ASC
//        """)
//    List<SldTldProjection> getSldTlds(
//            @Param("windowStart") LocalDateTime windowStart,
//            @Param("now") LocalDateTime now,
//            @Param("status") DomainStatus status
//    );


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



//    @Query(value = """
//    SELECT
//        x.id,
//        x.name,
//        x.tld,
//        x.registrar_id,
//        x.registered_at
//    FROM (
//        SELECT
//            d.id,
//            d.name,
//            d.tld,
//            d.registrar_id,
//            d.registered_at,
//
//            COUNT(*) FILTER (
//                WHERE d.registered_at > :since
//            ) OVER () AS new_count,
//
//            ROW_NUMBER() OVER (
//                ORDER BY d.registered_at DESC
//            ) AS row_num
//
//        FROM domain d
//        WHERE d.status = :status
//    ) x
//    WHERE
//        x.new_count > 60
//        OR x.registered_at > :since
//        OR x.row_num <= 60
//
//    ORDER BY x.registered_at DESC
//    """,
//            nativeQuery = true)
//    List<FeedDomainProjection> getFeed(
//            @Param("since") LocalDateTime since,
//            @Param("status") String status
//    );

    @Query("SELECT MIN(d.registeredAt) FROM Domain d WHERE d.status = :status")
    LocalDateTime findEarliestRegisteredAt(@Param("status") DomainStatus status);

    @Query("SELECT MAX(d.createdAt) FROM Domain d")
    LocalDateTime findLatestCreatedAt();
}
