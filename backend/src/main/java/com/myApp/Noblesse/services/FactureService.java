package com.myApp.Noblesse.services;

import com.myApp.Noblesse.Entities.Client;
import com.myApp.Noblesse.Entities.Facture;
import com.myApp.Noblesse.Entities.Ventes;
import com.myApp.Noblesse.Repositories.ClientRepository;
import com.myApp.Noblesse.Repositories.FactureRepository;
import com.myApp.Noblesse.Repositories.VentesRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FactureService {
    private final FactureRepository factureRepository;
    private final VentesRepository ventesRepository;
    private final JournalActionService journalActionService;
    private final SessionUtilisateurService sessionUtilisateurService;
    private final ClientService clientService;
    private final ClientRepository clientRepository;

    public FactureService(FactureRepository factureRepository, VentesRepository ventesRepository, 
                          JournalActionService journalActionService, SessionUtilisateurService sessionUtilisateurService,
                          ClientService clientService, ClientRepository clientRepository) {
        this.factureRepository = factureRepository;
        this.ventesRepository = ventesRepository;
        this.journalActionService = journalActionService;
        this.sessionUtilisateurService = sessionUtilisateurService;
        this.clientService = clientService;
        this.clientRepository = clientRepository;
    }

    public List<Facture> listerFacturesIncompletes() {
        return factureRepository.findByStatus("ENREGISTRE");
    }

    public Page<Facture> listerFacturesTerminees(String nomClient, Pageable pageable) {
        if (nomClient != null && !nomClient.isEmpty()) {
            return factureRepository.findByStatusAndClientNomContainingIgnoreCase("TERMINEE", nomClient, pageable);
        }
        return factureRepository.findByStatus("TERMINEE", pageable);
    }

    public List<Facture> listerFacturesTermineesDuJour() {
        LocalDateTime start = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);
        return factureRepository.findByStatusAndDateCreationBetween("TERMINEE", start, end);
    }

    public List<Map<String, Object>> getTopClientsCA() {
        List<Object[]> results = factureRepository.findTopClientsByCA();
        List<Map<String, Object>> topClients = new ArrayList<>();
        for (int i = 0; i < Math.min(results.size(), 10); i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("nomClient", results.get(i)[0]);
            map.put("ca", results.get(i)[1]);
            topClients.add(map);
        }
        return topClients;
    }

    public List<Map<String, Object>> getTopClientsRepetition() {
        List<Object[]> results = factureRepository.findTopClientsByRepetition();
        List<Map<String, Object>> topClients = new ArrayList<>();
        for (int i = 0; i < Math.min(results.size(), 10); i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("nomClient", results.get(i)[0]);
            map.put("repetition", results.get(i)[1]);
            topClients.add(map);
        }
        return topClients;
    }

    @Transactional
    public void supprimerFacture(Long id) {
        Facture facture = factureRepository.findById(id).orElseThrow(() -> new RuntimeException("Facture introuvable"));
        if (!"ENREGISTRE".equals(facture.getStatus())) {
            throw new RuntimeException("Seules les factures non terminées peuvent être supprimées.");
        }
        
        // Détacher les ventes avant suppression
        for (Ventes v : facture.getVentes()) {
            v.setFacture(null);
            ventesRepository.save(v);
        }
        
        factureRepository.delete(facture);
        
        String user = sessionUtilisateurService.getUtilisateurActif() != null ? sessionUtilisateurService.getUtilisateurActif().getNom() : "Inconnu";
        journalActionService.enregistrerAction(user, "A supprimé la facture de " + facture.getNomClient(), "FACTURE");
    }

    public List<Ventes> listerVentesDuJour() {
        LocalDateTime start = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);
        return ventesRepository.findByDateVenteBetween(start, end);
    }

    public List<Ventes> listerVentesDernierMois() {
        LocalDateTime unMoisAvant = LocalDateTime.now().minusMonths(1);
        return ventesRepository.findByDateVenteAfterOrderByDateVenteDesc(unMoisAvant);
    }

    @Transactional
    public Facture enregistrerFacture(String nomClient, List<Long> ventesIds, boolean terminee, Double avance, String typeFacture, String telephone, Long clientId, String explications) {
        Facture facture = new Facture();
        facture.setTypeFacture(typeFacture != null ? typeFacture : "CLASSIQUE");
        facture.setExplications(explications);
        
        Client client;
        if (clientId != null) {
            client = clientService.getClientById(clientId)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));
            if (telephone != null && !telephone.isEmpty()) {
                client.setTelephone(telephone);
                client = clientRepository.save(client);
            }
        } else {
            client = clientService.creerOuRecupererClient(nomClient, telephone);
        }
        
        facture.setClient(client);
        facture.setStatus(terminee ? "TERMINEE" : "ENREGISTRE");
        facture.setUtilisateur(sessionUtilisateurService.getUtilisateurActif());
        
        if ("DEPOT".equals(typeFacture)) {
            facture.setVentes(new ArrayList<>());
            facture.setAvance(avance != null ? avance : 0.0);
            facture.setReste(0.0);
        } else {
            List<Ventes> ventes = ventesRepository.findAllById(ventesIds);
            facture.setVentes(ventes);
            double total = ventes.stream().mapToDouble(Ventes::getMontantTotal).sum();
            double avanceSaisie = avance != null ? avance : 0.0;
            double avanceUtilisee = avanceSaisie > 0 ? Math.min(avanceSaisie, total) : 0.0;
            
            facture.setAvance(avanceSaisie);
            facture.setReste(total - avanceUtilisee);
            
            if (terminee && avanceUtilisee > 0) {
                Facture retrait = new Facture();
                retrait.setTypeFacture("RETRAIT");
                retrait.setClient(client);
                retrait.setStatus("TERMINEE");
                retrait.setUtilisateur(sessionUtilisateurService.getUtilisateurActif());
                retrait.setVentes(new ArrayList<>());
                retrait.setAvance(-avanceUtilisee);
                retrait.setReste(0.0);
                factureRepository.save(retrait);
            }
        }
        
        Facture saved = factureRepository.save(facture);
        
        if ("CLASSIQUE".equals(typeFacture) || typeFacture == null) {
            for (Ventes v : saved.getVentes()) {
                v.setFacture(saved);
                ventesRepository.save(v);
            }
        }

        String user = sessionUtilisateurService.getUtilisateurActif() != null ? sessionUtilisateurService.getUtilisateurActif().getNom() : "Inconnu";
        String action = "DEPOT".equals(typeFacture) ? 
            "A enregistré un dépôt de " + avance + " FCFA pour " + nomClient :
            (terminee ? "A généré" : "A enregistré") + " une facture pour " + nomClient;
        journalActionService.enregistrerAction(user, action, "FACTURE");
        
        return saved;
    }

    @Transactional
    public Facture modifierFacture(Long id, String nomClient, List<Long> ventesIds, boolean terminee, Double avance, String typeFacture, String telephone, Long clientId, String explications) {
        Facture facture = factureRepository.findById(id).orElseThrow(() -> new RuntimeException("Facture introuvable"));
        double ancienneAvance = facture.getAvance() != null ? facture.getAvance() : 0.0;
        boolean etaitTerminee = "TERMINEE".equals(facture.getStatus());
        String ancienNomClient = facture.getNomClient();
        
        facture.setTypeFacture(typeFacture != null ? typeFacture : "CLASSIQUE");
        facture.setExplications(explications);
        
        Client client;
        if (clientId != null) {
            client = clientService.getClientById(clientId)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));
            if (telephone != null && !telephone.isEmpty()) {
                client.setTelephone(telephone);
                client = clientRepository.save(client);
            }
        } else {
            client = clientService.creerOuRecupererClient(nomClient, telephone);
        }
        
        facture.setClient(client);
        facture.setStatus(terminee ? "TERMINEE" : "ENREGISTRE");
        facture.setDerniereModif(LocalDateTime.now());
        facture.setUtilisateur(sessionUtilisateurService.getUtilisateurActif());

        List<String> modifications = new ArrayList<>();
        
        if (!ancienNomClient.equals(nomClient)) {
            modifications.add("client: " + ancienNomClient + " → " + nomClient);
        }

        for (Ventes v : facture.getVentes()) {
            v.setFacture(null);
            ventesRepository.save(v);
        }

        if ("DEPOT".equals(typeFacture)) {
            double nouvelleAvance = avance != null ? avance : 0.0;
            double differenceAvance = nouvelleAvance - ancienneAvance;
            
            if (differenceAvance != 0) {
                modifications.add("dépôt: " + String.format("%.0f", ancienneAvance) + " → " + String.format("%.0f", nouvelleAvance) + " FCFA");
            }
            
            facture.setVentes(new ArrayList<>());
            facture.setAvance(nouvelleAvance);
            facture.setReste(0.0);
            
            if (differenceAvance != 0 && terminee) {
                Facture ajustement = new Facture();
                ajustement.setTypeFacture("DEPOT");
                ajustement.setClient(client);
                ajustement.setStatus("TERMINEE");
                ajustement.setUtilisateur(sessionUtilisateurService.getUtilisateurActif());
                ajustement.setVentes(new ArrayList<>());
                ajustement.setAvance(differenceAvance);
                ajustement.setReste(0.0);
                factureRepository.save(ajustement);
            }
        } else {
            List<Ventes> nouvellesVentes = ventesRepository.findAllById(ventesIds);
            facture.setVentes(nouvellesVentes);
            double total = nouvellesVentes.stream().mapToDouble(Ventes::getMontantTotal).sum();
            double avanceSaisie = avance != null ? avance : 0.0;
            double avanceUtilisee = avanceSaisie > 0 ? Math.min(avanceSaisie, total) : 0.0;
            double ancienneAvanceUtilisee = (etaitTerminee && ancienneAvance > 0) ? Math.min(ancienneAvance, total) : 0.0;
            double differenceAvanceUtilisee = avanceUtilisee - ancienneAvanceUtilisee;
            
            if (avanceSaisie != ancienneAvance) {
                modifications.add("avance: " + String.format("%.0f", ancienneAvance) + " → " + String.format("%.0f", avanceSaisie) + " FCFA");
            }
            
            facture.setAvance(avanceSaisie);
            facture.setReste(total - avanceUtilisee);
            
            if (terminee && differenceAvanceUtilisee != 0) {
                Facture retrait = new Facture();
                retrait.setTypeFacture("RETRAIT");
                retrait.setClient(client);
                retrait.setStatus("TERMINEE");
                retrait.setUtilisateur(sessionUtilisateurService.getUtilisateurActif());
                retrait.setVentes(new ArrayList<>());
                retrait.setAvance(-differenceAvanceUtilisee);
                retrait.setReste(0.0);
                factureRepository.save(retrait);
            }
        }
        
        Facture saved = factureRepository.save(facture);
        
        if ("CLASSIQUE".equals(typeFacture) || typeFacture == null) {
            for (Ventes v : saved.getVentes()) {
                v.setFacture(saved);
                ventesRepository.save(v);
            }
        }

        String user = sessionUtilisateurService.getUtilisateurActif() != null ? sessionUtilisateurService.getUtilisateurActif().getNom() : "Inconnu";
        String baseAction = "DEPOT".equals(typeFacture) ?
            "A modifié un dépôt pour " + nomClient :
            (terminee ? "A terminé" : "A modifié") + " la facture de " + nomClient;
        
        String action = modifications.isEmpty() ? baseAction : baseAction + " (" + String.join(", ", modifications) + ")";
        journalActionService.enregistrerAction(user, action, "FACTURE");

        return saved;
    }
}
