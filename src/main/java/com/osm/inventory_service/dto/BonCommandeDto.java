package com.osm.inventory_service.dto;

import com.osm.inventory_service.Enum.StatutBonCommande;
import com.osm.inventory_service.entity.BonCommande;
import com.xdev.xdevbase.dtos.BaseDto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
 @Data
public class BonCommandeDto  extends BaseDto<BonCommande> implements Serializable{
    UUID id;
    UUID tenantId;
    Boolean isDeleted;
    String createdBy;
    LocalDateTime createdDate;
    String lastModifiedBy;
    LocalDateTime lastModifiedDate;
    UUID externalId;
    String numeroBC;
    LocalDateTime dateValidation;
    LocalDateTime dateReceptionPrevue;
    StatutBonCommande status;
    List<LigneBonCommandeDto> lignes;
    String motifRefus;
    String publicCode;
    String qrUrl;
    String qrImageBase64;

}