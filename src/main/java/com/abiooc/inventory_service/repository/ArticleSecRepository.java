package com.abiooc.inventory_service.repository;

import com.abiooc.inventory_service.Enum.CategorieArticle;
import com.abiooc.inventory_service.Enum.UniteMesure;
import com.abiooc.inventory_service.entity.ArticleSec;
import com.abiooc.inventory_service.entity.Fournisseur;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArticleSecRepository extends BaseRepository<ArticleSec> {

    boolean existsByNomAndFournisseur(String nom, Fournisseur fournisseur);

    @Query("SELECT a FROM ArticleSec a WHERE " +
            "(:nom IS NULL OR LOWER(a.nom) LIKE LOWER(CONCAT('%', :nom, '%'))) AND " +
            "(:categorie IS NULL OR a.categorie = :categorie) AND " +
            "(:fournisseurId IS NULL OR a.fournisseur.id = :fournisseurId) AND " +
            "(:actif IS NULL OR a.actif = :actif)")
    List<ArticleSec> rechercherArticles(
            @Param("nom") String nom,
            @Param("categorie") CategorieArticle categorie,
            @Param("fournisseurId") UUID fournisseurId,
            @Param("actif") Boolean actif);

}