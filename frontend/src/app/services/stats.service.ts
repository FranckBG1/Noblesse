import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { VenteResponse } from './ventes.service';
import { JournalAction } from './journal.service';

export interface DashboardStats {
  chiffreAffaireJour: number;
  nombreVentesJour: number;
  stockCritique: number;
  ventesJour: VenteResponse[];
  produitsPopulaires: { designation: string; quantiteVendue: number }[];
  activitesRecentes: JournalAction[];
}

@Injectable({ providedIn: 'root' })
export class DashboardStatsService {
  private apiUrl = 'http://localhost:8080/stats';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  getStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.apiUrl}/dashboard`, {
      headers: this.authService.getAuthHeaders(),
    });
  }
}
