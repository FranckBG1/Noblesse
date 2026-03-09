package com.myApp.Noblesse.services;

import com.myApp.Noblesse.Entities.JournalAction;
import com.myApp.Noblesse.Repositories.JournalActionRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class JournalActionService {
    private final JournalActionRepository repository;

    public JournalActionService(JournalActionRepository repository) {
        this.repository = repository;
    }

    public void enregistrerAction(String utilisateur, String action) {
        repository.save(new JournalAction(utilisateur, action));
    }

    public void enregistrerAction(String utilisateur, String action, String typeAction) {
        repository.save(new JournalAction(utilisateur, action, typeAction));
    }

    public List<JournalAction> listerHistoriqueRecente() {
        return repository.findTop10ByOrderByDateActionDesc();
    }

    public List<JournalAction> listerHistorique() {
        return repository.findAllByOrderByDateActionDesc();
    }

    public List<JournalAction> filtrerParUtilisateur(String utilisateur) {
        if (utilisateur == null || utilisateur.isBlank() || "null".equals(utilisateur) || "undefined".equals(utilisateur)) {
            return listerHistorique();
        }
        return repository.findByUtilisateurContainingIgnoreCaseOrderByDateActionDesc(utilisateur);
    }

    public List<JournalAction> filtrerParType(String type) {
        if (type == null || type.isBlank()) {
            return listerHistorique();
        }
        return repository.findByTypeActionOrderByDateActionDesc(type);
    }
}
