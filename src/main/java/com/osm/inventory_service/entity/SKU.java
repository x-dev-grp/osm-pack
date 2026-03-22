package com.osm.inventory_service.entity;


import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

@Audited
@Entity
@Table(name = "skus")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SKU extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String code;

    private Float volume;

    private String category;

    private Integer unitesParCols;

    private Integer colisParPalette;
    private Boolean actif = true;
}