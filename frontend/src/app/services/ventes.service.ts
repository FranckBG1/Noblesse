import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

// Interface pour structurer les données envoyées au backend
export interface VenteRequest {
  idProduit?: number;
  nomProduitHorsStock?: string;
  prixVente: number;
  quantite: number;
  remise?: number;
}

export interface VenteResponse {
  idVente: number;
  nomProduit: string;
  prixVente: number;
  quantite: number;
  remise?: number;
  montantTotal: number;
  dateVente: string;
}

@Injectable({ providedIn: 'root' })
export class VenteService {
  private apiUrl = 'http://localhost:8080/ventes';

  constructor(
    private http: HttpClient,
    private authService: AuthService,
  ) {}

  enregistrerVente(data: VenteRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/enregistrer`, data, {
      headers: this.authService.getAuthHeaders(),
    });
  }

  rechercherProduits(keyword: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/rechercher?keyword=${keyword}`, {
      headers: this.authService.getAuthHeaders(),
    });
  }

  rechercherParCode(code: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/rechercher/code?code=${code}`, {
      headers: this.authService.getAuthHeaders(),
    });
  }
}
