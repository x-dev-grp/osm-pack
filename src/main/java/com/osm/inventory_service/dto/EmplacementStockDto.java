package com.osm.inventory_service.dto;

import com.osm.inventory_service.Enum.CategorieArticle;
import com.osm.inventory_service.Enum.TypeEmplacement;
import com.osm.inventory_service.entity.EmplacementStock;
import com.xdev.xdevbase.dtos.BaseDto;
import lombok.*;

import java.io.Serializable;


@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EmplacementStockDto extends BaseDto<EmplacementStock> implements Serializable {
    private String code;
    private String nom;
    private TypeEmplacement typeEmplacement;
    private String capaciteMaximale;
    private String capaciteActuelle;
    private String zone;
    private Boolean disponible;
    private String reservePour;
    private String conditionsSpeciales;
    private Double temperatureMin;
    private Double temperatureMax;
    private String description;
    private String notes;
    private Boolean actif = true;
    private CategorieArticle categorieArticleStocke;
}