package com.myApp.Noblesse.DTO;

public class ProduitUpdateDTO {
    private String designation;
    private String code;
    private String photo;
    private double quantite;
    private double dernierPrix;
    private double prixUnitaire;
    private String motDePasseAdmin;

    // Getters and Setters
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    public double getQuantite() { return quantite; }
    public void setQuantite(double quantite) { this.quantite = quantite; }

    public double getDernierPrix() { return dernierPrix; }
    public void setDernierPrix(double dernierPrix) { this.dernierPrix = dernierPrix; }

    public double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(double prixUnitaire) { this.prixUnitaire = prixUnitaire; }

    public String getMotDePasseAdmin() { return motDePasseAdmin; }
    public void setMotDePasseAdmin(String motDePasseAdmin) { this.motDePasseAdmin = motDePasseAdmin; }
}
