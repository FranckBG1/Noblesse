package com.myApp.Noblesse.Repositories;

import com.myApp.Noblesse.Entities.Facture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> {
    List<Facture> findByStatus(String status);
    Page<Facture> findByStatus(String status, Pageable pageable);
    Page<Facture> findByStatusAndClientNomContainingIgnoreCase(String status, String nom, Pageable pageable);
    List<Facture> findByStatusAndDateCreationBetween(String status, LocalDateTime start, LocalDateTime end);

    @Query("SELECT c.nom, SUM(v.montantTotal) as total FROM Facture f JOIN f.client c JOIN f.ventes v WHERE f.status = 'TERMINEE' GROUP BY c.nom ORDER BY total DESC")
    List<Object[]> findTopClientsByCA();

    @Query("SELECT c.nom, COUNT(f) as total FROM Facture f JOIN f.client c WHERE f.status = 'TERMINEE' GROUP BY c.nom ORDER BY total DESC")
    List<Object[]> findTopClientsByRepetition();
}
