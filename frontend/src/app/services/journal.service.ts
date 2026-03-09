import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

export interface JournalAction {
  id: number;
  utilisateur: string;
  action: string;
  dateAction: string;
  date_action?: string;
}

@Injectable({ providedIn: 'root' })
export class JournalService {
  private apiUrl = 'http://localhost:8080/journal';

  constructor(
    private http: HttpClient,
    private authService: AuthService,
  ) {}

  getHistorique(utilisateur?: string): Observable<JournalAction[]> {
    let url = `${this.apiUrl}/historique`;
    if (utilisateur) {
      url += `?utilisateur=${utilisateur}`;
    }
    return this.http.get<JournalAction[]>(url, {
      headers: this.authService.getAuthHeaders(),
    });
  }

  getHistoriqueParType(type?: string): Observable<JournalAction[]> {
    let url = `${this.apiUrl}/type`;
    if (type) {
      url += `?type=${type}`;
    }
    return this.http.get<JournalAction[]>(url, {
      headers: this.authService.getAuthHeaders(),
    });
  }
}
