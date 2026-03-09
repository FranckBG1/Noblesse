package com.myApp.Noblesse.Controllers;

import com.myApp.Noblesse.DTO.ProduitCreateDTO;
import com.myApp.Noblesse.DTO.ProduitUpdateDTO;
import com.myApp.Noblesse.Entities.Produit;
import com.myApp.Noblesse.services.ProduitService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/produit")
public class ProduitController {
    private final ProduitService produitService;

    public ProduitController(ProduitService produitService) {
        this.produitService = produitService;
    }

    //  Ajouter un produit (ADMIN uniquement)
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(path = "/ajouter", consumes = APPLICATION_JSON_VALUE)
    public void ajouterProduit(@RequestBody ProduitCreateDTO produit) {
        produitService.ajouterProduit(produit);
    }

    //  Supprimer un produit (ADMIN uniquement)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/supprimer/{idProduit}")
    public void supprimerProduit(@PathVariable Long idProduit) {
        produitService.supprimerProduit(idProduit);
    }

    //  Rechercher un ou plusieurs produits par mot-clé
    @GetMapping(path = "/rechercher/{keyword}")
    public List<Produit> getProduit(@PathVariable String keyword) {
        return produitService.getProduit(keyword);
    }

    //  Rechercher un ou plusieurs produits par code
    @GetMapping(path = "/rechercher/code/{code}")
    public List<Produit> getProduitByCode(@PathVariable String code) {
        return produitService.getProduitByCode(code);
    }

    //  Modifier un produit (ADMIN uniquement)
    @PutMapping(path = "/modifier/{id}")
    public ResponseEntity<Produit> updateProduitById(@PathVariable Long id, @RequestBody ProduitUpdateDTO updatedProduit) {
        Produit produit = produitService.updateProduitById(id, updatedProduit);
        return ResponseEntity.ok(produit);
    }

    // Lister tous les produits avec pagination
    @GetMapping(path = "/page")
    public Page<Produit> getAllProduitsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return produitService.getAllProduitsPaginated(PageRequest.of(page, size));
    }

    // Lister les produits critiques avec pagination
    @GetMapping(path = "/critique")
    public Page<Produit> getProduitsCritiquesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return produitService.getProduitsCritiquesPaginated(PageRequest.of(page, size));
    }

    //  Rechercher un produit par ID
    @GetMapping(path = "/{id}")
    public Produit getProduitById(@PathVariable Long id) {
        return produitService.getProduitById(id);
    }

    //  Lister tous les produits
    @GetMapping
    public List<Produit> getAllProduits() {
        return produitService.getAllProduits();
    }

    //  Retirer du stock (Utilisateur connecté requis)
    @PutMapping(path = "/retirerStock/{idProduit}/{quantite}")
    public void retirerStock(@PathVariable int idProduit, @PathVariable double quantite) {
        produitService.retirerStock(idProduit, quantite);
    }
}
