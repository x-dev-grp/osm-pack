package com.osm.inventory_service.config;

import lombok.Data;

import java.util.UUID;

@Data
public class ColisConfig implements ArticleConfig {
    private UUID unitArticleId;
    private int unitsPerColis;
    private Dimensions dimensions;
    private double maxWeightKg;
}