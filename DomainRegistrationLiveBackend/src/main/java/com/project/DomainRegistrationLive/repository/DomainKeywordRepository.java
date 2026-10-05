package com.project.DomainRegistrationLive.repository;

import com.project.DomainRegistrationLive.dto.projection.KeywordStatsProjection;
import com.project.DomainRegistrationLive.dto.projection.RisingKeywordProjection;
import com.project.DomainRegistrationLive.entity.DomainKeyword;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface DomainKeywordRepository extends JpaRepository<DomainKeyword, UUID> {



    @Query("""
            SELECT new com.project.DomainRegistrationLive.dto.projection.KeywordStatsProjection(
                dk.keyword,
                COUNT(*),
                SUM(CASE WHEN dk.isFirst = true THEN 1 ELSE 0 END),
                SUM(CASE WHEN dk.isLast = true THEN 1 ELSE 0 END)
            )
            FROM DomainKeyword dk
            WHERE dk.registeredAt >= :windowStart
              AND dk.registeredAt < :now
            GROUP BY dk.keyword
            ORDER BY COUNT(*) DESC, dk.keyword ASC
            """)
    List<KeywordStatsProjection> getKeywordStats(@Param("windowStart") LocalDateTime windowStart,
                                                 @Param("now") LocalDateTime now,
                                                 Pageable pageable);

    // [keyword, count]
    @Query("""
            SELECT dk.keyword, COUNT(*)
            FROM DomainKeyword dk
            WHERE dk.registeredAt >= :windowStart
              AND dk.registeredAt < :now
              AND dk.isFirst = true
            GROUP BY dk.keyword
            ORDER BY COUNT(*) DESC, dk.keyword ASC
            """)
    List<Object[]> getPrefixCounts(@Param("windowStart") LocalDateTime windowStart,
                                   @Param("now") LocalDateTime now,
                                   Pageable pageable);

    // Rows: [keyword, count]
    @Query("""
            SELECT dk.keyword, COUNT(*)
            FROM DomainKeyword dk
            WHERE dk.registeredAt >= :windowStart
              AND dk.registeredAt < :now
              AND dk.isLast = true
            GROUP BY dk.keyword
            ORDER BY COUNT(*) DESC, dk.keyword ASC
            """)
    List<Object[]> getSuffixCounts(@Param("windowStart") LocalDateTime windowStart,
                                   @Param("now") LocalDateTime now,
                                   Pageable pageable);

     // Number of multi-word domains in the window.  Every multi-word domain has exactly
    @Query("""
            SELECT COUNT(*)
            FROM DomainKeyword dk
            WHERE dk.registeredAt >= :windowStart
              AND dk.registeredAt < :now
              AND dk.isFirst = true
            """)
    long countMultiWordDomains(@Param("windowStart") LocalDateTime windowStart,
                               @Param("now") LocalDateTime now);


     @Query("""
            SELECT new com.project.DomainRegistrationLive.dto.projection.RisingKeywordProjection(
                dk.keyword,
                SUM(CASE WHEN dk.registeredAt >= :recent15mStart THEN 1 ELSE 0 END),
                SUM(CASE WHEN dk.registeredAt >= :prior15mStart AND dk.registeredAt < :recent15mStart THEN 1 ELSE 0 END),
                SUM(CASE WHEN dk.registeredAt >= :recent1hStart THEN 1 ELSE 0 END),
                SUM(CASE WHEN dk.registeredAt >= :prior1hStart AND dk.registeredAt < :recent1hStart THEN 1 ELSE 0 END),
                SUM(CASE WHEN dk.registeredAt >= :recent3hStart THEN 1 ELSE 0 END),
                SUM(CASE WHEN dk.registeredAt >= :prior3hStart AND dk.registeredAt < :recent3hStart THEN 1 ELSE 0 END)
            )
            FROM DomainKeyword dk
            WHERE dk.registeredAt >= :prior3hStart
              AND dk.registeredAt < :now
            GROUP BY dk.keyword
            HAVING SUM(CASE WHEN dk.registeredAt >= :recent3hStart THEN 1 ELSE 0 END) >= :minRecent
            """)
    List<RisingKeywordProjection> getRisingKeywordStats(@Param("recent15mStart") LocalDateTime recent15mStart,
                                                        @Param("prior15mStart") LocalDateTime prior15mStart,
                                                        @Param("recent1hStart") LocalDateTime recent1hStart,
                                                        @Param("prior1hStart") LocalDateTime prior1hStart,
                                                        @Param("recent3hStart") LocalDateTime recent3hStart,
                                                        @Param("prior3hStart") LocalDateTime prior3hStart,
                                                        @Param("now") LocalDateTime now,
                                                        @Param("minRecent") long minRecent);

    @Query("""
            SELECT dk.keyword, COUNT(*)
            FROM DomainKeyword dk
            WHERE dk.registeredAt >= :todayStart
              AND dk.registeredAt < :now
            GROUP BY dk.keyword
            HAVING COUNT(*) >= :minToday
            ORDER BY COUNT(*) DESC, dk.keyword ASC
            """)
    List<Object[]> getTodayKeywordCounts(@Param("todayStart") LocalDateTime todayStart,
                                         @Param("now") LocalDateTime now,
                                         @Param("minToday") long minToday,
                                         Pageable pageable);


    @Query("""
            SELECT dk.keyword, COUNT(*)
            FROM DomainKeyword dk
            WHERE dk.keyword IN :keywords
              AND dk.registeredAt >= :windowStart
              AND dk.registeredAt < :windowEnd
            GROUP BY dk.keyword
            """)
    List<Object[]> getBaselineCounts(@Param("keywords") Collection<String> keywords,
                                     @Param("windowStart") LocalDateTime windowStart,
                                     @Param("windowEnd") LocalDateTime windowEnd);


    @Query("""
            SELECT dk.domain.id, dk.keyword
            FROM DomainKeyword dk
            WHERE dk.domain.id IN :domainIds
            ORDER BY dk.domain.id ASC, dk.position ASC
            """)
    List<Object[]> findWordsByDomainIds(@Param("domainIds") Collection<Long> domainIds);
}
