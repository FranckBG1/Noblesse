package com.myApp.Noblesse.Repositories;

import com.myApp.Noblesse.Entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByNomIgnoreCase(String nom);
    List<Client> findByNomContainingIgnoreCase(String nom);
    
    @Query("SELECT SUM(f.avance) FROM Facture f WHERE f.client.idClient = :clientId AND f.typeFacture = 'DEPOT'")
    Double getTotalDepots(@Param("clientId") Long clientId);
    
    @Query("SELECT SUM(f.avance) FROM Facture f WHERE f.client.idClient = :clientId AND f.typeFacture = 'RETRAIT'")
    Double getTotalRetraits(@Param("clientId") Long clientId);
}
