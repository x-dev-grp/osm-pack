package com.abiooc.inventory_service.dto;

import com.abiooc.inventory_service.Enum.Statue;
import com.abiooc.inventory_service.entity.LigneConditionnement;
import com.xdev.xdevbase.dtos.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LigneConditionnementDto extends BaseDto<LigneConditionnement> implements Serializable {
    private String code;
    private String nom;
    private String description;
    private Statue etat;
    private Integer vitesseNominale;
    private Integer tempsPreparation;
    private Integer tempsNettoyage;
    private String responsable;
    private LocalDateTime dateDerniereMaintenance;
    private LocalDateTime dateProchaineMaintenance;
    private String notes;
}