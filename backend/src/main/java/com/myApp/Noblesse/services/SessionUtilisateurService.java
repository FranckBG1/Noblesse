package com.myApp.Noblesse.services;

import com.myApp.Noblesse.Entities.Users;
import org.springframework.stereotype.Service;

@Service
public class SessionUtilisateurService {

    private Users utilisateurActif;

    public void connecter(Users utilisateur) {
        this.utilisateurActif = utilisateur;
    }

    public void deconnecter() {
        this.utilisateurActif = null;
    }

    public boolean estConnecte() {
        return this.utilisateurActif != null;
    }

    public boolean estAdmin() {
        return estConnecte() && utilisateurActif.isAdmin();
    }

    public Users getUtilisateurActif() {
        return utilisateurActif;
    }
}
