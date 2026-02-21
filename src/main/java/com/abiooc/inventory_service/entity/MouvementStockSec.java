package com.abiooc.inventory_service.entity;

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
public class MouvementStockSec {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private ArticleSec article;

    @Column(nullable = false)
    private Integer quantite;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_mouvement", nullable = false)
    private TypeMouvement typeMouvement;

    private String motif;

    @Column(name = "date_mouvement")
    private LocalDateTime dateMouvement;

    @PrePersist
    protected void onCreate() {
        dateMouvement = LocalDateTime.now();
    }
}