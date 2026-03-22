package com.osm.inventory_service.dto;

import com.osm.inventory_service.entity.BOM;
import com.osm.inventory_service.entity.SKU;
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
    private UUID skuId;
    private String skuCode;
    private SKU sku;
    private String version;

}