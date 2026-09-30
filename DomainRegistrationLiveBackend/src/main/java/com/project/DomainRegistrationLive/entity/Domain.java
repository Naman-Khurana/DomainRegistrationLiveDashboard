package com.project.DomainRegistrationLive.entity;

import com.project.DomainRegistrationLive.enums.DomainStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "domain",
    indexes = {

        @Index(
            name = "dmn_status_registered_at_idx",
            columnList = "registered_at, status"
        ),
        @Index(
                name = "dmn_status_idx",
                columnList = "status"
        )
    })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Domain extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String sld;

    @Column(nullable = false)
    private String tld;

    @Column(name = "registrar_id")
    private Integer registrarId;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DomainStatus status = DomainStatus.PENDING;

    @Column(nullable = false)
    private Integer attempts;

    @Column(name = "model_version")
    private String modelVersion;

    @Column(name = "parsed_at")
    private LocalDateTime parsedAt;

    @Column(name = "feed_seq", unique = true)
    private Long feedSeq;

    @OneToMany(mappedBy = "domain")
    private List<DomainKeyword> keywords;


}
