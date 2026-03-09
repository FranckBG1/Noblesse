package com.myApp.Noblesse.Entities;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "factures")
public class Facture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFacture;

    @ManyToOne
    @JoinColumn(name = "id_client")
    private Client client;

    @Column(name = "type_facture")
    private String typeFacture; // CLASSIQUE, DEPOT

    @Column(name = "status")
    private String status; // ENREGISTRE, TERMINEE

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "derniere_modif")
    private LocalDateTime derniereModif;

    @Column(name = "avance")
    private Double avance = 0.0;

    @Column(name = "reste")
    private Double reste = 0.0;

    @Column(name = "explications", columnDefinition = "TEXT")
    private String explications;

    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("facture")
    private List<Ventes> ventes;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur")
    private Users utilisateur;

    public Facture() {
        this.dateCreation = LocalDateTime.now();
        this.derniereModif = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getIdFacture() { return idFacture; }
    public void setIdFacture(Long idFacture) { this.idFacture = idFacture; }
    public String getNomClient() { return client != null ? client.getNom() : null; }
    public void setNomClient(String nomClient) { } // Deprecated
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    public LocalDateTime getDerniereModif() { return derniereModif; }
    public void setDerniereModif(LocalDateTime derniereModif) { this.derniereModif = derniereModif; }
    public List<Ventes> getVentes() { return ventes; }
    public void setVentes(List<Ventes> ventes) { this.ventes = ventes; }

    public Users getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Users utilisateur) { this.utilisateur = utilisateur; }
    public Double getAvance() { return avance; }
    public void setAvance(Double avance) { this.avance = avance; }
    public Double getReste() { return reste; }
    public void setReste(Double reste) { this.reste = reste; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public String getTypeFacture() { return typeFacture; }
    public void setTypeFacture(String typeFacture) { this.typeFacture = typeFacture; }
    public String getExplications() { return explications; }
    public void setExplications(String explications) { this.explications = explications; }
}
