package com.osm.inventory_service.entity;

 import com.xdev.xdevbase.entities.BaseEntity;
 import com.xdev.xdevbase.models.UniteMesure;
 import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
@Getter
@Setter
@Audited
@Entity
@Table(name = "bom_line")
public class BomLine extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "bom_id")
    private BOM bom;

    @ManyToOne
    @JoinColumn(name = "articleSec_id")
    private ArticleSec article;

    private BigDecimal quantity;
    @Enumerated(EnumType.STRING)
    private UniteMesure unitOfMeasure;
}