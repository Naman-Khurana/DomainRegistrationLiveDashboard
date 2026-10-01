package com.project.DomainRegistrationLive.entity;

import com.project.DomainRegistrationLive.dto.FeedItem;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "snapshot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Snapshot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_seq", nullable = false)
    private Long fromSeq;

    @Column(name = "to_seq", nullable = false)
    private Long toSeq;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "stats", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> stats;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "feed", nullable = false, columnDefinition = "jsonb")
    private List<FeedItem> feed;
}

