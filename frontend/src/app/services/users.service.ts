import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService, UserResponse } from './auth.service';

export interface UserUpdateData {
  nom?: string;
  motDePasse?: string;
  isAdmin?: boolean;
  motDePasseAdmin?: string;
}

export interface UserCreateData {
  nom: string;
  motDePasse: string;
  isAdmin: boolean;
  motDePasseAdmin?: string;
}

@Injectable({
  providedIn: 'root'
})
export class UsersService {
  private apiUrl = 'http://localhost:8080/usersUU';

  constructor(private http: HttpClient, private authService: AuthService) {}

  getUsers(): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(`${this.apiUrl}/lister`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  deleteUser(idUsers: string, motDePasseAdmin?: string): Observable<void> {
    const adminId = this.authService.getCurrentUser()?.idUsers;
    let url = `${this.apiUrl}/supprimer/${adminId}/${idUsers}`;
    if (motDePasseAdmin) {
      url += `?motDePasseAdmin=${encodeURIComponent(motDePasseAdmin)}`;
    }
    return this.http.delete<void>(url, {
      headers: this.authService.getAuthHeaders()
    });
  }

  updateUser(idUsers: string, data: UserUpdateData): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/modifier/${idUsers}`, data, {
      headers: this.authService.getAuthHeaders()
    });
  }

  createUser(data: UserCreateData): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/creer`, data, {
      headers: this.authService.getAuthHeaders()
    });
  }
}
