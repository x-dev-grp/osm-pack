package com.osm.inventory_service.controller;



import com.osm.inventory_service.dto.AuditDto;
import com.osm.inventory_service.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/inventaire/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<AuditDto>> getAllAudits() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(auditService.getAllAudits());
    }
}