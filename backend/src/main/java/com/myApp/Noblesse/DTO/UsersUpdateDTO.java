package com.myApp.Noblesse.DTO;

import jakarta.validation.constraints.NotBlank;

public class UsersUpdateDTO {
    private String motDePasse;
    private String nom;
    private Boolean isAdmin;
    private String motDePasseAdmin;

    public void setNom(String nom) {
        this.nom = nom;
    }
    public String getNom() {
        return nom;
    }
    public String getMotDePasse() {
        return motDePasse;
    }
    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }
    public Boolean getIsAdmin() {
        return isAdmin;
    }
    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
    public String getMotDePasseAdmin() {
        return motDePasseAdmin;
    }
    public void setMotDePasseAdmin(String motDePasseAdmin) {
        this.motDePasseAdmin = motDePasseAdmin;
    }
}
