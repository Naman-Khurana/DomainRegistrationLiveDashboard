package com.project.DomainRegistrationLive.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "domain_keyword",
        indexes = {
            @Index(
                    name = "kw_window_idx",
                    columnList = "registered_at, keyword, is_first, is_last"
            ),
            @Index(
                    name = "kw_keyword_time_idx",
                    columnList = "keyword, registered_at"
            ),
            @Index(
                name = "kw_domain_idx",
                columnList = "domain_id, position"
            )
        } )
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainKeyword extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", nullable = false)
    private Domain domain;

    @Column(nullable = false)
    private Short position;

    @Column(nullable = false)
    private String keyword;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    @Column(name = "is_first", nullable = false)
    private Boolean isFirst;

    @Column(name = "is_last", nullable = false)
    private Boolean isLast;
}