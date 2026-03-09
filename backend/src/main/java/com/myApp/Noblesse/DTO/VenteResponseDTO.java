package com.myApp.Noblesse.DTO;

import java.time.LocalDateTime;

public record VenteResponseDTO(
    Long idVente,
    String nomProduit,
    double prixVente,
    int quantite,
    Double remise,
    double montantTotal,
    LocalDateTime dateVente
) {}
