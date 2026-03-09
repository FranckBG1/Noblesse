package com.myApp.Noblesse.Controllers;

import com.myApp.Noblesse.Entities.JournalAction;
import com.myApp.Noblesse.services.JournalActionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/journal")
public class JournalActionController {
    private final JournalActionService journalActionService;

    public JournalActionController(JournalActionService journalActionService) {
        this.journalActionService = journalActionService;
    }

    @GetMapping("/historique")
    public List<JournalAction> getHistorique(@RequestParam(required = false) String utilisateur) {
        System.out.println("Requête historique reçue avec filtre utilisateur : [" + utilisateur + "]");
        return journalActionService.filtrerParUtilisateur(utilisateur);
    }

    @GetMapping("/type")
    public List<JournalAction> getHistoriqueParType(@RequestParam(required = false) String type) {
        return journalActionService.filtrerParType(type);
    }
}
