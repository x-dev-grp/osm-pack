package com.osm.inventory_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockDashboardPayloadDto {
    private StatistiquesDTO statistiques = new StatistiquesDTO();
    private List<ArticleCritiqueDto> articlesCritiques = new ArrayList<>();
    private List<MouvementRecentDto> mouvementsRecents = new ArrayList<>();
}
