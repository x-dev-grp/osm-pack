package com.osm.inventory_service.dto;

import com.osm.inventory_service.Enum.TypeMouvement;
import com.osm.inventory_service.entity.MouvementStockSec;
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
public class MouvementStockSecDto  extends BaseDto<MouvementStockSec> implements Serializable{
    UUID id;
    UUID tenantId;
    Boolean isDeleted;
    String createdBy;
    LocalDateTime createdDate;
    String lastModifiedBy;
    LocalDateTime lastModifiedDate;
    UUID externalId;
    ArticleSecDto article;
    Integer quantite;
    TypeMouvement typeMouvement;
    String motif;
    LocalDateTime dateMouvement;

}