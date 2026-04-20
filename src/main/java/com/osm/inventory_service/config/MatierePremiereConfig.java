package com.osm.inventory_service.config;

import lombok.Data;

@Data
public class MatierePremiereConfig implements ArticleConfig {
    private String sousType;
    private String origin;
    private String qualityGrade;
    private Double density;
    private Boolean certifieBio;
}
