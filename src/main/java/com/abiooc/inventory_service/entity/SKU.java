package com.abiooc.inventory_service.entity;


import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(name = "unites_par_cols")
    private Integer unitesParCoulis;

    @Column(name = "colis_par_palette")
    private Integer colisParPalette;
}