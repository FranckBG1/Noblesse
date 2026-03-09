package com.myApp.Noblesse.Controllers;

import com.myApp.Noblesse.Entities.Client;
import com.myApp.Noblesse.services.ClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public ResponseEntity<List<Client>> listerClients() {
        return ResponseEntity.ok(clientService.listerClients());
    }

    @GetMapping("/rechercher")
    public ResponseEntity<List<Client>> rechercherClients(@RequestParam String nom) {
        return ResponseEntity.ok(clientService.rechercherClients(nom));
    }
}
