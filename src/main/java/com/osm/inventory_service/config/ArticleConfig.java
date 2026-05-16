package com.osm.inventory_service.config;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "configType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = UniteConfig.class, name = "UNITE"),
        @JsonSubTypes.Type(value = ColisConfig.class, name = "COLIS"),
        @JsonSubTypes.Type(value = PaletteConfig.class, name = "PALETTE"),
        @JsonSubTypes.Type(value = EmballageConfig.class, name = "EMBALLAGE"),
        @JsonSubTypes.Type(value = ConsommableConfig.class, name = "CONSOMMABLE")
})
public interface ArticleConfig {
}