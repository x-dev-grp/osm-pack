package com.osm.inventory_service.entity;

import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;
import com.osm.inventory_service.Enum.TypeMouvement;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "mouvements_stock_secs")
@Data
@Audited
@NoArgsConstructor
@AllArgsConstructor
public class MouvementStockSec  extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private ArticleSec article;

    @Column(nullable = false)
    private Integer quantite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeMouvement typeMouvement;

    private String motif;

    private LocalDateTime dateMouvement;

    @Column(name = "reference_type")
    private String referenceType;

    @Column(name = "reference_id")
    private UUID referenceId;

}