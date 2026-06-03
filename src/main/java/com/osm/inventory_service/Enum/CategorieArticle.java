package com.osm.inventory_service.Enum;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Locale;

public enum CategorieArticle {
    EMBALLAGE,
    CONSOMMABLE,
    UNITE,
    COLIS,
    PALETTE;

    @JsonCreator
    public static CategorieArticle fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return CategorieArticle.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}

