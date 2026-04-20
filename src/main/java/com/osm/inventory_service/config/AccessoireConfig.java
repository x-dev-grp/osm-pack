package com.osm.inventory_service.config;

import lombok.Data;

@Data
public class AccessoireConfig implements ArticleConfig {
    private String sousType;
    private String usage;
    private Boolean necessiteMontage;
    private Integer garantieMois;
}