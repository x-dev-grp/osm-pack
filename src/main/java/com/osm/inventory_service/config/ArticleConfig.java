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
        @JsonSubTypes.Type(value = ConsommableConfig.class, name = "CONSOMMABLE"),
        @JsonSubTypes.Type(value = MatierePremiereConfig.class, name = "MATIERE_PREMIERE"),
        @JsonSubTypes.Type(value = AccessoireConfig.class, name = "ACCESSOIRE")
})
public interface ArticleConfig {
}