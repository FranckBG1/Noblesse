import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export interface UserResponse {
  idUsers: string;
  nom: string;
  isAdmin: boolean;
  derniereConnexion?: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/usersUU';
  private currentUser: UserResponse | null = null;
  private authHeader: string | null = null;

  constructor(private http: HttpClient) {}

  login(idUsers: string, motDePasse: string): Observable<UserResponse> {
    const body = { idUsers, motDePasse };

    // Store credentials for Basic Auth in subsequent requests
    const credentials = btoa(`${idUsers}:${motDePasse}`);
    this.authHeader = `Basic ${credentials}`;

    return this.http.post<UserResponse>(`${this.apiUrl}/connecter`, body).pipe(
      tap((user) => {
        this.currentUser = user;
        // Optionnel: stocker dans le localStorage si on veut persister la session
        localStorage.setItem('currentUser', JSON.stringify(user));
        localStorage.setItem('authHeader', this.authHeader!);
      }),
    );
  }

  getAuthHeaders(): HttpHeaders {
    if (!this.authHeader) {
      this.authHeader = localStorage.getItem('authHeader');
    }
    
    if (this.authHeader) {
      return new HttpHeaders({
        Authorization: this.authHeader,
      });
    }
    return new HttpHeaders();
  }

  logout() {
    this.currentUser = null;
    this.authHeader = null;
    localStorage.removeItem('currentUser');
    localStorage.removeItem('authHeader');
  }

  isLoggedIn(): boolean {
    return !!this.authHeader || !!localStorage.getItem('authHeader');
  }

  getCurrentUser(): UserResponse | null {
    if (!this.currentUser) {
      const stored = localStorage.getItem('currentUser');
      if (stored) {
        this.currentUser = JSON.parse(stored);
      }
    }
    return this.currentUser;
  }
}
