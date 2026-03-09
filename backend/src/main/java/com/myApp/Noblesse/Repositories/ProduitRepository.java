package com.myApp.Noblesse.Repositories;

import com.myApp.Noblesse.Entities.Produit;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProduitRepository extends JpaRepository<Produit, Long> {
    List<Produit> findAllByDesignationContainingIgnoreCase(String keyword);
    List<Produit> findByDesignationContainingIgnoreCase(String designation);
    List<Produit> findByCodeContainingIgnoreCase(String code);
    long countByQuantiteLessThanEqual(Double quantite);
    Page<Produit> findByQuantiteLessThanEqual(Double quantite, org.springframework.data.domain.Pageable pageable);
    @Modifying
    @Transactional
    @Query("UPDATE Produit p SET p.quantite = p.quantite - :quantiteRetiree WHERE p.idProduit = :idProduit")
    void diminuerQuantite(@Param("idProduit") Long idProduit, @Param("quantiteRetiree") double quantiteRetiree);
}
