package com.myApp.Noblesse.Repositories;

import com.myApp.Noblesse.Entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface UsersRepository extends JpaRepository<Users, String> {
    boolean existsById(String idUsers);

    // Pour la connexion
    Optional<Users> findByIdUsersAndMotDePasse(String idUsers, String motDePasse);

    // Pour générer un nouvel id_users unique
    @Query("SELECT u.idUsers FROM Users u WHERE u.idUsers LIKE CONCAT(:nom, '%')")
    List<String> findAllIdsStartingWith(@Param("nom") String nom);
}
