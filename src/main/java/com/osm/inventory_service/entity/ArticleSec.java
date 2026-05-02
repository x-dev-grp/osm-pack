package com.osm.inventory_service.entity;

import com.osm.inventory_service.Enum.CategorieArticle;
import com.osm.inventory_service.config.*;
import com.xdev.xdevbase.entities.BaseEntity;
import com.xdev.xdevbase.models.UniteMesure;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.envers.Audited;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "articles_secs")
@Data
@Getter
@Setter
@Audited
@NoArgsConstructor
@AllArgsConstructor
public class ArticleSec extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategorieArticle categorie;

    @ManyToOne
    @JoinColumn(name = "fournisseur_id")
    private Fournisseur fournisseur;

    private Integer stockMinimum = 0;
    private Integer stockMaximum = 0;
    private Boolean actif = true;

    @Enumerated(EnumType.STRING)
    private UniteMesure um;

    // Dans com.osm.inventory_service.entity.ArticleSec
    @Column(name = "sku_id")
    private UUID skuId;

    @Column(name = "lot_created_date")
    private LocalDateTime lotCreatedDate;

    @Column(name = "lot_ddm")
    private LocalDate lotDdm;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private ArticleConfig configuration;
    public void validateConfiguration() {
        if (configuration == null) {
            throw new IllegalArgumentException("La configuration ne peut être nulle");
        }
        CategorieArticle configCategory = extractCategoryFromConfig(configuration);
        if (configCategory != this.categorie) {
            throw new IllegalArgumentException(
                    "Incohérence : la configuration est de type " + configCategory +
                            " mais l'article est catégorisé " + this.categorie
            );
        }
        validateBusinessRules();
    }

    private CategorieArticle extractCategoryFromConfig(ArticleConfig config) {
        if (config instanceof UniteConfig) return CategorieArticle.UNITE;
        if (config instanceof ColisConfig) return CategorieArticle.COLIS;
        if (config instanceof PaletteConfig) return CategorieArticle.PALETTE;
        if (config instanceof EmballageConfig) return CategorieArticle.EMBALLAGE;
        if (config instanceof ConsommableConfig) return CategorieArticle.CONSOMMABLE;
        if (config instanceof MatierePremiereConfig) return CategorieArticle.MATIERE_PREMIERE;
        if (config instanceof AccessoireConfig) return CategorieArticle.ACCESSOIRE;
        throw new IllegalStateException("Type de configuration inconnu");
    }

    private void validateBusinessRules() {
        switch (this.categorie) {
            case UNITE:
                UniteConfig u = (UniteConfig) configuration;
                if (u.getVolumeMl() <= 0) throw new IllegalArgumentException("Volume unitaire invalide");
                break;
            case COLIS:
                ColisConfig c = (ColisConfig) configuration;
                if (c.getUnitsPerColis() <= 0) throw new IllegalArgumentException("Nombre d'unités par colis invalide");
                if (c.getDimensions() == null) throw new IllegalArgumentException("Dimensions du colis requises");
                break;
            case PALETTE:
                PaletteConfig p = (PaletteConfig) configuration;
                if (p.getColisPerLayer() <= 0 || p.getNumberOfLayers() <= 0)
                    throw new IllegalArgumentException("Configuration palette incomplète");
                if (p.getColisId() == null)
                    throw new IllegalArgumentException("La palette doit référencer un colis");
                break;
            case EMBALLAGE:
                EmballageConfig e = (EmballageConfig) configuration;
                if (e.getDimensions() == null && e.getPoidsGrammes() == null)
                    throw new IllegalArgumentException("Emballage doit avoir au moins dimensions ou poids");
                break;
            default:
                break;
        }
    }
}