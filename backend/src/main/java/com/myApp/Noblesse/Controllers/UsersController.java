package com.myApp.Noblesse.Controllers;

import com.myApp.Noblesse.DTO.UsersRequestDTO;
import com.myApp.Noblesse.DTO.UsersResponseDTO;
import com.myApp.Noblesse.DTO.UsersUpdateDTO;
import com.myApp.Noblesse.Entities.Users;
import com.myApp.Noblesse.services.UsersService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usersUU")
public class UsersController {

    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    //  CRÉER UTILISATEUR
    @PostMapping("/creer")
    public ResponseEntity<Void> creerUtilisateur(@Valid @RequestBody UsersRequestDTO request) {
        usersService.creerUtilisateur(
                request.getNom(),
                request.getMotDePasse(),
                request.isAdmin(),
                request.getMotDePasseAdmin()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //  LISTER UTILISATEURS
    @GetMapping("/lister")
    public ResponseEntity<List<UsersResponseDTO>> listerUtilisateurs() {
        List<Users> users = usersService.listerUtilisateurs();
        List<UsersResponseDTO> resultat =
                users.stream().map(UsersService::mapToResponseDTO).toList();

        return ResponseEntity.ok(resultat);
    }

    //  TROUVER UTILISATEUR PAR ID
    @GetMapping("/trouver/{id}")
    public ResponseEntity<UsersResponseDTO> trouverUtilisateurParId(@PathVariable String id) {
        Users user = usersService.trouverUtilisateurParId(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        return ResponseEntity.ok(UsersService.mapToResponseDTO(user));
    }

    //  SUPPRIMER UTILISATEUR (par un admin)
    @DeleteMapping("/supprimer/{idAdmin}/{idASupprimer}")
    public ResponseEntity<Void> supprimerUtilisateur(
            @PathVariable String idAdmin,
            @PathVariable String idASupprimer,
            @RequestParam(required = false) String motDePasseAdmin
    ) {
        usersService.supprimerUtilisateur(idASupprimer, idAdmin, motDePasseAdmin);
        return ResponseEntity.noContent().build();
    }

    //  MODIFIER UTILISATEUR (MDP uniquement ou nom si admin)
    @PutMapping("/modifier/{id}")
    public ResponseEntity<Void> modifierUtilisateur(
            @PathVariable String id,
            @RequestBody UsersUpdateDTO user
    ) {
        usersService.modifierUtilisateur(id, user);
        return ResponseEntity.ok().build();
    }

    //  CONNECTER
    @PostMapping("/connecter")
    public ResponseEntity<UsersResponseDTO> connecter(@RequestBody UsersRequestDTO request) {
        Users user = usersService.connecter(
                request.getIdUsers(),
                request.getMotDePasse()
        );

        return ResponseEntity.ok(UsersService.mapToResponseDTO(user));
    }

    //  DÉCONNECTER
    @PostMapping("/deconnecter")
    public ResponseEntity<String> deconnecter() {
        usersService.deconnecter();
        return ResponseEntity.ok("Déconnexion réussie.");
    }
}
