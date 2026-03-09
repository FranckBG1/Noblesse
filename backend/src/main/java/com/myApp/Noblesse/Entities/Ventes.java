package com.myApp.Noblesse.Entities;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@Entity
@Table(name = "ventes")
public class Ventes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVente;

    @ManyToOne
    @JoinColumn(name = "id_produit", nullable = true) // Nullable car on peut vendre un produit hors stock
    private Produit produit;
    @Column(name = "nom_produit_hors_stock")
    private String nomProduitHorsStock; // Si le produit n'existe pas en BD
    @Column(name = "prix_vente", nullable = false)
    private double prixVente;
    @Column(name = "quantite", nullable = false)
    private int quantite;
    @Column(name = "remise")
    private Double remise; // Peut être null
    @Column(name = "date_vente", nullable = false)
    private LocalDateTime dateVente;
    @Column(name = "montant_total", nullable = false)
    private double montantTotal;

    @ManyToOne
    @JoinColumn(name = "id_facture")
    @JsonIgnoreProperties("ventes")
    private Facture facture;

    public Ventes() {
        this.dateVente = LocalDateTime.now();
    }

    public Ventes(Produit produit, double prixVente, int quantite, Double remise) {
        this.produit = produit;
        this.prixVente = prixVente;
        this.quantite = quantite;
        this.remise = remise;
        this.dateVente = LocalDateTime.now();
        this.calculerMontantTotal();
    }

    public Ventes(String nomProduitHorsStock, double prixVente, int quantite, Double remise) {
        this.nomProduitHorsStock = nomProduitHorsStock;
        this.prixVente = prixVente;
        this.quantite = quantite;
        this.remise = remise;
        this.dateVente = LocalDateTime.now();
        this.calculerMontantTotal();
    }

    private void calculerMontantTotal() {
        double r = (remise != null) ? remise : 0.0;
        this.montantTotal = (prixVente * quantite) - r;
    }

    public Long getIdVente() {
        return idVente;
    }

    public void setIdVente(Long idVente) {
        this.idVente = idVente;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public String getNomProduitHorsStock() {
        return nomProduitHorsStock;
    }

    public void setNomProduitHorsStock(String nomProduitHorsStock) {
        this.nomProduitHorsStock = nomProduitHorsStock;
    }

    public double getPrixVente() {
        return prixVente;
    }

    public void setPrixVente(double prixVente) {
        this.prixVente = prixVente;
        this.calculerMontantTotal();
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
        this.calculerMontantTotal();
    }

    public Double getRemise() {
        return remise;
    }

    public void setRemise(Double remise) {
        this.remise = remise;
        this.calculerMontantTotal();
    }

    public LocalDateTime getDateVente() {
        return dateVente;
    }

    public void setDateVente(LocalDateTime dateVente) {
        this.dateVente = dateVente;
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
    }

    public Facture getFacture() { return facture; }
    public void setFacture(Facture facture) { this.facture = facture; }

}



