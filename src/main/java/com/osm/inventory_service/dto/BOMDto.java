package com.osm.inventory_service.dto;

import com.osm.inventory_service.entity.BOM;
import com.osm.inventory_service.entity.ProduitFinal;
import com.xdev.xdevbase.dtos.BaseDto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
@Getter
@Setter

public class BOMDto  extends BaseDto<BOM> implements Serializable {
    private List<BomLineDto> lines;
    private UUID productId;
    private String productName;
    private ProduitFinal produitFinal;
    private String version;
    private Boolean active;
    private String publicCode;
    private String qrUrl;
    private String qrImageBase64;

    public UUID getSkuId() {
        return productId;
    }

    public void setSkuId(UUID skuId) {
        this.productId = skuId;
    }

    public String getSkuCode() {
        return productName;
    }

    public void setSkuCode(String skuCode) {
        this.productName = skuCode;
    }
}
