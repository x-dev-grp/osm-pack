package com.osm.inventory_service.service;

import com.osm.inventory_service.dto.EmplacementStockDto;
import com.osm.inventory_service.entity.EmplacementStock;
import com.osm.inventory_service.repository.EmplacementStockRepository;
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
public class EmplacementStockService extends BaseServiceImpl<EmplacementStock, EmplacementStockDto, EmplacementStockDto> {

    private final EmplacementStockRepository emplacementRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public EmplacementStockService(BaseRepository<EmplacementStock> repository, EmplacementStockRepository emplacementRepository, ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.emplacementRepository = emplacementRepository;
        this.modelMapper = modelMapper;
    }
    public List<EmplacementStockDto> getAllEmplacements() {
        return emplacementRepository.findAll().stream()
                .map(emp -> modelMapper.map(emp, EmplacementStockDto.class))
                .collect(Collectors.toList());
    }
    public EmplacementStockDto getEmplacementById(UUID id) {
        EmplacementStock emplacement = emplacementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emplacement non trouvé avec id: " + id));
        return modelMapper.map(emplacement, EmplacementStockDto.class);
    }

    public List<EmplacementStockDto> getEmplacementsReservesPour(String client) {
        return emplacementRepository.findReservesPour(client).stream()
                .map(emp -> modelMapper.map(emp, EmplacementStockDto.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public EmplacementStockDto createEmplacement(EmplacementStockDto emplacementDto) {
        EmplacementStock emplacement = modelMapper.map(emplacementDto, EmplacementStock.class);
        emplacement.setCode(generateUniqueCode());
        if (emplacement.getDisponible() == null) {
            emplacement.setDisponible(true);
        }
        if (emplacement.getCapaciteActuelle() == null) {
            emplacement.setCapaciteActuelle("0");
        }
        emplacement.setActif(true);

        EmplacementStock savedEmplacement = emplacementRepository.save(emplacement);
        return modelMapper.map(savedEmplacement, EmplacementStockDto.class);
    }
    @Transactional
    public EmplacementStockDto activerEmplacement(UUID id) {
        EmplacementStock emplacement = emplacementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emplacement non trouvé avec id: " + id));
        emplacement.setActif(true);
        EmplacementStock updated = emplacementRepository.save(emplacement);
        return modelMapper.map(updated, EmplacementStockDto.class);
    }
    @Transactional
    public EmplacementStockDto desactiverEmplacement(UUID id) {
        EmplacementStock emplacement = emplacementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emplacement non trouvé avec id: " + id));
        emplacement.setActif(false);
        EmplacementStock updated = emplacementRepository.save(emplacement);
        return modelMapper.map(updated, EmplacementStockDto.class);
    }

    @Transactional
    public EmplacementStockDto updateEmplacement(UUID id, EmplacementStockDto emplacementDto) {
        EmplacementStock existingEmplacement = emplacementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emplacement non trouvé avec id: " + id));

        if (!existingEmplacement.getCode().equals(emplacementDto.getCode())) {
            if (emplacementRepository.existsByCode(emplacementDto.getCode())) {
                throw new RuntimeException("Un emplacement avec ce code existe déjà: " + emplacementDto.getCode());
            }
            existingEmplacement.setCode(emplacementDto.getCode());
        }
        existingEmplacement.setNom(emplacementDto.getNom());
        existingEmplacement.setTypeEmplacement(emplacementDto.getTypeEmplacement());
        existingEmplacement.setCapaciteMaximale(emplacementDto.getCapaciteMaximale());
        existingEmplacement.setCapaciteActuelle(emplacementDto.getCapaciteActuelle());
        existingEmplacement.setZone(emplacementDto.getZone());
        existingEmplacement.setDisponible(emplacementDto.getDisponible());
        existingEmplacement.setReservePour(emplacementDto.getReservePour());
        existingEmplacement.setConditionsSpeciales(emplacementDto.getConditionsSpeciales());
        existingEmplacement.setTemperatureMin(emplacementDto.getTemperatureMin());
        existingEmplacement.setTemperatureMax(emplacementDto.getTemperatureMax());
        existingEmplacement.setDescription(emplacementDto.getDescription());
        existingEmplacement.setNotes(emplacementDto.getNotes());

        EmplacementStock updatedEmplacement = emplacementRepository.save(existingEmplacement);
        return modelMapper.map(updatedEmplacement, EmplacementStockDto.class);
    }
    @Transactional
    public EmplacementStockDto reserverEmplacement(UUID id, String reservePour) {
        EmplacementStock emplacement = emplacementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emplacement non trouvé avec id: " + id));

        emplacement.setDisponible(false);
        emplacement.setReservePour(reservePour);

        EmplacementStock updatedEmplacement = emplacementRepository.save(emplacement);
        return modelMapper.map(updatedEmplacement, EmplacementStockDto.class);
    }

    @Transactional
    public EmplacementStockDto libererEmplacement(UUID id) {
        EmplacementStock emplacement = emplacementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emplacement non trouvé avec id: " + id));

        emplacement.setDisponible(true);
        emplacement.setReservePour(null);
        emplacement.setCapaciteActuelle("0");

        EmplacementStock updatedEmplacement = emplacementRepository.save(emplacement);
        return modelMapper.map(updatedEmplacement, EmplacementStockDto.class);
    }

    @Transactional
    public EmplacementStockDto mettreAJourCapacite(UUID id, String nouvelleCapacite) {
        EmplacementStock emplacement = emplacementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emplacement non trouvé avec id: " + id));

        emplacement.setCapaciteActuelle(nouvelleCapacite);

        EmplacementStock updatedEmplacement = emplacementRepository.save(emplacement);
        return modelMapper.map(updatedEmplacement, EmplacementStockDto.class);
    }

    @Transactional
    public void deleteEmplacement(UUID id) {
        EmplacementStock emplacement = emplacementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emplacement non trouvé avec id: " + id));
        emplacementRepository.delete(emplacement);
    }
    private String generateUniqueCode() {
        String code;
        do {
            code = "EMP-" + System.currentTimeMillis();
        } while (emplacementRepository.existsByCode(code));
        return code;
    }

   /* public List<AuditDto> getAuditEmplacements() {
        return emplacementRepository.findAll().stream()
                .map(emp -> new AuditDto(
                        "EmplacementStock",
                        emp.getId().toString(),
                        emp.getCreatedBy(),
                        emp.getCreatedDate(),
                        emp.getLastModifiedBy(),
                        emp.getLastModifiedDate()
                ))
                .collect(Collectors.toList());*/

}