package com.myApp.Noblesse.DTO;

import com.myApp.Noblesse.Entities.JournalAction;
import java.util.List;

public record DashboardStatsDTO(
    double chiffreAffaireJour,
    long nombreVentesJour,
    long stockCritique,
    List<VenteResponseDTO> ventesJour,
    List<ProduitTopDTO> produitsPopulaires,
    List<JournalAction> activitesRecentes
) {}
