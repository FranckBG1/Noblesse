package com.myApp.Noblesse.Entities;

import jakarta.persistence.*;




@Entity
@Table(name = "produit")
public class Produit {

    @Id
    @Column(name = "id_produit", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProduit;                   // SQLite INTEGER PRIMARY KEY

    @Column(nullable = false)
    private String designation;               // SQLite TEXT NOT NULL

    @Column(name = "code")
    private String code;                    // SQLite TEXT (nullable)

    @Column(name = "photo")
    private String photo;                   // SQLite TEXT (nullable)

    @Column(name = "quantite")
    private Double quantite;                 // SQLite REAL (nullable)

    @Column(name = "dernier_prix")
    private Double dernierPrix;               // SQLite REAL (nullable)

    @Column(name = "prix_unitaire", nullable = false)
    private Double prixUnitaire;              // SQLite REAL NOT NULL

    public Produit() {}

    public Produit(Long idProduit, String designation, String code, String photo,
                   Double quantite, Double dernierPrix, Double prixUnitaire) {
        this.idProduit   = idProduit;
        this.designation = designation;
        this.code        = code;
        this.photo       = photo;
        this.quantite    = quantite;
        this.dernierPrix = dernierPrix;
        this.prixUnitaire= prixUnitaire;
    }

    // — Getters & Setters —

    public Long getIdProduit() {
        return idProduit;
    }
    public void setIdProduit(Long idProduit) {
        this.idProduit = idProduit;
    }

    public String getDesignation() {
        return designation;
    }
    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }

    public String getPhoto() {
        return photo;
    }
    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public Double getQuantite() {
        return quantite;
    }
    public void setQuantite(Double quantite) {
        this.quantite = quantite;
    }

    public Double getDernierPrix() {
        return dernierPrix;
    }
    public void setDernierPrix(Double dernierPrix) {
        this.dernierPrix = dernierPrix;
    }

    public Double getPrixUnitaire() {
        return prixUnitaire;
    }
    public void setPrixUnitaire(Double prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }
}
