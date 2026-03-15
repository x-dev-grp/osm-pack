package com.abiooc.inventory_service.entity;

import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.abiooc.inventory_service.Enum.TypeMouvement;

@Entity
@Table(name = "mouvements_stock_secs")
@Data
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

}