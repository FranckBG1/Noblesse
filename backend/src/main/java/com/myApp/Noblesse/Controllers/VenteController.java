package com.myApp.Noblesse.Controllers;

import com.myApp.Noblesse.DTO.VenteRequestDTO;
import com.myApp.Noblesse.DTO.VenteResponseDTO;
import com.myApp.Noblesse.Entities.Produit;
import com.myApp.Noblesse.services.VenteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ventes")
public class VenteController {

    private final VenteService venteService;

    public VenteController(VenteService venteService) {
        this.venteService = venteService;
    }

    @PostMapping("/enregistrer")
    public ResponseEntity<VenteResponseDTO> enregistrerVente(@RequestBody VenteRequestDTO request) {
        return ResponseEntity.ok(venteService.enregistrerVente(request));
    }

    @GetMapping("/lister")
    public ResponseEntity<List<VenteResponseDTO>> listerVentes() {
        return ResponseEntity.ok(venteService.listerVentes());
    }

    @GetMapping("/rechercher")
    public ResponseEntity<List<Produit>> rechercherProduits(@RequestParam String keyword) {
        return ResponseEntity.ok(venteService.rechercherProduits(keyword));
    }

    @GetMapping("/rechercher/code")
    public ResponseEntity<List<Produit>> rechercherParCode(@RequestParam String code) {
        return ResponseEntity.ok(venteService.rechercherParCode(code));
    }
}
