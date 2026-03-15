package com.abiooc.inventory_service.dto;

import com.abiooc.inventory_service.entity.SKU;
import com.xdev.xdevbase.dtos.BaseDto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;



@Getter
@Setter
@Data
public class SKUDto extends BaseDto<SKU> implements Serializable {
    UUID id;
    UUID tenantId;
    Boolean isDeleted;
    String createdBy;
    LocalDateTime createdDate;
    String lastModifiedBy;
    LocalDateTime lastModifiedDate;
    UUID externalId;
    String code;
    Float volume;
    String category;
    Integer unitesParCols;
    Integer colisParPalette;
}