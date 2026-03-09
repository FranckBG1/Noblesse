package com.myApp.Noblesse.services;

import com.myApp.Noblesse.DTO.VenteRequestDTO;
import com.myApp.Noblesse.DTO.VenteResponseDTO;
import com.myApp.Noblesse.Entities.Produit;
import com.myApp.Noblesse.Entities.Ventes;
import com.myApp.Noblesse.Repositories.ProduitRepository;
import com.myApp.Noblesse.Repositories.VentesRepository;
import com.myApp.Noblesse.exceptions.RequeteInvalidException;
import com.myApp.Noblesse.exceptions.RessourceIntrouvableException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VenteService {

    private final VentesRepository ventesRepository;
    private final ProduitRepository produitRepository;
    private final JournalActionService journalActionService;
    private final SessionUtilisateurService sessionUtilisateurService;

    public VenteService(VentesRepository ventesRepository, ProduitRepository produitRepository, 
                        JournalActionService journalActionService, SessionUtilisateurService sessionUtilisateurService) {
        this.ventesRepository = ventesRepository;
        this.produitRepository = produitRepository;
        this.journalActionService = journalActionService;
        this.sessionUtilisateurService = sessionUtilisateurService;
    }

    private String formatDouble(Double val) {
        if (val == null) return "0";
        return String.valueOf(val).replaceAll("\\.0$", "");
    }

    @Transactional
    public VenteResponseDTO enregistrerVente(VenteRequestDTO request) {
        if (request.quantite() <= 0) {
            throw new RequeteInvalidException("La quantité vendue doit être supérieure à zéro.");
        }

        Produit produitAssocie = null;
        boolean produitCree = false;

        if (request.idProduit() != null) {
            // Cas 1 : Produit sélectionné via la liste (ID connu)
            produitAssocie = produitRepository.findById(request.idProduit())
                    .orElseThrow(() -> new RessourceIntrouvableException("Produit introuvable"));
        } else if (request.nomProduitHorsStock() != null && !request.nomProduitHorsStock().isBlank()) {
            // Cas 2 : Produit écrit à la main
            List<Produit> existants = produitRepository.findByDesignationContainingIgnoreCase(request.nomProduitHorsStock());
            
            produitAssocie = existants.stream()
                .filter(p -> p.getDesignation().equalsIgnoreCase(request.nomProduitHorsStock()))
                .findFirst()
                .orElse(null);

            if (produitAssocie == null) {
                // IL N'EXISTE PAS : ON LE CRÉE
                produitAssocie = new Produit();
                produitAssocie.setDesignation(request.nomProduitHorsStock());
                // On initialise avec la quantité demandée pour que la soustraction le ramène à 0
                produitAssocie.setQuantite((double) request.quantite()); 
                produitAssocie.setCode("DIVERS");
                produitAssocie.setPrixUnitaire(request.prixVente());
                produitAssocie = produitRepository.save(produitAssocie);
                produitCree = true;
            }
        } else {
            throw new RequeteInvalidException("Vous devez sélectionner un produit ou en saisir un nouveau.");
        }

        // Stock avant vente pour le journal
        double stockAvant = produitAssocie.getQuantite();

        // Diminuer le stock (en évitant la violation de contrainte SQL si possible)
        double nouvelleQuantite = produitAssocie.getQuantite() - request.quantite();
        if (nouvelleQuantite < 0) {
            nouvelleQuantite = 0.0; // On cap à 0 pour éviter l'erreur SQLite CHECK >= 0
        }
        produitAssocie.setQuantite(nouvelleQuantite);
        produitRepository.save(produitAssocie);
        
        Ventes vente = new Ventes(produitAssocie, request.prixVente(), request.quantite(), request.remise());
        Ventes savedVente = ventesRepository.save(vente);

        // Journalisation
        String utilisateurNom = sessionUtilisateurService.getUtilisateurActif() != null 
                ? sessionUtilisateurService.getUtilisateurActif().getNom() : "Inconnu";
        
        String prixUnitaireFormate = formatDouble(request.prixVente());
        String qteFormatee = formatDouble((double) request.quantite());
        
        String action = "A vendu " + qteFormatee + " " + produitAssocie.getDesignation() + " au prix unitaire de " + prixUnitaireFormate;
        
        if (request.remise() != null && request.remise() > 0) {
            action += " avec une remise de " + formatDouble(request.remise());
        }

        if (produitCree) {
            action += " (Ce produit n'était pas présent en stock mais nous l'avons ajouté avec quantité 0)";
        } else if (stockAvant <= 0) {
            action += " (sachant qu'en stock c'était vide 0)";
        }

        journalActionService.enregistrerAction(utilisateurNom, action, "VENTE");
        
        return VenteService.mapToResponseDTO(savedVente);
    }

    public List<VenteResponseDTO> listerVentes() {
        return ventesRepository.findAll().stream()
                .map(VenteService::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<Produit> rechercherProduits(String keyword) {
        return produitRepository.findByDesignationContainingIgnoreCase(keyword);
    }

    public List<Produit> rechercherParCode(String code) {
        return produitRepository.findByCodeContainingIgnoreCase(code);
    }

    public static VenteResponseDTO mapToResponseDTO(Ventes vente) {
        String nomProduit = (vente.getProduit() != null) 
                ? vente.getProduit().getDesignation() 
                : vente.getNomProduitHorsStock();

        return new VenteResponseDTO(
                vente.getIdVente(),
                nomProduit,
                vente.getPrixVente(),
                vente.getQuantite(),
                vente.getRemise(),
                vente.getMontantTotal(),
                vente.getDateVente()
        );
    }
}
