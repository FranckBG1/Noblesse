package com.myApp.Noblesse.Entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class Users {

    @Id
    @Column(name = "id_users", nullable = false)
    private String idUsers;

    private String nom;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin;

    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    @Column(name = "tentatives_echouees")
    private int tentativesEchouees = 0;

    @Column(name = "date_verrouillage")
    private LocalDateTime dateVerrouillage;

    @Column(name = "derniere_connexion")
    private LocalDateTime derniereConnexion;

    public Users() {}

    public Users(String idUsers, String nom, boolean isAdmin, String motDePasse) {
        this.idUsers = idUsers;
        this.nom = nom;
        this.isAdmin = isAdmin;
        this.motDePasse = motDePasse;
    }

    // Getters & setters

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

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public int getTentativesEchouees() {
        return tentativesEchouees;
    }

    public void setTentativesEchouees(int tentativesEchouees) {
        this.tentativesEchouees = tentativesEchouees;
    }

    public LocalDateTime getDateVerrouillage() {
        return dateVerrouillage;
    }

    public void setDateVerrouillage(LocalDateTime dateVerrouillage) {
        this.dateVerrouillage = dateVerrouillage;
    }

    public LocalDateTime getDerniereConnexion() {
        return derniereConnexion;
    }

    public void setDerniereConnexion(LocalDateTime derniereConnexion) {
        this.derniereConnexion = derniereConnexion;
    }
}
