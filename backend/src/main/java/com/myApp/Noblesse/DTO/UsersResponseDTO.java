package com.myApp.Noblesse.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class UsersResponseDTO {

    private String idUsers;
    private String nom;
    @JsonProperty("isAdmin")
    private boolean isAdmin;
    private LocalDateTime derniereConnexion;

    public UsersResponseDTO(String idUsers, String nom, boolean isAdmin, LocalDateTime derniereConnexion) {
        this.idUsers = idUsers;
        this.nom = nom;
        this.isAdmin = isAdmin;
        this.derniereConnexion = derniereConnexion;
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
    
    @JsonProperty("isAdmin")
    public boolean isAdmin() {
        return isAdmin;
    }
    
    @JsonProperty("isAdmin")
    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public LocalDateTime getDerniereConnexion() {
        return derniereConnexion;
    }

    public void setDerniereConnexion(LocalDateTime derniereConnexion) {
        this.derniereConnexion = derniereConnexion;
    }
}
