package com.myApp.Noblesse.Controllers;

import com.myApp.Noblesse.Entities.Facture;
import com.myApp.Noblesse.Entities.Ventes;
import com.myApp.Noblesse.services.FactureService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/factures")
public class FactureController {

    private final FactureService factureService;

    public FactureController(FactureService factureService) {
        this.factureService = factureService;
    }

    @GetMapping("/incompletes")
    public ResponseEntity<List<Facture>> listerFacturesIncompletes() {
        return ResponseEntity.ok(factureService.listerFacturesIncompletes());
    }

    @GetMapping("/terminees")
    public ResponseEntity<Page<Facture>> listerFacturesTerminees(
            @RequestParam(required = false) String nomClient,
            Pageable pageable) {
        return ResponseEntity.ok(factureService.listerFacturesTerminees(nomClient, pageable));
    }

    @GetMapping("/terminees/du-jour")
    public ResponseEntity<List<Facture>> listerFacturesTermineesDuJour() {
        return ResponseEntity.ok(factureService.listerFacturesTermineesDuJour());
    }

    @GetMapping("/top-clients/ca")
    public ResponseEntity<List<Map<String, Object>>> getTopClientsCA() {
        return ResponseEntity.ok(factureService.getTopClientsCA());
    }

    @GetMapping("/top-clients/repetition")
    public ResponseEntity<List<Map<String, Object>>> getTopClientsRepetition() {
        return ResponseEntity.ok(factureService.getTopClientsRepetition());
    }

    @DeleteMapping("/supprimer/{id}")
    public ResponseEntity<Void> supprimerFacture(@PathVariable Long id) {
        factureService.supprimerFacture(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/ventes/du-jour")
    public ResponseEntity<List<Ventes>> listerVentesDuJour() {
        return ResponseEntity.ok(factureService.listerVentesDuJour());
    }

    @GetMapping("/ventes/dernier-mois")
    public ResponseEntity<List<Ventes>> listerVentesDernierMois() {
        return ResponseEntity.ok(factureService.listerVentesDernierMois());
    }

    @PostMapping("/enregistrer")
    public ResponseEntity<Facture> enregistrerFacture(@RequestBody FactureRequest request) {
        return ResponseEntity.ok(factureService.enregistrerFacture(request.nomClient, request.ventesIds, request.terminee, request.avance, request.typeFacture, request.telephone, request.clientId, request.explications));
    }

    @PutMapping("/modifier/{id}")
    public ResponseEntity<Facture> modifierFacture(@PathVariable Long id, @RequestBody FactureRequest request) {
        return ResponseEntity.ok(factureService.modifierFacture(id, request.nomClient, request.ventesIds, request.terminee, request.avance, request.typeFacture, request.telephone, request.clientId, request.explications));
    }

    public static class FactureRequest {
        public String nomClient;
        public List<Long> ventesIds;
        public boolean terminee;
        public Double avance;
        public String typeFacture;
        public String telephone;
        public Long clientId;
        public String explications;
    }
}
