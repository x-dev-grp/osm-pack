package com.osm.inventory_service.entity;

import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.util.ArrayList;
import java.util.List;
@Data
@Getter
@Audited
@Setter
@Entity
@Table(name = "bom")
public class BOM extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "sku_id")
    private Product product;
    private String version;
    @OneToMany(mappedBy = "bom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BomLine> lines = new ArrayList<>();
}
