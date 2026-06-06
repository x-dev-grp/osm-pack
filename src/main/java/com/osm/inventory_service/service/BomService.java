package com.osm.inventory_service.service;

import com.osm.inventory_service.Enum.ProduitFinalType;
import com.osm.inventory_service.dto.BOMDto;
import com.osm.inventory_service.dto.BomLineDto;
import com.osm.inventory_service.exception.InventoryBusinessException;
import com.osm.inventory_service.exception.ResourceNotFoundException;
import com.osm.inventory_service.entity.ArticleSec;
import com.osm.inventory_service.entity.BOM;
import com.osm.inventory_service.entity.BomLine;
import com.osm.inventory_service.entity.ProduitFinal;
import com.osm.inventory_service.repository.ArticleSecRepository;
import com.osm.inventory_service.repository.BomRepository;
import com.osm.inventory_service.repository.ProduitFinalRepository;
import com.xdev.xdevbase.config.TenantContext;
import com.xdev.xdevbase.qr.CodeGenerator;
import com.xdev.xdevbase.qr.model.QrCodeInfo;
import com.xdev.xdevbase.qr.model.QrResolveResponse;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BomService extends BaseServiceImpl<BOM, BOMDto, BOMDto> {

    private final BomRepository bomRepository;
    private final ProduitFinalRepository produitFinalRepository;
    private final ArticleSecRepository articleRepository;

    @Autowired
    public BomService(BaseRepository<BOM> repository,
                      BomRepository bomRepository,
                      ProduitFinalRepository produitFinalRepository,
                      CodeGenerator codeGenerator,
                      ArticleSecRepository articleRepository,
                      ModelMapper modelMapper) {
        super(repository, codeGenerator, modelMapper);
        this.bomRepository = bomRepository;
        this.produitFinalRepository = produitFinalRepository;
        this.articleRepository = articleRepository;
    }

    @Override
    public Class<BOM> getEntityClass() {
        return BOM.class;
    }

    @Transactional(readOnly = true)
    public BOMDto getBomById(UUID id) {
        BOM bom = bomRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("BOM non trouvee avec l'id : " + id));
        return convertToDto(bom);
    }

    @Transactional(readOnly = true)
    public List<BOMDto> getBomsByProduct(UUID productId) {
        return bomRepository.findByProduitFinalId(productId).stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BOMDto getActiveBomForProduct(UUID productId) {
        return bomRepository.findFirstByProduitFinalIdAndActiveTrue(productId)
                .map(this::convertToDto)
                .orElse(null);
    }

    @Transactional
    public BOMDto activateBom(UUID bomId) {
        BOM bom = bomRepository.findById(bomId)
                .orElseThrow(() -> new ResourceNotFoundException("BOM non trouvee avec l'id : " + bomId));
        if (bom.getProduitFinal() == null) {
            throw new InventoryBusinessException("BOM_NO_PRODUCT", "Impossible d'activer une nomenclature sans produit");
        }
        UUID productId = bom.getProduitFinal().getId();
        List<BOM> siblings = bomRepository.findByProduitFinalId(productId);
        for (BOM other : siblings) {
            other.setActive(other.getId().equals(bomId));
        }
        return convertToDto(bomRepository.saveAll(siblings).stream()
                .filter(b -> b.getId().equals(bomId))
                .findFirst()
                .orElse(bom));
    }

    @Transactional
    public BOMDto createBom(BOMDto bomDto) {
        validateBomDto(bomDto, true);

        ProduitFinal produitFinal = produitFinalRepository.findById(bomDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouve avec l'id : " + bomDto.getProductId()));
        if (produitFinal.getType() == ProduitFinalType.VRAC) {
            throw new InventoryBusinessException("BOM_VRAC_NOT_ALLOWED", "Une nomenclature emballage n'est pas applicable aux produits VRAC");
        }

        int count = bomRepository.findByProduitFinalId(bomDto.getProductId()).size();
        String version = "V" + (count + 1);

        BOM bom = new BOM();
        bom.setProduitFinal(produitFinal);
        bom.setVersion(version);
        bom.setLines(buildLines(bom, bomDto.getLines()));

        boolean shouldActivate = Boolean.TRUE.equals(bomDto.getActive()) || count == 0;
        if (shouldActivate) {
            List<BOM> existing = bomRepository.findByProduitFinalId(produitFinal.getId());
            existing.forEach(other -> other.setActive(false));
            if (!existing.isEmpty()) {
                bomRepository.saveAll(existing);
            }
            bom.setActive(true);
        }

        BOM saved = bomRepository.save(bom);
        QrCodeInfo qrInfo = generateQrInfo("BOM", saved.getId());
        BOMDto result = convertToDto(saved);
        result.setPublicCode(qrInfo.getPublicCode());
        result.setQrUrl(qrInfo.getQrUrl());
        result.setQrImageBase64(qrInfo.getQrImageBase64());
        return result;
    }

    @Transactional
    public BOMDto updateBom(UUID id, BOMDto bomDto) {
        BOM bom = bomRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("BOM non trouvee avec l'id : " + id));
        validateBomDto(bomDto, false);

        if (bomDto.getProductId() == null) {
            throw new InventoryBusinessException("BOM_PRODUCT_REQUIRED", "Le produit fini est obligatoire");
        }

        ProduitFinal produitFinal = produitFinalRepository.findById(bomDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouve avec l'id : " + bomDto.getProductId()));
        if (produitFinal.getType() == ProduitFinalType.VRAC) {
            throw new InventoryBusinessException("BOM_VRAC_NOT_ALLOWED", "Une nomenclature emballage n'est pas applicable aux produits VRAC");
        }

        bom.setProduitFinal(produitFinal);
        if (bomDto.getVersion() != null && !bomDto.getVersion().isBlank()) {
            bom.setVersion(bomDto.getVersion());
        }
        bom.getLines().clear();
        bom.getLines().addAll(buildLines(bom, bomDto.getLines()));

        if (Boolean.TRUE.equals(bomDto.getActive())) {
            List<BOM> siblings = bomRepository.findByProduitFinalId(produitFinal.getId());
            for (BOM other : siblings) {
                other.setActive(other.getId().equals(bom.getId()));
            }
            if (!siblings.isEmpty()) {
                return convertToDto(bomRepository.saveAll(siblings).stream()
                        .filter(b -> b.getId().equals(bom.getId()))
                        .findFirst()
                        .orElse(bom));
            }
            bom.setActive(true);
        } else if (Boolean.FALSE.equals(bomDto.getActive())) {
            bom.setActive(false);
        }

        BOM updated = bomRepository.save(bom);
        return convertToDto(updated);
    }

    @Transactional(readOnly = true)
    public List<BOMDto> getAllBoms() {
        return bomRepository.findAll().stream()
                .sorted(java.util.Comparator
                        .comparing(BOM::isActive).reversed()
                        .thenComparing(BOM::getCreatedDate, java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteBom(UUID id) {
        BOM bom = bomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BOM non trouvee avec l'id : " + id));
        if (bom.isActive()) {
            throw new InventoryBusinessException("BOM_ACTIVE_DELETE", "Desactivez la nomenclature avant de la supprimer");
        }
        bomRepository.deleteById(id);
    }

    private void validateBomDto(BOMDto bomDto, boolean creating) {
        if (bomDto.getProductId() == null) {
            throw new InventoryBusinessException("BOM_PRODUCT_REQUIRED", "Le produit fini est obligatoire");
        }
        if (bomDto.getLines() == null || bomDto.getLines().isEmpty()) {
            throw new InventoryBusinessException("BOM_LINES_REQUIRED", "La nomenclature doit contenir au moins une ligne");
        }

        Set<UUID> articleIds = new HashSet<>();
        for (BomLineDto lineDto : bomDto.getLines()) {
            if (lineDto.getArticleId() == null) {
                throw new InventoryBusinessException("BOM_LINE_ARTICLE_REQUIRED", "Chaque ligne doit referencer un article");
            }
            if (!articleIds.add(lineDto.getArticleId())) {
                throw new InventoryBusinessException("BOM_DUPLICATE_ARTICLE", "Un article ne peut apparaitre qu'une seule fois dans la nomenclature");
            }
            if (lineDto.getQuantity() <= 0) {
                throw new InventoryBusinessException("BOM_LINE_QTY_INVALID", "La quantite par unite doit etre positive");
            }
            ArticleSec article = articleRepository.findById(lineDto.getArticleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Article non trouve avec l'id : " + lineDto.getArticleId()));
            if (!Boolean.TRUE.equals(article.getActif())) {
                throw new InventoryBusinessException(
                        "BOM_INACTIVE_ARTICLE",
                        "L'article " + article.getNom() + " est inactif et ne peut pas etre utilise"
                );
            }
        }
    }

    private List<BomLine> buildLines(BOM bom, List<BomLineDto> lineDtos) {
        return lineDtos.stream().map(lineDto -> {
            ArticleSec article = articleRepository.findById(lineDto.getArticleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Article non trouve avec l'id : " + lineDto.getArticleId()));
            BomLine line = new BomLine();
            line.setBom(bom);
            line.setArticle(article);
            line.setQuantity(lineDto.getQuantity());
            line.setUnitOfMeasure(article.getUm());
            return line;
        }).collect(Collectors.toList());
    }

    private BOMDto convertToDto(BOM bom) {
        BOMDto dto = new BOMDto();
        dto.setId(bom.getId());
        if (bom.getProduitFinal() != null) {
            dto.setProductId(bom.getProduitFinal().getId());
            dto.setProductName(bom.getProduitFinal().getName());
        } else {
            dto.setProductName("Produit non assigne");
        }
        dto.setVersion(bom.getVersion());
        dto.setActive(bom.isActive());
        dto.setPublicCode(bom.getQrHex());
        dto.setQrImageBase64(bom.getQrImageBase64());

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

    @Override
    protected String getEntityType() {
        return "BOM";
    }

    @Override
    protected String getLabel(BOM entity) {
        String product = entity.getProduitFinal() != null ? entity.getProduitFinal().getName() : "Produit";
        return product + " " + entity.getVersion();
    }

    @Override
    protected String getStatus(BOM entity) {
        return entity.isActive() ? "ACTIVE" : "INACTIVE";
    }

    @Override
    protected String getMobileRoute() {
        return "/bom/detail";
    }

    @Override
    protected String getWebRoute(BOM entity) {
        if (entity == null || entity.getId() == null) {
            return "/stock/boms";
        }
        return "/stock/boms/" + entity.getId();
    }

    @Override
    protected Object getData(BOM entity) {
        return convertToDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public QrResolveResponse resolve(String publicCode) {
        if (publicCode == null || publicCode.isBlank()) {
            throw new IllegalArgumentException("Le code est obligatoire");
        }

        String normalizedCode = publicCode.trim().toUpperCase(Locale.ROOT);
        UUID tenantId = TenantContext.getCurrentTenant();

        Optional<BOM> entity = (tenantId == null)
                ? bomRepository.findByQrHex(normalizedCode)
                : bomRepository.findByQrHexAndTenantIdAndIsDeletedFalse(normalizedCode, tenantId);

        if (entity.isEmpty() && tenantId != null) {
            entity = bomRepository.findByQrHex(normalizedCode);
        }

        return entity.map(bom -> {
                    QrResolveResponse response = new QrResolveResponse();
                    response.setEntityType(getEntityType());
                    response.setPublicCode(normalizedCode);
                    response.setEntityId(bom.getId().toString());
                    response.setLabel(getLabel(bom));
                    response.setStatus(getStatus(bom));
                    response.setMobileRoute(getMobileRoute());
                    response.setWebRoute(getWebRoute(bom));
                    response.setData(getData(bom));
                    return response;
                })
                .orElseThrow(() -> new EntityNotFoundException("BOM non trouvee pour le code : " + publicCode));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<QrResolveResponse> searchByCode(String code) {
        try {
            return Optional.ofNullable(resolve(code));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
