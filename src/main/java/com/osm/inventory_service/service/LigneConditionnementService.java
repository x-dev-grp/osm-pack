package com.osm.inventory_service.service;

import com.osm.inventory_service.Enum.Statue;
import com.osm.inventory_service.dto.LigneConditionnementDto;
import com.osm.inventory_service.entity.LigneConditionnement;
import com.osm.inventory_service.repository.LigneConditionnementRepository;
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
public class LigneConditionnementService extends BaseServiceImpl<LigneConditionnement, LigneConditionnementDto, LigneConditionnementDto> {

    private final LigneConditionnementRepository ligneRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public LigneConditionnementService(BaseRepository<LigneConditionnement> repository, LigneConditionnementRepository ligneRepository, ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.ligneRepository = ligneRepository;
        this.modelMapper = modelMapper;
    }

    public List<LigneConditionnementDto> getAllLignes() {
        return ligneRepository.findAll().stream()
                .map(ligne -> modelMapper.map(ligne, LigneConditionnementDto.class))
                .collect(Collectors.toList());
    }


    public LigneConditionnementDto getLigneById(UUID id) {
        LigneConditionnement ligne = ligneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ligne non trouvée avec id: " + id));
        return modelMapper.map(ligne, LigneConditionnementDto.class);
    }

    @Transactional
    public LigneConditionnementDto createLigne(LigneConditionnementDto ligneDto) {

        LigneConditionnement ligne = modelMapper.map(ligneDto, LigneConditionnement.class);
        ligne.setCode(genererCodeLigne());
        ligne.setDeleted(false);
        ligne.setEtat(
                ligneDto.getEtat() != null ? ligneDto.getEtat() : Statue.ACTIF
        );
        LigneConditionnement savedLigne = ligneRepository.save(ligne);

        return modelMapper.map(savedLigne, LigneConditionnementDto.class);
    }
    @Transactional
    public LigneConditionnementDto updateLigne(UUID id, LigneConditionnementDto ligneDto) {
        LigneConditionnement existingLigne = ligneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ligne non trouvée avec id: " + id));
        if (!existingLigne.getCode().equals(ligneDto.getCode())) {
            if (ligneRepository.existsByCode(ligneDto.getCode())) {
                throw new RuntimeException("Une ligne avec ce code existe déjà: " + ligneDto.getCode());
            }
            existingLigne.setCode(ligneDto.getCode());
        }
        existingLigne.setNom(ligneDto.getNom());
        existingLigne.setDescription(ligneDto.getDescription());
        existingLigne.setEtat(ligneDto.getEtat());
        existingLigne.setVitesseNominale(ligneDto.getVitesseNominale());
        existingLigne.setTempsPreparation(ligneDto.getTempsPreparation());
        existingLigne.setTempsNettoyage(ligneDto.getTempsNettoyage());
        existingLigne.setResponsable(ligneDto.getResponsable());
        existingLigne.setDateDerniereMaintenance(ligneDto.getDateDerniereMaintenance());
        existingLigne.setDateProchaineMaintenance(ligneDto.getDateProchaineMaintenance());
        existingLigne.setNotes(ligneDto.getNotes());

        LigneConditionnement updatedLigne = ligneRepository.save(existingLigne);
        return modelMapper.map(updatedLigne, LigneConditionnementDto.class);
    }

    @Transactional
    public void desactiverLigne(UUID id) {
        LigneConditionnement ligne = ligneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ligne non trouvée avec id: " + id));
        ligne.setDeleted(true);
        ligneRepository.save(ligne);
    }

    @Transactional
    public void activerLigne(UUID id) {
        LigneConditionnement ligne = ligneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ligne non trouvée avec id: " + id));
        ligne.setDeleted(false);
        ligneRepository.save(ligne);
    }

    @Transactional
    public LigneConditionnementDto changerEtat(UUID id, Statue nouvelEtat) {
        LigneConditionnement ligne = ligneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ligne non trouvée avec id: " + id));

        ligne.setEtat(nouvelEtat);
        LigneConditionnement updatedLigne = ligneRepository.save(ligne);
        return modelMapper.map(updatedLigne, LigneConditionnementDto.class);
    }
    public List<LigneConditionnementDto> getLignesActives() {
        return ligneRepository.findByEtat(Statue.ACTIF).stream()
                .map(ligne -> modelMapper.map(ligne, LigneConditionnementDto.class))
                .collect(Collectors.toList());
    }

    private String genererCodeLigne() {
        String prefix = "LIG";
        String date = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return prefix + "-" + date + "-" + random;
    }


}