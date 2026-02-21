package com.abiooc.inventory_service.controller;

import com.abiooc.inventory_service.entity.ArticleSec;
import com.abiooc.inventory_service.service.ArticleSecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventaire/articles-secs")
@CrossOrigin(origins = "*")
public class ArticleSecController {

    @Autowired
    private ArticleSecService articleSecService;

    @GetMapping
    public List<ArticleSec> getAllArticles() {
        return articleSecService.getAllArticles();
    }

    @GetMapping("/{id}")
    public ArticleSec getArticleById(@PathVariable Long id) {
        return articleSecService.getArticleById(id);
    }

    @GetMapping("/sku/{sku}")
    public ArticleSec getArticleBySku(@PathVariable String sku) {
        return articleSecService.getArticleBySku(sku);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleSec createArticle(@RequestBody ArticleSec article) {
        return articleSecService.createArticle(article);
    }

    @PutMapping("/{id}")
    public ArticleSec updateArticle(@PathVariable Long id, @RequestBody ArticleSec article) {
        return articleSecService.updateArticle(id, article);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteArticle(@PathVariable Long id) {
        articleSecService.deleteArticle(id);
    }
}