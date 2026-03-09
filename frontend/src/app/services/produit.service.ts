import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

export interface Produit {
  idProduit: number;
  designation: string;
  code: string;
  photo: string;
  quantite: number;
  dernierPrix: number;
  prixUnitaire: number;
}

export interface ProduitUpdateData extends Partial<Produit> {
  motDePasseAdmin?: string;
}

export interface ProduitCreateData {
  designation: string;
  code: string;
  photo?: string;
  quantite: number;
  dernierPrix: number;
  prixUnitaire: number;
  motDePasseAdmin: string;
}

export interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class ProduitService {
  private apiUrl = 'http://localhost:8080/produit';

  constructor(private http: HttpClient, private authService: AuthService) {}

  getProduitsPaginated(page: number, size: number): Observable<Page<Produit>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
      
    return this.http.get<Page<Produit>>(`${this.apiUrl}/page`, {
      params,
      headers: this.authService.getAuthHeaders()
    });
  }

  getProduitsCritiquesPaginated(page: number, size: number): Observable<Page<Produit>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
      
    return this.http.get<Page<Produit>>(`${this.apiUrl}/critique`, {
      params,
      headers: this.authService.getAuthHeaders()
    });
  }

  rechercherProduits(keyword: string): Observable<Produit[]> {
    return this.http.get<Produit[]>(`${this.apiUrl}/rechercher/${keyword}`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  rechercherParCode(code: string): Observable<Produit[]> {
    return this.http.get<Produit[]>(`${this.apiUrl}/rechercher/code/${code}`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  createProduit(produit: ProduitCreateData): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/ajouter`, produit, {
      headers: this.authService.getAuthHeaders()
    });
  }

  deleteProduit(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/supprimer/${id}`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  updateProduit(id: number, produit: ProduitUpdateData): Observable<Produit> {
    return this.http.put<Produit>(`${this.apiUrl}/modifier/${id}`, produit, {
      headers: this.authService.getAuthHeaders()
    });
  }

  retirerStock(idProduit: number, quantite: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/retirerStock/${idProduit}/${quantite}`, {}, {
      headers: this.authService.getAuthHeaders()
    });
  }
}
