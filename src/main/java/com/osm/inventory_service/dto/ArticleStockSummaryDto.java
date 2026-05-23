package com.osm.inventory_service.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ArticleStockSummaryDto {
    private UUID articleId;
    private Integer quantiteActuelle;
    private Integer quantiteReservee;
    private Integer quantiteDisponible;
    private Boolean belowMinimum;
}
