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
public class ProduitFinal extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType type = ProductType.NON_VRAC;

    private String category;

    @Column(name = "unit_of_measure")
    private String unitOfMeasure;

    @Column(columnDefinition = "text")
    private String description;

    private String grade;

    private String origin;

    @Column(name = "harvest_campaign")
    private String harvestCampaign;

    private Float volume;

    @Column(name = "packaging_type")
    private String packagingType;

    private String barcode;

    @Column(name = "unites_par_cols")
    private Integer unitsPerCarton;

    @Column(name = "colis_par_palette")
    private Integer cartonsPerPallet;

    @Column(name = "net_weight")
    private Float netWeight;

    @Column(name = "gross_weight")
    private Float grossWeight;

    private String brand;

    private Float density;

    @Column(name = "storage_unit")
    private String storageUnit;

    private Boolean actif = true;

    public Integer getUnitesParCols() {
        return unitsPerCarton;
    }

    public void setUnitesParCols(Integer unitesParCols) {
        this.unitsPerCarton = unitesParCols;
    }

    public Integer getColisParPalette() {
        return cartonsPerPallet;
    }

    public void setColisParPalette(Integer colisParPalette) {
        this.cartonsPerPallet = colisParPalette;
    }
}
