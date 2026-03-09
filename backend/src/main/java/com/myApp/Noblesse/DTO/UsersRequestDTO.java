package com.myApp.Noblesse.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UsersRequestDTO {
    private String idUsers;
    private String nom;
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 4, message = "Le mot de passe doit contenir au moins 4 caractères")
    private String motDePasse;
    
    @JsonProperty("isAdmin")
    private boolean isAdmin;

    private String motDePasseAdmin;

    public UsersRequestDTO() {}

    public UsersRequestDTO(String idUsers, String nom, String motDePasse, boolean isAdmin, String motDePasseAdmin) {
        this.idUsers = idUsers;
        this.nom = nom;
        this.motDePasse = motDePasse;
        this.isAdmin = isAdmin;
        this.motDePasseAdmin = motDePasseAdmin;
    }

    public String getMotDePasseAdmin() {
        return motDePasseAdmin;
    }

    public void setMotDePasseAdmin(String motDePasseAdmin) {
        this.motDePasseAdmin = motDePasseAdmin;
    }

    public String getIdUsers() {
        return idUsers;
    }

    public void setIdUsers(String idUsers) {
        this.idUsers = idUsers;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
    public String getMotDePasse() {
        return motDePasse;
    }
    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }
    
    @JsonProperty("isAdmin")
    public boolean isAdmin() {
        return isAdmin;
    }
    
    @JsonProperty("isAdmin")
    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}
