package com.abiooc.inventory_service.service;

import com.abiooc.inventory_service.dto.ClientDto;
import com.abiooc.inventory_service.entity.Client;
import com.abiooc.inventory_service.repository.ClientRepository;
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
public class ClientService extends BaseServiceImpl<Client, ClientDto, ClientDto> {

    private final ClientRepository clientRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public ClientService(BaseRepository<Client> repository, ClientRepository clientRepository, ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.clientRepository = clientRepository;
        this.modelMapper = modelMapper;
    }


    public List<ClientDto> getAllClients() {
        return clientRepository.findAll().stream()
                .map(client -> modelMapper.map(client, ClientDto.class))
                .collect(Collectors.toList());
    }


    public ClientDto getClientById(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec id: " + id));
        return modelMapper.map(client, ClientDto.class);
    }

    public ClientDto getClientByCode(String codeClient) {
        Client client = clientRepository.findByCodeClient(codeClient)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec code: " + codeClient));
        return modelMapper.map(client, ClientDto.class);
    }

    public ClientDto getClientByEmail(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec email: " + email));
        return modelMapper.map(client, ClientDto.class);
    }

    public List<ClientDto> rechercherClients(String searchTerm) {
        return clientRepository.rechercherClients(searchTerm).stream()
                .map(client -> modelMapper.map(client, ClientDto.class))
                .collect(Collectors.toList());
    }

    public List<ClientDto> getClientsPrivateLabel() {
        return clientRepository.findByPrivateLabelTrue().stream()
                .map(client -> modelMapper.map(client, ClientDto.class))
                .collect(Collectors.toList());
    }
//récupère tous les clients qui ne sont pas des "private label".
    public List<ClientDto> getClientsStandard() {
        return clientRepository.findByPrivateLabelFalse().stream()
                .map(client -> modelMapper.map(client, ClientDto.class))
                .collect(Collectors.toList());
    }


    @Transactional
    public ClientDto createClient(ClientDto clientDto) {
        clientDto.setCodeClient(genererCodeClient());
        if (clientDto.getEmail() != null && !clientDto.getEmail().isEmpty()) {
            if (clientRepository.existsByEmail(clientDto.getEmail())) {
                throw new RuntimeException("Un client avec cet email existe déjà: " + clientDto.getEmail());
            }
        }

        Client client = modelMapper.map(clientDto, Client.class);
        client.setDeleted(false);

        Client savedClient = clientRepository.save(client);
        return modelMapper.map(savedClient, ClientDto.class);
    }

    @Transactional
    public ClientDto updateClient(UUID id, ClientDto clientDto) {
        Client existingClient = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec id: " + id));

        if (clientDto.getCodeClient() != null && !clientDto.getCodeClient().equals(existingClient.getCodeClient())) {
            if (clientRepository.existsByCodeClient(clientDto.getCodeClient())) {
                throw new RuntimeException("Un client avec ce code existe déjà: " + clientDto.getCodeClient());
            }
            existingClient.setCodeClient(clientDto.getCodeClient());
        }

        if (clientDto.getEmail() != null && !clientDto.getEmail().equals(existingClient.getEmail())) {
            if (clientRepository.existsByEmail(clientDto.getEmail())) {
                throw new RuntimeException("Un client avec cet email existe déjà: " + clientDto.getEmail());
            }
            existingClient.setEmail(clientDto.getEmail());
        }


        existingClient.setNom(clientDto.getNom());
        existingClient.setTelephone(clientDto.getTelephone());
        existingClient.setAdresse(clientDto.getAdresse());
        existingClient.setVille(clientDto.getVille());
        existingClient.setPays(clientDto.getPays());
        existingClient.setCodePostal(clientDto.getCodePostal());
        existingClient.setPrivateLabel(clientDto.getPrivateLabel());
        existingClient.setSiret(clientDto.getSiret());
        existingClient.setNumeroTva(clientDto.getNumeroTva());
        existingClient.setNotes(clientDto.getNotes());

        Client updatedClient = clientRepository.save(existingClient);
        return modelMapper.map(updatedClient, ClientDto.class);
    }


    @Transactional
    public void desactiverClient(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec id: " + id));
        client.setDeleted(true);
        clientRepository.save(client);
    }

    @Transactional
    public void activerClient(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec id: " + id));
        client.setDeleted(false);
        clientRepository.save(client);
    }


    @Transactional
    public void deleteClient(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec id: " + id));
        clientRepository.delete(client); // Suppression physique
    }


    private String genererCodeClient() {
        String prefix = "CLT";
        String date = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return prefix + "-" + date + "-" + random;
    }

    public long countClients() {
        return clientRepository.count();
    }


}