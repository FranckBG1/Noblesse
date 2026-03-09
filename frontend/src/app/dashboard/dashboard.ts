import { Component, OnInit, AfterViewInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService, UserResponse } from '../services/auth.service';
import { Router } from '@angular/router';
import { StatsComponent } from './components/stats/stats';
import { VendreComponent } from './components/vendre/vendre';
import { StockComponent } from './components/stock/stock';
import { UsersComponent } from './components/users/users';
import { HistoryComponent } from './components/history/history';
import { SettingsComponent } from './components/settings/settings';
import { FactureComponent } from './components/facture/facture';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css'],
  standalone: true,
  imports: [
    CommonModule,
    StatsComponent,
    VendreComponent,
    StockComponent,
    UsersComponent,
    HistoryComponent,
    SettingsComponent,
    FactureComponent
  ],
})
export class DashboardComponent implements OnInit, AfterViewInit {
  menuItems = [
    { label: 'Tableau de Bord', icon: '📊', route: 'stats' },
    { label: 'Factures', icon: '🧾', route: 'facture' },
    { label: 'Vendre', icon: '🛒', route: 'vendre' },
    { label: 'Stock', icon: '📦', route: 'stock' },
    { label: 'Historique', icon: '📜', route: 'history', adminOnly: true },
    { label: 'Utilisateurs', icon: '👥', route: 'users', adminOnly: true },
    { label: 'Paramètres', icon: '⚙️', route: 'settings' }
  ];

  currentRoute: string = 'stats';
  currentUser: UserResponse | null = null;
  today = new Date();
  
  private canvas: HTMLCanvasElement | null = null;
  private ctx: CanvasRenderingContext2D | null = null;
  private particles: any[] = [];
  private animationId: number | null = null;

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit() {
    this.currentUser = this.authService.getCurrentUser();
    if (!this.currentUser) {
      this.router.navigate(['/login']);
      return;
    }
    
    // Si l'utilisateur n'est pas admin, on retire les menus restreints
    if (!this.currentUser.isAdmin) {
      this.menuItems = this.menuItems.filter(m => !m.adminOnly);
    }

    this.applySavedSettings();
    
    // Ecouter les changements de paramètres
    window.addEventListener('settingsChanged', () => {
      this.applySavedSettings();
    });
  }

  ngAfterViewInit() {
    this.initParticles();
  }

  @HostListener('window:resize')
  onResize() {
    if (this.canvas) {
      this.canvas.width = window.innerWidth;
      this.canvas.height = window.innerHeight;
      this.createParticles();
    }
  }

  private initParticles() {
    this.canvas = document.getElementById('particleCanvas') as HTMLCanvasElement;
    if (!this.canvas) return;
    
    this.ctx = this.canvas.getContext('2d');
    this.canvas.width = window.innerWidth;
    this.canvas.height = window.innerHeight;
    
    this.createParticles();
    this.animate();
  }

  private createParticles() {
    this.particles = [];
    const count = Math.floor((window.innerWidth * window.innerHeight) / 15000);
    
    for (let i = 0; i < count; i++) {
      this.particles.push({
        x: Math.random() * window.innerWidth,
        y: Math.random() * window.innerHeight,
        vx: (Math.random() - 0.5) * 0.5,
        vy: (Math.random() - 0.5) * 0.5,
        radius: Math.random() * 2 + 1
      });
    }
  }

  private animate() {
    if (!this.ctx || !this.canvas) return;
    
    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
    
    const isDark = document.body.classList.contains('dark-theme');
    const color = isDark ? 'rgba(255, 255, 255, 0.3)' : 'rgba(79, 70, 229, 0.4)';
    const lineColor = isDark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(79, 70, 229, 0.15)';
    
    this.ctx.fillStyle = color;
    this.ctx.strokeStyle = lineColor;
    this.ctx.lineWidth = 1;
    
    for (let i = 0; i < this.particles.length; i++) {
      const p = this.particles[i];
      
      p.x += p.vx;
      p.y += p.vy;
      
      if (p.x < 0) p.x = this.canvas.width;
      if (p.x > this.canvas.width) p.x = 0;
      if (p.y < 0) p.y = this.canvas.height;
      if (p.y > this.canvas.height) p.y = 0;
      
      this.ctx.beginPath();
      this.ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
      this.ctx.fill();
      
      // Lines
      for (let j = i + 1; j < this.particles.length; j++) {
        const p2 = this.particles[j];
        const dx = p.x - p2.x;
        const dy = p.y - p2.y;
        const dist = Math.sqrt(dx * dx + dy * dy);
        
        if (dist < 150) {
          this.ctx.beginPath();
          this.ctx.moveTo(p.x, p.y);
          this.ctx.lineTo(p2.x, p2.y);
          this.ctx.stroke();
        }
      }
    }
    
    this.animationId = requestAnimationFrame(() => this.animate());
  }

  applySavedSettings() {
    const saved = localStorage.getItem('app_settings');
    if (saved) {
      const config = JSON.parse(saved);
      
      // Apply theme
      if (config.darkMode) {
        document.body.classList.add('dark-theme');
      } else {
        document.body.classList.remove('dark-theme');
      }

      // Animation
      setTimeout(() => {
        const root = document.querySelector('.dashboard-root');
        if (root) {
          if (config.showAnimation === false) {
            root.classList.add('no-animation');
            if (this.animationId) {
              cancelAnimationFrame(this.animationId);
              this.animationId = null;
            }
          } else {
            root.classList.remove('no-animation');
            if (!this.animationId) {
              this.animate();
            }
          }
        }
      }, 0);
    }
  }

  navigateTo(route: string) {
    this.currentRoute = route;
  }

  logout() {
    if (this.animationId) {
      cancelAnimationFrame(this.animationId);
    }
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  get pageTitle(): string {
    const item = this.menuItems.find(m => m.route === this.currentRoute);
    return item ? item.label : 'Dashboard';
  }

  get userName(): string {
    return this.currentUser?.nom || 'Utilisateur';
  }

  get userRole(): string {
    return this.currentUser?.isAdmin ? 'Administrateur' : 'Vendeur';
  }
}
