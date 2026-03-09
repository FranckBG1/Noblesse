package com.myApp.Noblesse.services;

import com.myApp.Noblesse.DTO.DashboardStatsDTO;
import com.myApp.Noblesse.DTO.ProduitTopDTO;
import com.myApp.Noblesse.DTO.VenteResponseDTO;
import com.myApp.Noblesse.Entities.Ventes;
import com.myApp.Noblesse.Repositories.JournalActionRepository;
import com.myApp.Noblesse.Repositories.ProduitRepository;
import com.myApp.Noblesse.Repositories.VentesRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final VentesRepository ventesRepository;
    private final ProduitRepository produitRepository;
    private final JournalActionRepository journalActionRepository;

    public StatsService(VentesRepository ventesRepository, ProduitRepository produitRepository, JournalActionRepository journalActionRepository) {
        this.ventesRepository = ventesRepository;
        this.produitRepository = produitRepository;
        this.journalActionRepository = journalActionRepository;
    }

    public DashboardStatsDTO getDashboardStats() {
        LocalDateTime maintenant = LocalDateTime.now();
        LocalDateTime debutJour = LocalDate.now().atTime(6, 0);
        LocalDateTime finJour = LocalDate.now().atTime(23, 0);
        LocalDateTime debutSemaine = maintenant.minusDays(7);

        // Ventes du jour (06h - 23h)
        List<Ventes> ventesJourEntities = ventesRepository.findByDateVenteBetween(debutJour, finJour);
        double caJour = ventesJourEntities.stream().mapToDouble(Ventes::getMontantTotal).sum();
        long nbVentes = ventesJourEntities.size();

        long stockCritique = produitRepository.countByQuantiteLessThanEqual(5.0);

        List<VenteResponseDTO> ventesJour = ventesJourEntities.stream()
                .sorted(Comparator.comparing(Ventes::getDateVente).reversed())
                .map(VenteService::mapToResponseDTO)
                .collect(Collectors.toList());

        // Calcul des produits populaires sur 1 semaine (Top 5 par quantité totale vendue)
        List<Ventes> ventesSemaine = ventesRepository.findByDateVenteBetween(debutSemaine, maintenant);
        List<ProduitTopDTO> produitsPopulaires = ventesSemaine.stream()
                .collect(Collectors.groupingBy(v -> v.getProduit() != null ? v.getProduit().getDesignation() : v.getNomProduitHorsStock(),
                        Collectors.summingLong(Ventes::getQuantite)))
                .entrySet().stream()
                .map(e -> new ProduitTopDTO(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(ProduitTopDTO::quantiteVendue).reversed())
                .limit(5)
                .collect(Collectors.toList());

        var activitesRecentes = journalActionRepository.findTop5ByOrderByDateActionDesc();

        return new DashboardStatsDTO(caJour, nbVentes, stockCritique, ventesJour, produitsPopulaires, activitesRecentes);
    }
}
