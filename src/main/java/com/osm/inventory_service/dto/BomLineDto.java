package com.osm.inventory_service.dto;

import com.osm.inventory_service.entity.ArticleSec;
import com.osm.inventory_service.entity.BOM;
import com.osm.inventory_service.entity.BomLine;
import com.xdev.xdevbase.dtos.BaseDto;
import com.xdev.xdevbase.models.UniteMesure;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Data
@Getter
public class BomLineDto  extends BaseDto<BomLine> implements Serializable {
    private BOM bom;
    private ArticleSec article;
    private UUID articleId;
    private String articleName;
    private double quantity;
    private UniteMesure unitOfMeasure;

}