package com.osm.inventory_service.service;

import com.osm.inventory_service.dto.BOMDto;
import com.osm.inventory_service.dto.BomLineDto;
import com.osm.inventory_service.entity.BOM;
import com.osm.inventory_service.entity.BomLine;
import com.osm.inventory_service.entity.ArticleSec;
import com.osm.inventory_service.entity.SKU;
import com.osm.inventory_service.repository.BomRepository;
import com.osm.inventory_service.repository.ArticleSecRepository;
import com.osm.inventory_service.repository.SKURepository;
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
    private final SKURepository skuRepository;
    private final ArticleSecRepository articleRepository;

    @Autowired
    public BomService(BaseRepository<BOM> repository,
                      BomRepository bomRepository,
                      SKURepository skuRepository,
                      ArticleSecRepository articleRepository,
                      ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.bomRepository = bomRepository;
        this.skuRepository = skuRepository;
        this.articleRepository = articleRepository;

    }

    @Override
    public Class<BOM> getEntityClass() {
        return BOM.class;
    }

    @Transactional(readOnly = true)
    public BOMDto getBomById(UUID id) {
        BOM bom = bomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BOM non trouvée avec l'id : " + id));
        return convertToDto(bom);
    }
    @Transactional(readOnly = true)
    public List<BOMDto> getBomsBySku(UUID skuId) {
        return bomRepository.findBySkuId(skuId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BOMDto createBom(BOMDto bomDto) {
        SKU sku = skuRepository.findById(bomDto.getSkuId())
                .orElseThrow(() -> new RuntimeException("SKU non trouvé avec l'id : " + bomDto.getSkuId()));

        BOM bom = new BOM();
        bom.setSku(sku);
        bom.setVersion(bomDto.getVersion());

        List<BomLine> lines = bomDto.getLines().stream()
                .map(lineDto -> {
                    ArticleSec article = articleRepository.findById(lineDto.getArticleId())
                            .orElseThrow(() -> new RuntimeException("Article non trouvé avec l'id : " + lineDto.getArticleId()));
                    BomLine line = new BomLine();
                    line.setBom(bom);
                    line.setArticle(article);
                    line.setQuantity(lineDto.getQuantity());
                        line.setUnitOfMeasure(article.getUm());
                    return line;
                })
                .collect(Collectors.toList());
        bom.setLines(lines);

        BOM saved = bomRepository.save(bom);
        return convertToDto(saved);
    }

    @Transactional
    public BOMDto updateBom(UUID id, BOMDto bomDto) {
        BOM bom = bomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BOM non trouvée avec l'id : " + id));
        if (!bom.getSku().getId().equals(bomDto.getSkuId())) {
            SKU newSku = skuRepository.findById(bomDto.getSkuId())
                    .orElseThrow(() -> new RuntimeException("SKU non trouvé avec l'id : " + bomDto.getSkuId()));
            bom.setSku(newSku);
        }
        bom.setVersion(bomDto.getVersion());
        bom.getLines().clear();

        List<BomLine> newLines = bomDto.getLines().stream()
                .map(lineDto -> {
                    ArticleSec article = articleRepository.findById(lineDto.getArticleId())
                            .orElseThrow(() -> new RuntimeException("Article non trouvé avec l'id : " + lineDto.getArticleId()));
                    BomLine line = new BomLine();
                    line.setBom(bom);
                    line.setArticle(article);
                    line.setQuantity(lineDto.getQuantity());
                    line.setUnitOfMeasure(article.getUm());
                    return line;
                })
                .collect(Collectors.toList());
        bom.getLines().addAll(newLines);
        BOM updated = bomRepository.save(bom);
        return convertToDto(updated);
    }
    @Transactional(readOnly = true)
    public List<BOMDto> getAllBoms() {
        return bomRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private BOMDto convertToDto(BOM bom) {
        BOMDto dto = new BOMDto();
        dto.setId(bom.getId());
        dto.setSkuId(bom.getSku().getId());
        dto.setSkuCode(bom.getSku().getCode());
        dto.setVersion(bom.getVersion());

        List<BomLineDto> lineDtos = bom.getLines().stream()
                .map(line -> {
                    BomLineDto lineDto = new BomLineDto();
                    lineDto.setId(line.getId());
                    lineDto.setArticleId(line.getArticle().getId());
                    lineDto.setArticleName(line.getArticle().getNom());
                    lineDto.setQuantity(line.getQuantity());
                    lineDto.setUnitOfMeasure(line.getUnitOfMeasure());
                    return lineDto;
                })
                .collect(Collectors.toList());
        dto.setLines(lineDtos);
        return dto;
    }
}