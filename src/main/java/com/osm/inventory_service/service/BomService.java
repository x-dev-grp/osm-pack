package com.osm.inventory_service.service;

import com.osm.inventory_service.dto.BOMDto;
import com.osm.inventory_service.dto.BomLineDto;
import com.osm.inventory_service.entity.ArticleSec;
import com.osm.inventory_service.entity.BOM;
import com.osm.inventory_service.entity.BomLine;
import com.osm.inventory_service.entity.Product;
import com.osm.inventory_service.repository.ArticleSecRepository;
import com.osm.inventory_service.repository.BomRepository;
import com.osm.inventory_service.repository.ProductRepository;
import com.xdev.xdevbase.qr.CodeGenerator;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BomService extends BaseServiceImpl<BOM, BOMDto, BOMDto> {

    private final BomRepository bomRepository;
    private final ProductRepository productRepository;
    private final ArticleSecRepository articleRepository;

    @Autowired
    public BomService(BaseRepository<BOM> repository, BomRepository bomRepository, ProductRepository productRepository,
                      CodeGenerator codeGenerator, ArticleSecRepository articleRepository, ModelMapper modelMapper) {
        super(repository, codeGenerator, modelMapper);
        this.bomRepository = bomRepository;
        this.productRepository = productRepository;
        this.articleRepository = articleRepository;
    }

    @Override
    public Class<BOM> getEntityClass() {
        return BOM.class;
    }

    @Transactional(readOnly = true)
    public BOMDto getBomById(UUID id) {
        BOM bom = bomRepository.findById(id).orElseThrow(() -> new RuntimeException("BOM non trouvee avec l'id : " + id));
        return convertToDto(bom);
    }

    @Transactional(readOnly = true)
    public List<BOMDto> getBomsByProduct(UUID productId) {
        return bomRepository.findByProductId(productId).stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Transactional
    public BOMDto createBom(BOMDto bomDto) {
        Product product = productRepository.findById(bomDto.getProductId())
                .orElseThrow(() -> new RuntimeException("Produit non trouve avec l'id : " + bomDto.getProductId()));
        int count = bomRepository.findByProductId(bomDto.getProductId()).size();
        String version = "V" + (count + 1);

        BOM bom = new BOM();
        bom.setProduct(product);
        bom.setVersion(version);

        List<BomLine> lines = bomDto.getLines().stream().map(lineDto -> {
            ArticleSec article = articleRepository.findById(lineDto.getArticleId())
                    .orElseThrow(() -> new RuntimeException("Article non trouve avec l'id : " + lineDto.getArticleId()));
            BomLine line = new BomLine();
            line.setBom(bom);
            line.setArticle(article);
            line.setQuantity(lineDto.getQuantity());
            line.setUnitOfMeasure(article.getUm());
            return line;
        }).collect(Collectors.toList());
        bom.setLines(lines);

        BOM saved = bomRepository.save(bom);
        return convertToDto(saved);
    }

    @Transactional
    public BOMDto updateBom(UUID id, BOMDto bomDto) {
        BOM bom = bomRepository.findById(id).orElseThrow(() -> new RuntimeException("BOM non trouvee avec l'id : " + id));
        if (!bom.getProduct().getId().equals(bomDto.getProductId())) {
            Product newProduct = productRepository.findById(bomDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Produit non trouve avec l'id : " + bomDto.getProductId()));
            bom.setProduct(newProduct);
        }
        bom.setVersion(bomDto.getVersion());
        bom.getLines().clear();

        List<BomLine> newLines = bomDto.getLines().stream().map(lineDto -> {
            ArticleSec article = articleRepository.findById(lineDto.getArticleId())
                    .orElseThrow(() -> new RuntimeException("Article non trouve avec l'id : " + lineDto.getArticleId()));
            BomLine line = new BomLine();
            line.setBom(bom);
            line.setArticle(article);
            line.setQuantity(lineDto.getQuantity());
            line.setUnitOfMeasure(article.getUm());
            return line;
        }).collect(Collectors.toList());
        bom.getLines().addAll(newLines);
        BOM updated = bomRepository.save(bom);
        return convertToDto(updated);
    }

    @Transactional(readOnly = true)
    public List<BOMDto> getAllBoms() {
        return bomRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Transactional
    public void deleteBom(UUID id) {
        if (!bomRepository.existsById(id)) {
            throw new RuntimeException("BOM non trouvee avec l'id : " + id);
        }
        bomRepository.deleteById(id);
    }

    private BOMDto convertToDto(BOM bom) {
        BOMDto dto = new BOMDto();
        dto.setId(bom.getId());
        if (bom.getProduct() != null) {
            dto.setProductId(bom.getProduct().getId());
            dto.setProductName(bom.getProduct().getName());
        } else {
            dto.setProductName("Produit non assigné");
        }
        dto.setVersion(bom.getVersion());

        List<BomLineDto> lineDtos = bom.getLines().stream().map(line -> {
            BomLineDto lineDto = new BomLineDto();
            lineDto.setId(line.getId());
            if (line.getArticle() != null) {
                lineDto.setArticleId(line.getArticle().getId());
                lineDto.setArticleName(line.getArticle().getNom());
            } else {
                lineDto.setArticleName("Article inconnu");
            }
            lineDto.setQuantity(line.getQuantity());
            lineDto.setUnitOfMeasure(line.getUnitOfMeasure());
            return lineDto;
        }).collect(Collectors.toList());
        dto.setLines(lineDtos);
        return dto;
    }
}
