package com.abiooc.inventory_service.controller;

import com.abiooc.inventory_service.Enum.CategorieArticle;
import com.abiooc.inventory_service.Enum.UniteMesure;
import com.abiooc.inventory_service.dto.ArticleSecDto;
import com.abiooc.inventory_service.entity.ArticleSec;
import com.abiooc.inventory_service.service.ArticleSecService;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventaire/articles")
@CrossOrigin(origins = "*")
public class ArticleSecController extends BaseControllerImpl<ArticleSec, ArticleSecDto, ArticleSecDto> {

    private final ArticleSecService articleService;

    @Autowired
    public ArticleSecController(BaseService<ArticleSec, ArticleSecDto, ArticleSecDto> baseService, ModelMapper modelMapper, ArticleSecService articleService) {
        super(baseService, modelMapper);
        this.articleService = articleService;
    }

    @GetMapping
    public ResponseEntity<List<ArticleSecDto>> getAllArticles() {
        return ResponseEntity.ok(articleService.getAllArticles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleSecDto> getArticleById(@PathVariable UUID id) {
        return ResponseEntity.ok(articleService.getArticleById(id));
    }


    @GetMapping("/recherche")
    public ResponseEntity<List<ArticleSecDto>> rechercherArticles(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) CategorieArticle categorie,
            @RequestParam(required = false) UUID fournisseurId,
            @RequestParam(required = false) Boolean actif) {
        return ResponseEntity.ok(articleService.rechercherArticles(nom, categorie, fournisseurId, actif));
    }

    @PostMapping
    public ResponseEntity<ArticleSecDto> createArticle(@RequestBody ArticleSecDto articleDto) {
        return new ResponseEntity<>(articleService.createArticle(articleDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArticleSecDto> updateArticle(@PathVariable UUID id, @RequestBody ArticleSecDto articleDto) {
        return ResponseEntity.ok(articleService.updateArticle(id, articleDto));
    }

    @PatchMapping("/{id}/activer")
    public ResponseEntity<ArticleSecDto> activerArticle(@PathVariable UUID id) {
        return ResponseEntity.ok(articleService.activerArticle(id));
    }

    @PatchMapping("/{id}/desactiver")
    public ResponseEntity<ArticleSecDto> desactiverArticle(@PathVariable UUID id) {
        return ResponseEntity.ok(articleService.desactiverArticle(id));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable UUID id) {
        articleService.deleteArticle(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    protected String getResourceName() {
        return "ArticleSec";
    }
}