package com.myApp.Noblesse.Entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "journal_actions")
public class JournalAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "utilisateur", nullable = false)
    private String utilisateur; // Nom ou ID de l'utilisateur ayant fait l'action

    @Column(name = "action", nullable = false)
    private String action; // Description de l'action (ex: "A vendu 3 ampoules")

    @Column(name = "type_action")
    private String typeAction; // VENTE, CONNEXION, FACTURE, PRODUIT, etc.

    @Column(name = "date_action", nullable = false)
    private LocalDateTime dateAction;

    public JournalAction() {
        this.dateAction = LocalDateTime.now();
    }

    public JournalAction(String utilisateur, String action) {
        this.utilisateur = utilisateur;
        this.action = action;
        this.dateAction = LocalDateTime.now();
    }

    public JournalAction(String utilisateur, String action, String typeAction) {
        this.utilisateur = utilisateur;
        this.action = action;
        this.typeAction = typeAction;
        this.dateAction = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUtilisateur() { return utilisateur; }
    public void setUtilisateur(String utilisateur) { this.utilisateur = utilisateur; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getTypeAction() { return typeAction; }
    public void setTypeAction(String typeAction) { this.typeAction = typeAction; }
    public LocalDateTime getDateAction() { return dateAction; }
    public void setDateAction(LocalDateTime dateAction) { this.dateAction = dateAction; }
}
