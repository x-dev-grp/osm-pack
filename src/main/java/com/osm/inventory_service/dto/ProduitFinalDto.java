package com.osm.inventory_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.osm.inventory_service.entity.ProduitFinal;
import com.osm.inventory_service.entity.ProductType;
import com.xdev.xdevbase.dtos.BaseDto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Data
public class ProduitFinalDto extends BaseDto<ProduitFinal> implements Serializable {
    UUID id;
    UUID tenantId;
    Boolean isDeleted;
    String createdBy;
    LocalDateTime createdDate;
    String lastModifiedBy;
    LocalDateTime lastModifiedDate;
    UUID externalId;
    String name;
    @JsonAlias("skuCode")
    String code;
    ProductType type = ProductType.NON_VRAC;
    String category;
    String unitOfMeasure;
    String description;
    String grade;
    String origin;
    String harvestCampaign;
    Float volume;
    String packagingType;
    String barcode;
    @JsonAlias("unitesParCols")
    Integer unitsPerCarton;
    @JsonAlias("colisParPalette")
    Integer cartonsPerPallet;
    Float netWeight;
    Float grossWeight;
    String brand;
    Float density;
    String storageUnit;
    private Boolean actif = true;

    @JsonProperty("unitesParCols")
    public Integer getUnitesParCols() {
        return unitsPerCarton;
    }

    public void setUnitesParCols(Integer unitesParCols) {
        this.unitsPerCarton = unitesParCols;
    }

    @JsonProperty("colisParPalette")
    public Integer getColisParPalette() {
        return cartonsPerPallet;
    }

    public void setColisParPalette(Integer colisParPalette) {
        this.cartonsPerPallet = colisParPalette;
    }
}
