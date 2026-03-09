package com.myApp.Noblesse.DTO;

public record VenteRequestDTO(
    Long idProduit,          // Null si produit hors stock
    String nomProduitHorsStock, 
    double prixVente,
    int quantite,
    Double remise
) {}
