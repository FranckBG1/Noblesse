import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardStatsService, DashboardStats } from '../../../services/stats.service';
import { AuthService } from '../../../services/auth.service';
import { FactureService, Facture } from '../../../services/facture.service';

@Component({
  selector: 'app-stats',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="stats-wrapper" *ngIf="stats">
      <!-- KPI CARDS -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon income">💰</div>
          <div class="stat-info">
            <span class="stat-label">Chiffre d'Affaires (Jour)</span>
            <h2 class="stat-value">{{ stats.chiffreAffaireJour | number:'1.0-0' }} FCFA</h2>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon sales">🛍️</div>
          <div class="stat-info">
            <span class="stat-label">Ventes (06h - 23h)</span>
            <h2 class="stat-value">{{ stats.nombreVentesJour }}</h2>
            <p class="stat-sub">ventes enregistrées aujourd'hui</p>
          </div>
        </div>

        <div class="stat-card" [class.danger]="stats.stockCritique > 0">
          <div class="stat-icon stock">⚠️</div>
          <div class="stat-info">
            <span class="stat-label">Stock Critique</span>
            <h2 class="stat-value">{{ stats.stockCritique }}</h2>
            <p class="stat-sub">produits à réapprovisionner</p>
          </div>
        </div>
      </div>

      <div class="details-grid">
        <!-- TOP 5 PRODUITS POPULAIRES -->
        <div class="data-panel">
          <div class="panel-header">
            <h3>⭐ Top 5 Populaires (7 jours)</h3>
          </div>
          <div class="panel-content">
            <ul class="popular-list">
              <li *ngFor="let p of stats.produitsPopulaires; let i = index">
                <span class="rank">{{ i + 1 }}</span>
                <div class="p-info">
                  <span class="p-name">{{ p.designation }}</span>
                  <span class="p-count">{{ p.quantiteVendue }} vendus</span>
                </div>
                <div class="p-bar-bg">
                  <div class="p-bar-fill" 
                    [style.width.%]="stats.produitsPopulaires.length > 0 && stats.produitsPopulaires[0].quantiteVendue > 0 ? (p.quantiteVendue / stats.produitsPopulaires[0].quantiteVendue) * 100 : 0">
                  </div>
                </div>
              </li>
              <li *ngIf="stats.produitsPopulaires.length === 0" class="empty">Aucune vente aujourd'hui</li>
            </ul>
          </div>
        </div>

        <!-- FLUX D'ACTIVITÉ (ADMIN SEULEMENT) -->
        <div class="data-panel" *ngIf="isAdmin">
          <div class="panel-header">
            <h3>⚡ Flux d'Activité (Admin)</h3>
            <span class="badge alt">Direct</span>
          </div>
          <div class="panel-content scrollable">
            <ul class="activity-list">
              <li *ngFor="let act of stats.activitesRecentes">
                <div class="act-dot"></div>
                <div class="act-info">
                  <p><strong>{{ act.utilisateur }}</strong> {{ act.action }}</p>
                  <small>{{ act.dateAction | date:'dd/MM HH:mm' }}</small>
                </div>
              </li>
              <li *ngIf="stats.activitesRecentes.length === 0" class="empty">Pas d'activité récente</li>
            </ul>
          </div>
        </div>

        <!-- FACTURES TERMINÉES DU JOUR -->
        <div class="data-panel">
          <div class="panel-header">
            <h3>📑 Factures Terminées (Jour)</h3>
            <span class="badge">Aujourd'hui</span>
          </div>
          <div class="panel-content scrollable">
            <table class="simple-table">
              <thead>
                <tr>
                  <th>Heure</th>
                  <th>Client</th>
                  <th>Émis par</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let f of facturesDuJour">
                  <td class="time">{{ f.dateCreation | date:'HH:mm' }}</td>
                  <td><strong>{{ f.nomClient }}</strong></td>
                  <td><span class="issuer-name">{{ f.utilisateur?.nom || 'N/A' }}</span></td>
                  <td>
                    <button class="btn-view" (click)="selectedFacture = f">Voir</button>
                  </td>
                </tr>
                <tr *ngIf="facturesDuJour.length === 0">
                  <td colspan="4" class="empty">Aucune facture terminée aujourd'hui</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- TOP 10 CLIENTS -->
        <div class="data-panel">
          <div class="panel-header">
            <h3>🏆 Meilleurs Clients</h3>
            <div class="tabs">
              <button [class.active]="topTab === 'ca'" (click)="topTab = 'ca'">CA</button>
              <button [class.active]="topTab === 'rep'" (click)="topTab = 'rep'">Répétition</button>
            </div>
          </div>
          <div class="panel-content">
            <table class="simple-table" *ngIf="topTab === 'ca'">
              <thead>
                <tr>
                  <th>Client</th>
                  <th *ngIf="isAdmin">Total CA</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let c of topCA">
                  <td><strong>{{ c.nomClient }}</strong></td>
                  <td *ngIf="isAdmin" class="text-success">{{ c.ca | number:'1.0-0' }} FCFA</td>
                </tr>
                <tr *ngIf="topCA.length === 0">
                  <td colspan="2" class="empty">Pas de données</td>
                </tr>
              </tbody>
            </table>

            <table class="simple-table" *ngIf="topTab === 'rep'">
              <thead>
                <tr>
                  <th>Client</th>
                  <th>Factures</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let c of topRep">
                  <td><strong>{{ c.nomClient }}</strong></td>
                  <td><span class="badge alt">{{ c.repetition }}</span></td>
                </tr>
                <tr *ngIf="topRep.length === 0">
                  <td colspan="2" class="empty">Pas de données</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- VENTES DE LA JOURNÉE (SCROLLABLE) -->
        <div class="data-panel">
          <div class="panel-header">
            <h3>🛒 Ventes de la Journée</h3>
            <span class="badge">Aujourd'hui</span>
          </div>
          <div class="panel-content scrollable">
            <table class="simple-table">
              <thead>
                <tr>
                  <th>Heure</th>
                  <th>Produit</th>
                  <th>Total</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let v of stats.ventesJour">
                  <td class="time">{{ v.dateVente | date:'HH:mm' }}</td>
                  <td><strong>{{ v.nomProduit }}</strong><br><small>{{ v.quantite }} x {{ v.prixVente | number:'1.0-0' }}</small></td>
                  <td class="text-success">{{ v.montantTotal | number:'1.0-0' }}</td>
                </tr>
                <tr *ngIf="stats.ventesJour.length === 0">
                  <td colspan="3" class="empty">Pas encore de vente aujourd'hui</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- MODAL DÉTAIL FACTURE -->
    <div class="modal-overlay" *ngIf="selectedFacture" (click)="selectedFacture = null">
      <div class="modal-content" (click)="$event.stopPropagation()">
        <div class="modal-header">
          <h3>Facture - {{ selectedFacture.nomClient }}</h3>
          <button (click)="selectedFacture = null">×</button>
        </div>
        <div class="modal-body">
          <p>Date: {{ selectedFacture.dateCreation | date:'dd/MM/yyyy HH:mm' }}</p>
          <p>Émis par: <strong>{{ selectedFacture.utilisateur?.nom || 'N/A' }}</strong></p>
          <table class="simple-table">
            <thead>
              <tr>
                <th>Produit</th>
                <th>Qte</th>
                <th>Total</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let v of selectedFacture.ventes">
                <td>{{ v.produit?.designation || v.nomProduitHorsStock }}</td>
                <td>{{ v.quantite }}</td>
                <td>{{ v.montantTotal | number:'1.0-0' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div *ngIf="!stats" class="loading-placeholder">
       <p>Chargement des statistiques...</p>
    </div>
  `,
  styles: [`
    .stats-wrapper { display: flex; flex-direction: column; gap: 1.5rem; }
    .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 1rem; }
    
    .stat-card {
      background: var(--panel-bg);
      padding: 1.5rem;
      border-radius: 12px;
      display: flex;
      align-items: center;
      gap: 1.25rem;
      border: 1px solid var(--panel-border);
      transition: all 0.2s;
      box-shadow: var(--shadow-sm);
    }
    
    .stat-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); }
    .stat-card.danger { border-color: #fee2e2; background: rgba(239, 68, 68, 0.1); }
    
    .stat-icon {
      width: 48px;
      height: 48px;
      border-radius: 10px;
      display: grid;
      place-items: center;
      font-size: 1.5rem;
      background: var(--panel-bg-alt);
      color: var(--primary);
    }
    
    .income { background: rgba(16, 185, 129, 0.1); color: #10b981; }
    .sales { background: rgba(59, 130, 246, 0.1); color: #3b82f6; }
    .stock { background: rgba(239, 68, 68, 0.1); color: #ef4444; }
    
    .stat-info .stat-label { font-size: 0.8125rem; color: var(--text-muted); font-weight: 600; }
    .stat-info .stat-value { font-size: 1.5rem; font-weight: 700; color: var(--text-main); margin: 0.1rem 0; letter-spacing: -0.025em; }
    .stat-info .stat-sub { font-size: 0.75rem; color: var(--text-muted); font-weight: 400; }
    
    .details-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(400px, 1fr)); gap: 1.5rem; }
    
    .data-panel {
      background: var(--panel-bg);
      border-radius: 12px;
      padding: 1.5rem;
      border: 1px solid var(--panel-border);
      display: flex;
      flex-direction: column;
      height: 100%;
      box-shadow: var(--shadow-sm);
    }
    
    .panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    .panel-header h3 { font-size: 1.125rem; font-weight: 700; color: var(--text-main); margin: 0; letter-spacing: -0.025em; }
    
    .scrollable { max-height: 400px; overflow-y: auto; padding-right: 4px; }
    .scrollable::-webkit-scrollbar { width: 6px; }
    .scrollable::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 10px; }
    
    .simple-table { width: 100%; border-collapse: collapse; }
    .simple-table th { text-align: left; font-size: 0.75rem; color: var(--text-muted); font-weight: 600; text-transform: uppercase; padding: 10px; border-bottom: 1px solid var(--panel-border); }
    .simple-table td { padding: 12px 10px; border-bottom: 1px solid var(--panel-border); color: var(--text-main); font-size: 0.875rem; }
    .simple-table tr:last-child td { border-bottom: none; }
    .simple-table tr:hover td { background: var(--panel-bg-alt); }
    
    .time { color: var(--text-muted); font-weight: 500; font-variant-numeric: tabular-nums; font-size: 0.8125rem; }
    .issuer-name { font-size: 0.75rem; color: var(--primary); font-weight: 600; background: var(--panel-bg-alt); padding: 2px 6px; border-radius: 4px; }
    .text-success { color: #10b981; font-weight: 600; }
    
    .badge { padding: 4px 10px; border-radius: 9999px; font-size: 0.6875rem; font-weight: 600; text-transform: uppercase; background: var(--panel-bg-alt); color: var(--text-muted); }
    .badge.alt { background: rgba(79, 70, 229, 0.1); color: var(--primary); }
    
    .empty { text-align: center; color: var(--text-muted); padding: 3rem; font-weight: 500; font-size: 0.875rem; }
    .loading-placeholder { padding: 100px; text-align: center; color: var(--text-muted); }

    .tabs { display: flex; gap: 0.5rem; }
    .tabs button { 
      background: var(--panel-bg-alt); border: 1px solid var(--panel-border); padding: 4px 12px; border-radius: 8px;
      font-size: 0.75rem; cursor: pointer; color: var(--text-muted); font-weight: 600;
    }
    .tabs button.active { background: var(--primary); color: white; border-color: var(--primary); }
    
    /* POPULAR LIST STYLES */
    .popular-list { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 1rem; }
    .popular-list li { display: flex; align-items: center; gap: 1rem; position: relative; }
    .rank { 
      width: 28px; height: 28px; background: var(--panel-bg-alt); border-radius: 50%; 
      display: grid; place-items: center; font-size: 0.75rem; font-weight: 700; color: var(--primary);
    }
    .p-info { flex: 1; display: flex; justify-content: space-between; align-items: center; z-index: 1; }
    .p-name { font-weight: 600; font-size: 0.875rem; color: var(--text-main); }
    .p-count { font-size: 0.75rem; color: var(--text-muted); font-weight: 500; }
    .p-bar-bg { 
      position: absolute; left: 40px; right: 0; bottom: -4px; height: 4px; 
      background: var(--panel-bg-alt); border-radius: 2px; overflow: hidden; 
    }
    .p-bar-fill { height: 100%; background: var(--primary); opacity: 0.3; border-radius: 2px; transition: width 0.5s ease-out; }

    /* ACTIVITY LIST STYLES */
    .activity-list { list-style: none; padding: 0.5rem 0; margin: 0; display: flex; flex-direction: column; gap: 1.25rem; }
    .activity-list li { display: flex; gap: 1rem; position: relative; }
    .activity-list li:not(:last-child)::after {
      content: ''; position: absolute; left: 5px; top: 20px; bottom: -15px; width: 2px; 
      background: var(--panel-border);
    }
    .act-dot { 
      width: 12px; height: 12px; background: var(--primary); border-radius: 50%; 
      margin-top: 4px; z-index: 1; border: 2px solid var(--panel-bg);
    }
    .act-info p { margin: 0; font-size: 0.875rem; color: var(--text-main); line-height: 1.4; }
    .act-info small { color: var(--text-muted); font-size: 0.75rem; }

    .btn-view {
      background: var(--primary); color: white; border: none; padding: 4px 12px; border-radius: 6px;
      font-size: 0.75rem; cursor: pointer;
    }

    .modal-overlay {
      position: fixed; top: 0; left: 0; width: 100%; height: 100%;
      background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000;
    }
    .modal-content {
      background: var(--panel-bg); padding: 2rem; border-radius: 12px; min-width: 400px; max-width: 90%;
      box-shadow: var(--shadow-xl); border: 1px solid var(--panel-border);
    }
    .modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    .modal-header button { background: none; border: none; font-size: 1.5rem; cursor: pointer; color: var(--text-muted); }
  `]
})
export class StatsComponent implements OnInit {
  stats: DashboardStats | null = null;
  isAdmin = false;
  topTab: 'ca' | 'rep' = 'ca';
  topCA: any[] = [];
  topRep: any[] = [];
  facturesDuJour: Facture[] = [];
  selectedFacture: Facture | null = null;

  constructor(
    private statsService: DashboardStatsService,
    private authService: AuthService,
    private factureService: FactureService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.isAdmin = this.authService.getCurrentUser()?.isAdmin || false;
    this.loadStats();
    this.loadAdditionalStats();
  }

  loadStats() {
    this.statsService.getStats().subscribe({
      next: (data) => {
        this.stats = data;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error fetching stats', err);
        this.cdr.detectChanges();
      }
    });
  }

  loadAdditionalStats() {
    this.factureService.getTopClientsCA().subscribe(data => this.topCA = data);
    this.factureService.getTopClientsRepetition().subscribe(data => this.topRep = data);
    this.factureService.getFacturesTermineesDuJour().subscribe(data => this.facturesDuJour = data);
  }
}

