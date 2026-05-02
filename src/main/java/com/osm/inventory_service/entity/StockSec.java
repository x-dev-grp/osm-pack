package com.osm.inventory_service.entity;

import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

@Entity
@Table(name = "stocks_secs")
@Data
@Audited
@NoArgsConstructor
@AllArgsConstructor
public class StockSec extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "article_id", unique = true)
    private ArticleSec article;

    @Column(name = "quantite_actuelle", nullable = false)
    private Integer quantiteActuelle = 0;

    @ManyToOne
    @JoinColumn(name = "emplacement_id")
    private EmplacementStock emplacement;

    //expedition
    @Column(name = "reserve_pour")
    private String reservePour;

    @Column(name = "reserve_date")
    private LocalDateTime reserveDate;


}