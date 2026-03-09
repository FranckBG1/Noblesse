package com.myApp.Noblesse.Repositories;

import com.myApp.Noblesse.Entities.Ventes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VentesRepository extends JpaRepository<Ventes, Long> {
    List<Ventes> findByDateVenteBetween(LocalDateTime start, LocalDateTime end);
    List<Ventes> findTop5ByOrderByDateVenteDesc();
    List<Ventes> findByDateVenteAfterOrderByDateVenteDesc(LocalDateTime date);
}
