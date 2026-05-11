package com.osm.inventory_service.service;

import com.xdev.xdevbase.dtos.AuditDto;
import com.osm.inventory_service.entity.ArticleSec;
import com.osm.inventory_service.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuditService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<AuditDto> getAllAudits() {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        List<AuditDto> result = new ArrayList<>();

        List<Class<?>> auditedClasses = List.of(
                ArticleSec.class,
                BOM.class,
                BomLine.class,
                BonCommande.class,
                EmplacementStock.class,
                Fournisseur.class,
                LigneBonCommande.class,
                LigneConditionnement.class,
                MouvementStockSec.class,
                Product.class,
                StockSec.class
        );


        auditedClasses.forEach(clazz -> {
            List<Object[]> revisions = auditReader.createQuery()
                    .forRevisionsOfEntity(clazz, false, true)
                    .addProjection(AuditEntity.id())
                    .addProjection(AuditEntity.property("createdBy"))
                    .addProjection(AuditEntity.property("createdDate"))
                    .addProjection(AuditEntity.property("lastModifiedBy"))
                    .addProjection(AuditEntity.property("lastModifiedDate"))
                    .addProjection(AuditEntity.revisionNumber())
                    .addProjection(AuditEntity.revisionType())
                    .addOrder(AuditEntity.revisionNumber().desc())
                    .getResultList();
            revisions.forEach(row -> {
                AuditDto dto = new AuditDto();
                dto.setEntityName(clazz.getSimpleName());
                Object id = row[0];
                dto.setId(id != null ? id.toString() : null);
                dto.setCreatedBy((String) row[1]);
                dto.setCreatedDate((LocalDateTime) row[2]);
                dto.setLastModifiedBy((String) row[3]);
                dto.setLastModifiedDate((LocalDateTime) row[4]);
                Number revNum = (Number) row[5];
                dto.setRevision(revNum != null ? revNum.intValue() : null);
                RevisionType revType = (RevisionType) row[6];
                dto.setRevisionType(revType != null ? revType.name() : null);
                result.add(dto);
            });
        });
        
        result.sort((a, b) -> {
            Integer revA = a.getRevision();
            Integer revB = b.getRevision();
            if (revA == null && revB == null) return 0;
            if (revA == null) return 1;
            if (revB == null) return -1;
            return revB.compareTo(revA);
        });
        
        return result;
    }
}
