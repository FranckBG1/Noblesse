import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-login',
  templateUrl: './login.html',
  styleUrls: ['./login.css'],
  standalone: true,
  imports: [CommonModule, FormsModule],
})
export class LoginComponent {
  identifiant: string = '';
  password: string = '';
  errorMessage: string = '';
  isLoading: boolean = false;
  isLocked: boolean = false;
  remainingSeconds: number = 0;
  private timerInterval: any;

  constructor(
    private authService: AuthService, 
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  onLogin() {
    if (this.isLocked) return;
    this.errorMessage = '';

    if (!this.identifiant || !this.password) {
      this.errorMessage = 'Veuillez remplir tous les champs';
      return;
    }

    this.isLoading = true;

    this.authService.login(this.identifiant, this.password)
      .pipe(finalize(() => {
        this.isLoading = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: (user) => {
          this.router.navigate(['/dashboard']);
        },
        error: (err) => {
          console.error('Erreur de connexion:', err);
          
          if (err.status === 403) {
            this.startLockTimer(60); // Bloque 1 minute
          }
          
          if (err.error && err.error.message) {
            this.errorMessage = err.error.message;
          } else {
            this.errorMessage = 'Identifiant ou mot de passe incorrect';
          }
          this.cdr.detectChanges();
        }
      });
  }

  private startLockTimer(seconds: number) {
    this.isLocked = true;
    this.remainingSeconds = seconds;
    
    if (this.timerInterval) clearInterval(this.timerInterval);
    
    this.timerInterval = setInterval(() => {
      this.remainingSeconds--;
      if (this.remainingSeconds <= 0) {
        this.isLocked = false;
        clearInterval(this.timerInterval);
      }
      this.cdr.detectChanges();
    }, 1000);
  }

  onForgotPassword() {
    alert('Feature non implémentée : Récupération de mot de passe');
  }
}
