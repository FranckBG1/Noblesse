package com.myApp.Noblesse.services;

import com.myApp.Noblesse.DTO.ProduitCreateDTO;
import com.myApp.Noblesse.DTO.ProduitUpdateDTO;
import com.myApp.Noblesse.Entities.Produit;
import com.myApp.Noblesse.Entities.Users;
import com.myApp.Noblesse.Repositories.ProduitRepository;
import com.myApp.Noblesse.exceptions.RessourceIntrouvableException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProduitService {
    private final ProduitRepository produitRepository;
    private final SessionUtilisateurService sessionUtilisateur;
    private final PasswordEncoder passwordEncoder;
    private final JournalActionService journalActionService;

    public ProduitService(ProduitRepository produitRepository, SessionUtilisateurService sessionUtilisateur, 
                          PasswordEncoder passwordEncoder, JournalActionService journalActionService) {
        this.produitRepository = produitRepository;
        this.sessionUtilisateur = sessionUtilisateur;
        this.passwordEncoder = passwordEncoder;
        this.journalActionService = journalActionService;
    }

    // Ajouter un produit (ADMIN uniquement + MDP de confirmation)
    public void ajouterProduit(ProduitCreateDTO produitDto) {
        verifierAdmin();

        Users adminActuel = sessionUtilisateur.getUtilisateurActif();
        if (produitDto.getMotDePasseAdmin() == null ||
                !passwordEncoder.matches(produitDto.getMotDePasseAdmin(), adminActuel.getMotDePasse())) {
            throw new RuntimeException("Confirmation requise : mot de passe administrateur incorrect.");
        }

        Produit produit = new Produit();
        produit.setDesignation(produitDto.getDesignation());
        produit.setCode(produitDto.getCode());
        produit.setPhoto(produitDto.getPhoto());
        produit.setQuantite(produitDto.getQuantite());
        produit.setDernierPrix(produitDto.getDernierPrix());
        produit.setPrixUnitaire(produitDto.getPrixUnitaire());

        produitRepository.save(produit);
        journalActionService.enregistrerAction(adminActuel.getNom(), "A créé le produit : " + produit.getDesignation(), "PRODUIT");
    }

    // Supprimer un produit (ADMIN uniquement)
    public void supprimerProduit(Long idProduit) {
        verifierAdmin();

        Produit produit = produitRepository.findById(idProduit)
                .orElseThrow(() -> new RessourceIntrouvableException("Produit avec ID " + idProduit + " introuvable."));

        produitRepository.deleteById(idProduit);
        journalActionService.enregistrerAction(sessionUtilisateur.getUtilisateurActif().getNom(), "A supprimé le produit : " + produit.getDesignation(), "PRODUIT");
    }

    // Rechercher par mot-clé (Accessible à tous)
    public List<Produit> getProduit(String keyword) {
        List<Produit> resultats = produitRepository.findAllByDesignationContainingIgnoreCase(keyword);

        if (resultats.isEmpty()) {
            throw new RessourceIntrouvableException("Aucun produit correspondant à : " + keyword);
        }

        return resultats;
    }

    // Rechercher par code (Accessible à tous)
    public List<Produit> getProduitByCode(String code) {
        List<Produit> resultats = produitRepository.findByCodeContainingIgnoreCase(code);

        if (resultats.isEmpty()) {
            throw new RessourceIntrouvableException("Aucun produit correspondant au code : " + code);
        }

        return resultats;
    }

    // Modifier un produit (ADMIN uniquement + MDP de confirmation)
    public Produit updateProduitById(Long id, ProduitUpdateDTO updatedProduit) {
        verifierAdmin();

        Users adminActuel = sessionUtilisateur.getUtilisateurActif();
        if (updatedProduit.getMotDePasseAdmin() == null ||
                !passwordEncoder.matches(updatedProduit.getMotDePasseAdmin(), adminActuel.getMotDePasse())) {
            throw new RuntimeException("Confirmation requise : mot de passe administrateur incorrect.");
        }

        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Produit avec ID " + id + " introuvable."));

        StringBuilder modifications = new StringBuilder();
        
        if (!produit.getDesignation().equals(updatedProduit.getDesignation())) {
            modifications.append("Désignation: [").append(produit.getDesignation()).append(" -> ").append(updatedProduit.getDesignation()).append("] ");
        }
        
        String oldCode = produit.getCode() != null ? produit.getCode() : "Aucun";
        String newCode = updatedProduit.getCode() != null ? updatedProduit.getCode() : "Aucun";
        if (!oldCode.equals(newCode)) {
            modifications.append("Code: [").append(oldCode).append(" -> ").append(newCode).append("] ");
        }

        if (produit.getQuantite() == null || !produit.getQuantite().equals(updatedProduit.getQuantite())) {
            modifications.append("Quantité: [").append(formatDouble(produit.getQuantite())).append(" -> ").append(formatDouble(updatedProduit.getQuantite())).append("] ");
        }
        if (produit.getPrixUnitaire() == null || !produit.getPrixUnitaire().equals(updatedProduit.getPrixUnitaire())) {
            modifications.append("Prix: [").append(formatDouble(produit.getPrixUnitaire())).append(" -> ").append(formatDouble(updatedProduit.getPrixUnitaire())).append("] ");
        }

        String oldPhoto = produit.getPhoto() != null ? "Présente" : "Aucune";
        String newPhoto = updatedProduit.getPhoto() != null ? "Présente" : "Aucune";
        if (updatedProduit.getPhoto() != null && !updatedProduit.getPhoto().equals(produit.getPhoto())) {
            modifications.append("Photo: [Modifiée] ");
        } else if (produit.getPhoto() != null && updatedProduit.getPhoto() == null) {
            modifications.append("Photo: [Supprimée] ");
        }

        produit.setDesignation(updatedProduit.getDesignation());
        produit.setCode(updatedProduit.getCode());
        produit.setPhoto(updatedProduit.getPhoto());
        produit.setQuantite(updatedProduit.getQuantite());
        produit.setDernierPrix(updatedProduit.getDernierPrix());
        produit.setPrixUnitaire(updatedProduit.getPrixUnitaire());

        Produit saved = produitRepository.save(produit);
        
        String action = "A modifié le produit : " + saved.getDesignation();
        if (modifications.length() > 0) {
            action += " (Détails: " + modifications.toString().trim() + ")";
        }
        
        journalActionService.enregistrerAction(adminActuel.getNom(), action, "PRODUIT");
        return saved;
    }

    private String formatDouble(Double val) {
        if (val == null) return "0";
        return String.valueOf(val).replaceAll("\\.0$", "");
    }

    // Trouver un produit par ID (Accessible à tous)
    public Produit getProduitById(Long id) {
        return produitRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Produit avec ID " + id + " introuvable."));
    }

    // Lister tous les produits (Accessible à tous)
    public List<Produit> getAllProduits() {
        return produitRepository.findAll();
    }

    // Lister tous les produits avec pagination
    public Page<Produit> getAllProduitsPaginated(Pageable pageable) {
        return produitRepository.findAll(pageable);
    }

    // Lister les produits critiques avec pagination
    public Page<Produit> getProduitsCritiquesPaginated(Pageable pageable) {
        return produitRepository.findByQuantiteLessThanEqual(5.0, pageable);
    }

    // Retirer du stock (Utilisateurs connectés uniquement)
    public void retirerStock(int idProduit, double quantite) {
        verifierConnecte();

        Produit produit = produitRepository.findById((long) idProduit)
                .orElseThrow(() -> new RessourceIntrouvableException("Produit avec ID " + idProduit + " introuvable."));

        if (produit.getQuantite() < quantite) {
            throw new IllegalArgumentException("Stock insuffisant pour retirer " + quantite + " unités.");
        }

        produit.setQuantite(produit.getQuantite() - quantite);
        produitRepository.save(produit);
    }

    //  Vérifie si l'utilisateur est connecté
    private void verifierConnecte() {
        if (!sessionUtilisateur.estConnecte()) {
            throw new RuntimeException("Vous devez être connecté pour effectuer cette opération.");
        }
    }

    //   Vérifie si l'utilisateur est admin
    private void verifierAdmin() {
        if (!sessionUtilisateur.estAdmin()) {
            throw new RuntimeException("Seul un administrateur peut effectuer cette opération.");
        }
    }
}
