package com.myApp.Noblesse.Repositories;

import com.myApp.Noblesse.Entities.JournalAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalActionRepository extends JpaRepository<JournalAction, Long> {
    List<JournalAction> findAllByOrderByDateActionDesc();
    List<JournalAction> findTop10ByOrderByDateActionDesc();
    List<JournalAction> findTop5ByOrderByDateActionDesc();
    List<JournalAction> findByUtilisateurContainingIgnoreCaseOrderByDateActionDesc(String utilisateur);
    List<JournalAction> findByTypeActionOrderByDateActionDesc(String typeAction);
}
