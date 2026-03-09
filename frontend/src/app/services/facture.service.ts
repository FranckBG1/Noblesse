import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

export interface Vente {
  idVente: number;
  produit?: any;
  nomProduitHorsStock?: string;
  prixVente: number;
  quantite: number;
  remise?: number;
  dateVente: string;
  montantTotal: number;
  facture?: Facture;
}

export interface Facture {
  idFacture: number;
  nomClient: string;
  status: string;
  dateCreation: string;
  derniereModif: string;
  ventes: Vente[];
  utilisateur?: any;
  avance?: number;
  reste?: number;
  typeFacture?: string;
  client?: Client;
  explications?: string;
}

export interface Client {
  idClient: number;
  nom: string;
  telephone?: string;
  dateCreation: string;
  soldeDisponible?: number;
}

@Injectable({
  providedIn: 'root'
})
export class FactureService {
  private apiUrl = 'http://localhost:8080/factures';
  private clientApiUrl = 'http://localhost:8080/clients';

  constructor(private http: HttpClient, private authService: AuthService) {}

  getFacturesIncompletes(): Observable<Facture[]> {
    return this.http.get<Facture[]>(`${this.apiUrl}/incompletes`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  getFacturesTerminees(page: number = 0, size: number = 10, nomClient: string = ''): Observable<any> {
    let url = `${this.apiUrl}/terminees?page=${page}&size=${size}`;
    if (nomClient) url += `&nomClient=${nomClient}`;
    return this.http.get<any>(url, {
      headers: this.authService.getAuthHeaders()
    });
  }

  getFacturesTermineesDuJour(): Observable<Facture[]> {
    return this.http.get<Facture[]>(`${this.apiUrl}/terminees/du-jour`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  getTopClientsCA(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/top-clients/ca`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  getTopClientsRepetition(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/top-clients/repetition`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  supprimerFacture(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/supprimer/${id}`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  getVentesDuJour(): Observable<Vente[]> {
    return this.http.get<Vente[]>(`${this.apiUrl}/ventes/du-jour`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  getVentesDernierMois(): Observable<Vente[]> {
    return this.http.get<Vente[]>(`${this.apiUrl}/ventes/dernier-mois`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  enregistrerFacture(nomClient: string, ventesIds: number[], terminee: boolean, avance: number, typeFacture: string = 'CLASSIQUE', telephone?: string, clientId?: number | null, explications?: string): Observable<Facture> {
    return this.http.post<Facture>(`${this.apiUrl}/enregistrer`, { nomClient, ventesIds, terminee, avance, typeFacture, telephone, clientId, explications }, {
      headers: this.authService.getAuthHeaders()
    });
  }

  modifierFacture(id: number, nomClient: string, ventesIds: number[], terminee: boolean, avance: number, typeFacture: string = 'CLASSIQUE', telephone?: string, clientId?: number | null, explications?: string): Observable<Facture> {
    return this.http.put<Facture>(`${this.apiUrl}/modifier/${id}`, { nomClient, ventesIds, terminee, avance, typeFacture, telephone, clientId, explications }, {
      headers: this.authService.getAuthHeaders()
    });
  }

  rechercherClients(nom: string): Observable<Client[]> {
    return this.http.get<Client[]>(`${this.clientApiUrl}/rechercher?nom=${nom}`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  listerClients(): Observable<Client[]> {
    return this.http.get<Client[]>(`${this.clientApiUrl}`, {
      headers: this.authService.getAuthHeaders()
    });
  }
}
