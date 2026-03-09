package com.myApp.Noblesse.services;

import com.myApp.Noblesse.DTO.UsersResponseDTO;
import com.myApp.Noblesse.DTO.UsersUpdateDTO;
import com.myApp.Noblesse.Entities.Users;
import com.myApp.Noblesse.Repositories.UsersRepository;
import com.myApp.Noblesse.exceptions.CompteVerrouilleException;
import com.myApp.Noblesse.exceptions.IdentifiantsInvalidesException;
import com.myApp.Noblesse.exceptions.RequeteInvalidException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(noRollbackFor = {IdentifiantsInvalidesException.class, CompteVerrouilleException.class})
public class UsersService implements UserDetailsService {
    private UsersRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private SessionUtilisateurService sessionUtilisateurService;
    private JournalActionService journalActionService;

    private static final int MAX_TENTATIVES = 5;
    private static final int DELAI_VERROUILLAGE_MINUTES = 1;

    public UsersService(UsersRepository userRepository, PasswordEncoder passwordEncoder, 
                        SessionUtilisateurService sessionUtilisateurService, 
                        JournalActionService journalActionService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionUtilisateurService = sessionUtilisateurService;
        this.journalActionService = journalActionService;
    }

    @Override
    public UserDetails loadUserByUsername(String idUsers) throws UsernameNotFoundException {
        Users user = userRepository.findById(idUsers)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + idUsers));

        return new org.springframework.security.core.userdetails.User(
                user.getIdUsers(),
                user.getMotDePasse(),
                Collections.singletonList(new SimpleGrantedAuthority(user.isAdmin() ? "ROLE_ADMIN" : "ROLE_USER"))
        );
    }

    public String genererIdUtilisateur(String nom) {
        List<String> ids = userRepository.findAllIdsStartingWith(nom);
        int max = 0;
        for (String id : ids) {
            String suffix = id.replace(nom, "");
            if (suffix.matches("\\d+")) {
                max = Math.max(max, Integer.parseInt(suffix));
            }
        }
        return nom + (max + 1);
    }

    public Users creerUtilisateur(String nom, String motDePasse, boolean isAdmin, String motDePasseAdmin) {
        Users adminActuel = sessionUtilisateurService.getUtilisateurActif();
        if (adminActuel == null || motDePasseAdmin == null || !passwordEncoder.matches(motDePasseAdmin, adminActuel.getMotDePasse())) {
            throw new RequeteInvalidException("Mot de passe administrateur incorrect.");
        }

        String id = genererIdUtilisateur(nom);
        Users user = new Users();
        user.setIdUsers(id);
        user.setNom(nom);

        // Hash du mot de passe
        String hashedPassword = this.passwordEncoder.encode(motDePasse);
        user.setMotDePasse(hashedPassword);

        user.setAdmin(isAdmin);
        Users savedUser = this.userRepository.save(user);
        journalActionService.enregistrerAction(adminActuel.getNom(), "A créé l'utilisateur : " + nom);
        return savedUser;
    }

    public List<Users> listerUtilisateurs() {
        return this.userRepository.findAll();
    }

    public Optional<Users> trouverUtilisateurParId(String id) {
        return userRepository.findById(id);
    }

    public void supprimerUtilisateur(String idASupprimer, String idDemandeur, String motDePasseAdmin) {
        Users demandeur = userRepository.findById(idDemandeur)
                .orElseThrow(() -> new RuntimeException("Demandeur introuvable"));

        Users cible = userRepository.findById(idASupprimer)
                .orElseThrow(() -> new RuntimeException("Utilisateur à supprimer introuvable"));

        if (!demandeur.isAdmin()) {
            throw new RuntimeException("Permission refusée : seul un admin peut supprimer un utilisateur.");
        }

        if (idASupprimer.equals(idDemandeur)) {
            throw new RuntimeException("Vous ne pouvez pas vous supprimer vous-même.");
        }

        if (cible.isAdmin()) {
            if (motDePasseAdmin == null || !passwordEncoder.matches(motDePasseAdmin, demandeur.getMotDePasse())) {
                throw new RequeteInvalidException("Mot de passe administrateur incorrect.");
            }
        }

        userRepository.deleteById(idASupprimer);
        journalActionService.enregistrerAction(demandeur.getNom(), "A supprimé l'utilisateur : " + cible.getNom());
    }

    public static UsersResponseDTO mapToResponseDTO(Users user) {
        return new UsersResponseDTO(user.getIdUsers(), user.getNom(), user.isAdmin(), user.getDerniereConnexion());
    }

    public Users modifierUtilisateur(String id, UsersUpdateDTO majUtilisateur) {
        Users utilisateurExistant = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        StringBuilder mods = new StringBuilder();
        Users adminActuel = sessionUtilisateurService.getUtilisateurActif();

        boolean nomFournit = majUtilisateur.getNom() != null && !majUtilisateur.getNom().isEmpty();
        boolean nomChange = nomFournit && !utilisateurExistant.getNom().equalsIgnoreCase(majUtilisateur.getNom());

        if (nomChange) {
            if (!sessionUtilisateurService.estAdmin()) {
                throw new RuntimeException("Seul un administrateur peut modifier le nom.");
            }
            
            mods.append("Nom: [").append(utilisateurExistant.getNom()).append(" -> ").append(majUtilisateur.getNom()).append("] ");

            String nouvelId = genererIdUtilisateurUnique(majUtilisateur.getNom());
            userRepository.deleteById(id);

            Users nouvelUtilisateur = new Users();
            nouvelUtilisateur.setIdUsers(nouvelId);
            nouvelUtilisateur.setNom(majUtilisateur.getNom());
            
            if (majUtilisateur.getIsAdmin() != null) {
                if (majUtilisateur.getIsAdmin() != utilisateurExistant.isAdmin()) {
                    if (adminActuel == null || majUtilisateur.getMotDePasseAdmin() == null ||
                            !passwordEncoder.matches(majUtilisateur.getMotDePasseAdmin(), adminActuel.getMotDePasse())) {
                        throw new RequeteInvalidException("Mot de passe administrateur incorrect.");
                    }
                    mods.append("Rôle: [").append(utilisateurExistant.isAdmin()?"Admin":"Vendeur").append(" -> ").append(majUtilisateur.getIsAdmin()?"Admin":"Vendeur").append("] ");
                }
                nouvelUtilisateur.setAdmin(majUtilisateur.getIsAdmin());
            } else {
                nouvelUtilisateur.setAdmin(utilisateurExistant.isAdmin());
            }

            if (majUtilisateur.getMotDePasse() != null && !majUtilisateur.getMotDePasse().isEmpty()) {
                if (passwordEncoder.matches(majUtilisateur.getMotDePasse(), utilisateurExistant.getMotDePasse())) {
                    throw new RuntimeException("mdp identique au précedent");
                }
                nouvelUtilisateur.setMotDePasse(passwordEncoder.encode(majUtilisateur.getMotDePasse()));
                mods.append("Mot de passe: [Modifié] ");
            } else {
                nouvelUtilisateur.setMotDePasse(utilisateurExistant.getMotDePasse());
            }

            Users saved = userRepository.save(nouvelUtilisateur);
            journalActionService.enregistrerAction(adminActuel.getNom(), "A modifié l'utilisateur : " + majUtilisateur.getNom() + " (Détails: " + mods.toString().trim() + ")");
            return saved;
        }

        // Si nom inchangé
        if (majUtilisateur.getMotDePasse() != null && !majUtilisateur.getMotDePasse().isEmpty()) {
            if (passwordEncoder.matches(majUtilisateur.getMotDePasse(), utilisateurExistant.getMotDePasse())) {
                throw new RuntimeException("mdp identique au précedent");
            }
            utilisateurExistant.setMotDePasse(passwordEncoder.encode(majUtilisateur.getMotDePasse()));
            mods.append("Mot de passe: [Modifié] ");
        }

        if (majUtilisateur.getIsAdmin() != null) {
            if (majUtilisateur.getIsAdmin() != utilisateurExistant.isAdmin()) {
                if (adminActuel == null || majUtilisateur.getMotDePasseAdmin() == null ||
                        !passwordEncoder.matches(majUtilisateur.getMotDePasseAdmin(), adminActuel.getMotDePasse())) {
                    throw new RequeteInvalidException("Mot de passe administrateur incorrect.");
                }
                mods.append("Rôle: [").append(utilisateurExistant.isAdmin()?"Admin":"Vendeur").append(" -> ").append(majUtilisateur.getIsAdmin()?"Admin":"Vendeur").append("] ");
            }
            utilisateurExistant.setAdmin(majUtilisateur.getIsAdmin());
        }

        Users saved = userRepository.save(utilisateurExistant);
        String msg = "A modifié l'utilisateur : " + utilisateurExistant.getNom();
        if (mods.length() > 0) {
            msg += " (Détails: " + mods.toString().trim() + ")";
        }
        journalActionService.enregistrerAction(adminActuel.getNom(), msg);
        return saved;
    }

    public String genererIdUtilisateurUnique(String nom) {
        int suffixe = 1;
        String base = nom.toLowerCase().replaceAll("\\s+", "");
        String candidat = base + suffixe;

        while (userRepository.existsById(candidat)) {
            suffixe++;
            candidat = base + suffixe;
        }

        return candidat;
    }

    public Users connecter(String idUsers, String motDePasse) {
        try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        Users utilisateur = userRepository.findById(idUsers)
                .orElseThrow(() -> new IdentifiantsInvalidesException("Utilisateur ou mot de passe incorrect"));

        System.out.println("Tentative de connexion pour : " + idUsers);

        // Vérifier si le compte est verrouillé
        if (utilisateur.getDateVerrouillage() != null) {
            if (utilisateur.getDateVerrouillage().isAfter(LocalDateTime.now())) {
                System.out.println("Compte bloqué jusqu'à : " + utilisateur.getDateVerrouillage());
                throw new CompteVerrouilleException("Compte verrouillé. Réessayez après " + utilisateur.getDateVerrouillage());
            } else {
                utilisateur.setTentativesEchouees(0);
                utilisateur.setDateVerrouillage(null);
                userRepository.saveAndFlush(utilisateur);
            }
        }

        if (!passwordEncoder.matches(motDePasse, utilisateur.getMotDePasse())) {
            int tentatives = utilisateur.getTentativesEchouees() + 1;
            utilisateur.setTentativesEchouees(tentatives);
            System.out.println("Échec mdp. Tentative n°" + tentatives);

            if (tentatives >= MAX_TENTATIVES) {
                utilisateur.setDateVerrouillage(LocalDateTime.now().plusMinutes(DELAI_VERROUILLAGE_MINUTES));
                userRepository.saveAndFlush(utilisateur);
                throw new CompteVerrouilleException("Trop de tentatives. Compte verrouillé pour 1 minute.");
            } else {
                userRepository.saveAndFlush(utilisateur);
                int restantes = MAX_TENTATIVES - tentatives;
                throw new IdentifiantsInvalidesException("Identifiants incorrects. Tentatives restantes : " + restantes);
            }
        }

        // Succès : réinitialiser les tentatives et mettre à jour la date de connexion
        utilisateur.setTentativesEchouees(0);
        utilisateur.setDateVerrouillage(null);
        utilisateur.setDerniereConnexion(LocalDateTime.now());
        userRepository.saveAndFlush(utilisateur);

        journalActionService.enregistrerAction(utilisateur.getNom(), "s'est connecté", "CONNEXION");

        sessionUtilisateurService.connecter(utilisateur);
        return utilisateur;
    }

    public void deconnecter() {
        sessionUtilisateurService.deconnecter();
    }
}
