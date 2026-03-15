package com.abiooc.inventory_service.service;

import com.abiooc.inventory_service.Enum.CategorieFournisseur;
import com.abiooc.inventory_service.dto.FournisseurDto;
import com.abiooc.inventory_service.entity.Fournisseur;
import com.abiooc.inventory_service.repository.FournisseurRepository;
import com.xdev.communicator.models.enums.Currency;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public FournisseurDto getFournisseurByCode(String code) {
        Fournisseur fournisseur = fournisseurRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé avec code: " + code));
        return modelMapper.map(fournisseur, FournisseurDto.class);
    }

    public FournisseurDto getFournisseurByEmail(String email) {
        Fournisseur fournisseur = fournisseurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé avec email: " + email));
        return modelMapper.map(fournisseur, FournisseurDto.class);
    }

    public List<FournisseurDto> rechercherFournisseurs(String nom) {
        return fournisseurRepository.findByNomContainingIgnoreCase(nom).stream()
                .map(f -> modelMapper.map(f, FournisseurDto.class))
                .collect(Collectors.toList());
    }

    public List<FournisseurDto> getFournisseursByCategorie(CategorieFournisseur categorie) {
        return fournisseurRepository.findByCategorieFournisseur(categorie).stream()
                .map(f -> modelMapper.map(f, FournisseurDto.class))
                .collect(Collectors.toList());
    }


    public List<FournisseurDto> getFournisseursByPays(String pays) {
        return fournisseurRepository.findByPays(pays).stream()
                .map(f -> modelMapper.map(f, FournisseurDto.class))
                .collect(Collectors.toList());
    }


    @Transactional
    public FournisseurDto createFournisseur(FournisseurDto fournisseurDto) {

        if (fournisseurDto.getEmail() != null
                && fournisseurRepository.existsByEmail(fournisseurDto.getEmail())) {
            throw new RuntimeException(
                    "Un fournisseur avec cet email existe déjà: " + fournisseurDto.getEmail());
        }

        Fournisseur fournisseur = modelMapper.map(fournisseurDto, Fournisseur.class);
        fournisseur.setCode(genererCodeFournisseur());

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
        if (!existingFournisseur.getCode().equals(fournisseurDto.getCode())) {
            if (fournisseurRepository.existsByCode(fournisseurDto.getCode())) {
                throw new RuntimeException("Un fournisseur avec ce code existe déjà: " + fournisseurDto.getCode());
            }
            existingFournisseur.setCode(fournisseurDto.getCode());
        }
        if (fournisseurDto.getEmail() != null && !fournisseurDto.getEmail().equals(existingFournisseur.getEmail())) {
            if (fournisseurRepository.existsByEmail(fournisseurDto.getEmail())) {
                throw new RuntimeException("Un fournisseur avec cet email existe déjà: " + fournisseurDto.getEmail());
            }
            existingFournisseur.setEmail(fournisseurDto.getEmail());
        }

        existingFournisseur.setNom(fournisseurDto.getNom());
        existingFournisseur.setNomCommercial(fournisseurDto.getNomCommercial());
        existingFournisseur.setTelephone(fournisseurDto.getTelephone());
        existingFournisseur.setFax(fournisseurDto.getFax());
        existingFournisseur.setSiteWeb(fournisseurDto.getSiteWeb());
        existingFournisseur.setNumeroTva(fournisseurDto.getNumeroTva());
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


    @Transactional
    public void deleteFournisseur(UUID id) {
        Fournisseur fournisseur = fournisseurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé avec id: " + id));
        fournisseurRepository.delete(fournisseur);
    }

    private String genererCodeFournisseur() {
        String prefix = "FRN";
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return prefix + date + random;
    }
}