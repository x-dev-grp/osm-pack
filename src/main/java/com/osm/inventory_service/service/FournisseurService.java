package com.osm.inventory_service.service;

import com.osm.inventory_service.dto.FournisseurDto;
import com.osm.inventory_service.entity.Fournisseur;
import com.osm.inventory_service.repository.FournisseurRepository;
import com.xdev.communicator.models.enums.Currency;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FournisseurService extends BaseServiceImpl<Fournisseur, FournisseurDto, FournisseurDto> {

    private final FournisseurRepository fournisseurRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public FournisseurService(BaseRepository<Fournisseur> repository, FournisseurRepository fournisseurRepository, ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.fournisseurRepository = fournisseurRepository;
        this.modelMapper = modelMapper;
    }

    public List<FournisseurDto> getAllFournisseurs() {
        return fournisseurRepository.findAll().stream()
                .map(f -> modelMapper.map(f, FournisseurDto.class))
                .collect(Collectors.toList());
    }

    public FournisseurDto getFournisseurById(UUID id) {
        Fournisseur fournisseur = fournisseurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé avec id: " + id));
        return modelMapper.map(fournisseur, FournisseurDto.class);
    }

    @Transactional
    public FournisseurDto createFournisseur(FournisseurDto fournisseurDto) {
        if (!StringUtils.hasText(fournisseurDto.getNom())) {
            throw new RuntimeException("Le nom du fournisseur est obligatoire");
        }
        fournisseurDto.setCode(genererCodeFournisseur());
        if (fournisseurDto.getEmail() != null && !fournisseurDto.getEmail().isEmpty()) {
            if (fournisseurRepository.existsByEmail(fournisseurDto.getEmail())) {
                throw new RuntimeException("Un fournisseur avec cet email existe déjà: " + fournisseurDto.getEmail());
            }
        }
        if (fournisseurDto.getTelephone() != null && !fournisseurDto.getTelephone().isEmpty()) {
            if (fournisseurRepository.existsByTelephone(fournisseurDto.getTelephone())) {
                throw new RuntimeException("Un fournisseur avec ce téléphone existe déjà: " + fournisseurDto.getTelephone());
            }
        }
        if (fournisseurDto.getNumeroTva() != null && !fournisseurDto.getNumeroTva().isEmpty()) {
            if (fournisseurRepository.existsByNumeroTva(fournisseurDto.getNumeroTva())) {
                throw new RuntimeException("Un fournisseur avec ce numéro de TVA existe déjà: " + fournisseurDto.getNumeroTva());
            }
        }
        Fournisseur fournisseur = modelMapper.map(fournisseurDto, Fournisseur.class);

        if (fournisseur.getActif() == null) {
            fournisseur.setActif(true);
        }

        if (fournisseur.getCurrency() == null) {
            fournisseur.setCurrency(Currency.TND);
        }

        Fournisseur savedFournisseur = fournisseurRepository.save(fournisseur);
        return modelMapper.map(savedFournisseur, FournisseurDto.class);
    }

    @Transactional
    public FournisseurDto updateFournisseur(UUID id, FournisseurDto fournisseurDto) {
        Fournisseur existingFournisseur = fournisseurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé avec id: " + id));
        if (!StringUtils.hasText(fournisseurDto.getNom())) {
            throw new RuntimeException("Le nom du fournisseur est obligatoire");
        }
        if (fournisseurDto.getEmail() != null) {
            if (!fournisseurDto.getEmail().equals(existingFournisseur.getEmail())) {
                if (fournisseurRepository.existsByEmail(fournisseurDto.getEmail())) {
                    throw new RuntimeException("Un fournisseur avec cet email existe déjà: " + fournisseurDto.getEmail());
                }
                existingFournisseur.setEmail(fournisseurDto.getEmail());
            }
        } else {
            existingFournisseur.setEmail(null);
        }
        if (fournisseurDto.getTelephone() != null) {
            if (!fournisseurDto.getTelephone().equals(existingFournisseur.getTelephone())) {
                if (fournisseurRepository.existsByTelephone(fournisseurDto.getTelephone())) {
                    throw new RuntimeException("Un fournisseur avec ce téléphone existe déjà: " + fournisseurDto.getTelephone());
                }
                existingFournisseur.setTelephone(fournisseurDto.getTelephone());
            }
        } else {
            existingFournisseur.setTelephone(null);
        }
        if (fournisseurDto.getNumeroTva() != null) {
            if (!fournisseurDto.getNumeroTva().equals(existingFournisseur.getNumeroTva())) {
                if (fournisseurRepository.existsByNumeroTva(fournisseurDto.getNumeroTva())) {
                    throw new RuntimeException("Un fournisseur avec ce numéro de TVA existe déjà: " + fournisseurDto.getNumeroTva());
                }
                existingFournisseur.setNumeroTva(fournisseurDto.getNumeroTva());
            }
        } else {
            existingFournisseur.setNumeroTva(null);
        }
        existingFournisseur.setNom(fournisseurDto.getNom());
        existingFournisseur.setNomCommercial(fournisseurDto.getNomCommercial());
        existingFournisseur.setFax(fournisseurDto.getFax());
        existingFournisseur.setSiteWeb(fournisseurDto.getSiteWeb());
        existingFournisseur.setAdresse(fournisseurDto.getAdresse());
        existingFournisseur.setVille(fournisseurDto.getVille());
        existingFournisseur.setCodePostal(fournisseurDto.getCodePostal());
        existingFournisseur.setPays(fournisseurDto.getPays());
        existingFournisseur.setContactNom(fournisseurDto.getContactNom());
        existingFournisseur.setContactPrenom(fournisseurDto.getContactPrenom());
        existingFournisseur.setContactEmail(fournisseurDto.getContactEmail());
        existingFournisseur.setContactTelephone(fournisseurDto.getContactTelephone());
        existingFournisseur.setCategorieFournisseur(fournisseurDto.getCategorieFournisseur());
        existingFournisseur.setDelaiLivraisonMoyen(fournisseurDto.getDelaiLivraisonMoyen());
        existingFournisseur.setConditionsPaiement(fournisseurDto.getConditionsPaiement());
        existingFournisseur.setCurrency(fournisseurDto.getCurrency());
        existingFournisseur.setActif(fournisseurDto.getActif());
        existingFournisseur.setCertifications(fournisseurDto.getCertifications());

        Fournisseur updatedFournisseur = fournisseurRepository.save(existingFournisseur);
        return modelMapper.map(updatedFournisseur, FournisseurDto.class);
    }


    @Transactional
    public FournisseurDto activerFournisseur(UUID id) {
        Fournisseur fournisseur = fournisseurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé avec id: " + id));
        fournisseur.setActif(true);
        Fournisseur updatedFournisseur = fournisseurRepository.save(fournisseur);
        return modelMapper.map(updatedFournisseur, FournisseurDto.class);
    }

    @Transactional
    public FournisseurDto desactiverFournisseur(UUID id) {
        Fournisseur fournisseur = fournisseurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé avec id: " + id));
        fournisseur.setActif(false);
        Fournisseur updatedFournisseur = fournisseurRepository.save(fournisseur);
        return modelMapper.map(updatedFournisseur, FournisseurDto.class);
    }
    private String genererCodeFournisseur() {
        String prefix = "FRN";
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return prefix + date + random;
    }
}