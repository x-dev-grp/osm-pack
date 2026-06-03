package com.osm.inventory_service.controller;

import com.osm.inventory_service.Enum.CategorieArticle;
import com.osm.inventory_service.dto.ArticleSecDto;
import com.osm.inventory_service.entity.ArticleSec;
import com.osm.inventory_service.service.ArticleSecService;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.models.UniteMesure;
import com.xdev.xdevbase.qr.model.QrResolveResponse;
import com.xdev.xdevbase.services.BaseService;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Arrays;

@RestController
@RequestMapping("/api/inventaire/articles")
public class ArticleSecController extends BaseControllerImpl<ArticleSec, ArticleSecDto, ArticleSecDto> {

    private final ArticleSecService articleService;

    @Autowired
    public ArticleSecController(BaseService<ArticleSec, ArticleSecDto, ArticleSecDto> baseService,
                                ModelMapper modelMapper,
                                ArticleSecService articleService) {
        super(baseService, modelMapper);
        this.articleService = articleService;
    }

    @GetMapping
    public ResponseEntity<?> getAllArticles(@RequestParam(required = false) CategorieArticle categorie) {
        try {
            List<ArticleSecDto> articles;
            if (categorie != null) {
                articles = articleService.getArticlesByCategorie(categorie);
            } else {
                articles = articleService.getAllArticles();
            }
            return ResponseEntity.ok(attachPermittedActions(articles));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/actifs")
    public ResponseEntity<?> getActiveArticles() {
        try {
            List<ArticleSecDto> activeArticles = articleService.getAllActiveArticles();
            return ResponseEntity.ok(attachPermittedActions(activeArticles));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/unites-mesure")
    public ResponseEntity<List<Map<String, String>>> getUnitesMesure() {
        List<Map<String, String>> unites = Arrays.stream(UniteMesure.values())
                .map(unite -> Map.of(
                        "value", unite.name(),
                        "label", formatUniteMesureLabel(unite)
                ))
                .toList();

        return ResponseEntity.ok(unites);
    }

    private String formatUniteMesureLabel(UniteMesure unite) {
        return switch (unite) {
            case KG -> "Kilogramme (KG)";
            case LITRE -> "Litre";
            case UNITE -> "Unite";
            case METRE -> "Metre";
        };
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getArticleById(@PathVariable UUID id) {
        try {
            ArticleSecDto article = articleService.getArticleById(id);
            return ResponseEntity.ok(attachPermittedActions(article));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createArticle(@RequestBody ArticleSecDto articleDto) {
        try {
            ArticleSecDto created = articleService.createArticle(articleDto);
            return new ResponseEntity<>(attachPermittedActions(created), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateArticle(@PathVariable UUID id, @RequestBody ArticleSecDto articleDto) {
        try {
            ArticleSecDto updated = articleService.updateArticle(id, articleDto);
            return ResponseEntity.ok(attachPermittedActions(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/activer")
    public ResponseEntity<?> activerArticle(@PathVariable UUID id) {
        try {
            ArticleSecDto activated = articleService.activerArticle(id);
            return ResponseEntity.ok(attachPermittedActions(activated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/desactiver")
    public ResponseEntity<?> desactiverArticle(@PathVariable UUID id) {
        try {
            ArticleSecDto deactivated = articleService.desactiverArticle(id);
            return ResponseEntity.ok(attachPermittedActions(deactivated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Override
    protected String getResourceName() {
        return "ArticleSec";
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        try {
            QrResolveResponse response = getBaseService().resolve(publicCode);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/qr-image")
    public ResponseEntity<byte[]> getQrImage(@PathVariable UUID id) {
        ArticleSec entity = articleService.getArticleEntityById(id);
        byte[]  image = articleService.generateQrImage(entity.getQrHex());
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(image);
    }



}
