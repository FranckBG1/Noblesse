package com.myApp.Noblesse.Entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "clients")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idClient;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Transient
    private Double soldeDisponible;

    public Client() {
        this.dateCreation = LocalDateTime.now();
    }

    public Client(String nom) {
        this.nom = nom;
        this.dateCreation = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getIdClient() { return idClient; }
    public void setIdClient(Long idClient) { this.idClient = idClient; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    public Double getSoldeDisponible() { return soldeDisponible; }
    public void setSoldeDisponible(Double soldeDisponible) { this.soldeDisponible = soldeDisponible; }
}
