package com.myApp.Noblesse.services;

import com.myApp.Noblesse.Entities.Client;
import com.myApp.Noblesse.Repositories.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService {
    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<Client> listerClients() {
        return clientRepository.findAll();
    }

    public List<Client> rechercherClients(String nom) {
        List<Client> clients = clientRepository.findByNomContainingIgnoreCase(nom);
        // Calculer le solde pour chaque client (toujours recalculé à chaque recherche)
        for (Client client : clients) {
            double solde = calculerSoldeDisponible(client.getIdClient());
            client.setSoldeDisponible(solde);
        }
        return clients;
    }

    private double calculerSoldeDisponible(Long clientId) {
        // Total des dépôts (positif)
        Double totalDepots = clientRepository.getTotalDepots(clientId);
        // Total des retraits (négatif)
        Double totalRetraits = clientRepository.getTotalRetraits(clientId);
        
        double depots = totalDepots != null ? totalDepots : 0.0;
        double retraits = totalRetraits != null ? totalRetraits : 0.0;
        
        // Solde = Dépôts + Retraits (retraits sont négatifs)
        return depots + retraits;
    }

    public Client creerOuRecupererClient(String nom, String telephone) {
        Optional<Client> existant = clientRepository.findByNomIgnoreCase(nom);
        if (existant.isPresent()) {
            Client client = existant.get();
            if (telephone != null && !telephone.isEmpty()) {
                client.setTelephone(telephone);
                return clientRepository.save(client);
            }
            return client;
        }
        Client nouveau = new Client(nom);
        nouveau.setTelephone(telephone);
        return clientRepository.save(nouveau);
    }

    public Optional<Client> getClientById(Long id) {
        return clientRepository.findById(id);
    }
}
