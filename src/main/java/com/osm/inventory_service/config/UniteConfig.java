package com.osm.inventory_service.config;

import lombok.Data;

@Data
public class UniteConfig implements ArticleConfig {
    private String material;
    private int volumeMl;
    private String color;
    private String neckType;
    private int weightGr;
}