package com.osm.inventory_service.controller;

import com.osm.inventory_service.dto.ArticleSecDto;
import com.osm.inventory_service.entity.ArticleSec;
import com.osm.inventory_service.service.ArticleSecService;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventaire/articles")
public class ArticleSecController extends BaseControllerImpl<ArticleSec, ArticleSecDto, ArticleSecDto> {

    private final ArticleSecService articleService;

    @Autowired
    public ArticleSecController(BaseService<ArticleSec, ArticleSecDto, ArticleSecDto> baseService, ModelMapper modelMapper, ArticleSecService articleService) {
        super(baseService, modelMapper);
        this.articleService = articleService;
    }

    @GetMapping
    public ResponseEntity<List<ArticleSecDto>> getAllArticles() {
        List<ArticleSecDto> articles = articleService.getAllArticles();
        System.out.println("Returning " + articles.size() + " articles");
        return ResponseEntity.ok(articles);
    }
    @GetMapping("/actifs")
    public ResponseEntity<List<ArticleSecDto>> getActiveArticles() {
        return ResponseEntity.ok(articleService.getAllActiveArticles());
    }


    @GetMapping("/{id}")
    public ResponseEntity<ArticleSecDto> getArticleById(@PathVariable UUID id) {
        return ResponseEntity.ok(articleService.getArticleById(id));
    }

    @PostMapping("/create")
    public ResponseEntity<ArticleSecDto> createArticle(@RequestBody ArticleSecDto articleDto) {
        return new ResponseEntity<>(articleService.createArticle(articleDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArticleSecDto> updateArticle(@PathVariable UUID id, @RequestBody ArticleSecDto articleDto) {
        return ResponseEntity.ok(articleService.updateArticle(id, articleDto));
    }

    @PutMapping("/{id}/activer")
    public ResponseEntity<ArticleSecDto> activerArticle(@PathVariable UUID id) {
        return ResponseEntity.ok(articleService.activerArticle(id));
    }

    @PutMapping("/{id}/desactiver")
    public ResponseEntity<ArticleSecDto> desactiverArticle(@PathVariable UUID id) {
        return ResponseEntity.ok(articleService.desactiverArticle(id));
    }

    @Override
    protected String getResourceName() {
        return "ArticleSec";
    }
}