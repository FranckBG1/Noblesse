import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { JournalService, JournalAction } from '../../../services/journal.service';

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="history-container">
      <div class="header-actions">
        <div class="title-section">
          <h2 (click)="clearFilter()">📜 Historique des Actions</h2>
          <p class="subtitle">Suivi en temps réel de toutes les opérations</p>
        </div>
        
        <div class="filter-group">
          <select [(ngModel)]="typeFilter" (change)="loadHistory()" class="filter-select">
            <option value="">Tous les types</option>
            <option value="VENTE">Ventes</option>
            <option value="FACTURE">Factures</option>
            <option value="CONNEXION">Connexions</option>
            
          </select>
          <div class="search-input-wrapper">
            <span class="search-ico">👤</span>
            <input 
              type="text" 
              [(ngModel)]="userFilter" 
              (keyup.enter)="loadHistory()"
              placeholder="Chercher par utilisateur..." 
              class="filter-input">
            <button *ngIf="userFilter" class="btn-clear" (click)="clearFilter()">✕</button>
          </div>
          <button class="btn-filter" (click)="loadHistory()">Filtrer</button>
        </div>
      </div>
      
      <div *ngIf="errorMessage" class="msg-error">
        {{ errorMessage }}
      </div>

      <div class="table-glass">
        <table class="history-table">
          <thead>
            <tr>
              <th>Date & Heure</th>
              <th>Intervenant</th>
              <th>Description de l'Action</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let entry of historique" [class.action-danger]="isDangerAction(entry.action)">
              <td class="time-cell">{{ entry.dateAction | date:'dd/MM/yyyy HH:mm' }}</td>
              <td class="user-cell">
                <div class="user-badge">
                  {{ entry.utilisateur }}
                </div>
              </td>
              <td class="action-cell">{{ entry.action }}</td>
            </tr>
            <tr *ngIf="historique.length === 0 && !errorMessage">
              <td colspan="3" class="empty-state">
                <div class="empty-icon">📂</div>
                <p>Aucun historique trouvé pour cette recherche.</p>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .history-container { display: flex; flex-direction: column; gap: 1.5rem; }
    
    .header-actions {
      background: var(--panel-bg);
      padding: 1.5rem;
      
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 1.5rem;
      flex-wrap: wrap;
      box-shadow: var(--shadow-sm);
    }

    .title-section h2 { margin: 0; font-size: 1.5rem; font-weight: 950; color: var(--text-main); cursor: pointer; text-transform: uppercase; border-bottom: 3px solid var(--text-main); padding-bottom: 4px; }
    .title-section .subtitle { margin: 8px 0 0; font-size: 0.9rem; color: var(--text-muted); font-weight: 700; text-transform: uppercase; }

    .filter-group { display: flex; gap: 12px; align-items: center; }

    .filter-select {
      padding: 0.75rem;
      border-radius: 4px;
      background: var(--panel-bg-alt);
      color: var(--text-main);
      font-weight: 800;
      border: 1px solid var(--panel-border);
    }

    .search-input-wrapper { position: relative; display: flex; align-items: center; }
    .search-ico { position: absolute; left: 12px; color: var(--text-main); font-size: 1.1rem; z-index: 1; }
    .filter-input {
      padding: 0.75rem 1rem 0.75rem 2.5rem;
      background: var(--panel-bg-alt);
     
      border-radius: 4px;
      width: 280px;
      font-size: 1rem;
      font-weight: 800;
      color: var(--text-main);
      transition: all 0.2s;
    }
    .filter-input:focus { outline: none; border-color: var(--primary); box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1); }
    
    .btn-clear { position: absolute; right: 10px; background: transparent; border: none; width: 24px; height: 24px; border-radius: 4px; display: grid; place-items: center; cursor: pointer; font-size: 0.8rem; color: var(--text-muted); z-index: 2; }

    .btn-filter {
      padding: 0.625rem 1.25rem;
      background: var(--primary);
      color: white;
      border: none;
      border-radius: 8px;
      font-weight: 600;
      font-size: 0.875rem;
      cursor: pointer;
      transition: all 0.2s;
    }
    .btn-filter:hover { background: var(--primary-hover); box-shadow: 0 4px 6px -1px rgba(79, 70, 229, 0.3); }

    .table-glass {
      background: var(--panel-bg);
      border-radius: 12px;
      border: 1px solid var(--panel-border);
      overflow: hidden;
      box-shadow: var(--shadow-sm);
    }

    .history-table { width: 100%; border-collapse: collapse; }
    .history-table th {
      padding: 1rem 1.5rem;
      background: var(--panel-bg-alt);
      text-align: left;
      font-size: 0.75rem;
      font-weight: 600;
      color: var(--text-muted);
      text-transform: uppercase;
      letter-spacing: 0.05em;
      border-bottom: 1px solid var(--panel-border);
    }

    .history-table td { padding: 1rem 1.5rem; font-size: 0.9375rem; border-bottom: 1px solid var(--panel-border); color: var(--text-main); font-weight: 500; }
    .history-table tr:last-child td { border-bottom: none; }
    .history-table tr:hover td { background: var(--panel-bg-alt); }

    .time-cell { color: var(--text-muted); font-weight: 500; font-variant-numeric: tabular-nums; width: 180px; font-size: 0.8125rem; }
    .user-badge {
      background: rgba(79, 70, 229, 0.1); color: var(--primary);
      padding: 4px 10px; border-radius: 9999px; font-weight: 600; font-size: 0.75rem; display: inline-block;
    }

    .action-cell { font-weight: 500; color: var(--text-main); line-height: 1.5; }

    .action-danger td { background-color: rgba(239, 68, 68, 0.1) !important; }
    .action-danger .user-badge { background: #fee2e2; color: #ef4444; }
    .action-danger .action-cell { color: #ef4444; font-weight: 600; }

    .empty-state { text-align: center; padding: 4rem; color: var(--text-muted); font-weight: 500; }
    .empty-icon { font-size: 3rem; margin-bottom: 1rem; opacity: 0.5; }

    .msg-error {
      background: #fef2f2;
      color: #991b1b;
      padding: 1rem;
      border-radius: 8px;
      border: 1px solid #fee2e2;
      font-weight: 600;
      margin-bottom: 1rem;
      font-size: 0.875rem;
    }
  `]
})
export class HistoryComponent implements OnInit {
  historique: JournalAction[] = [];
  errorMessage = '';
  userFilter = '';
  typeFilter = '';

  constructor(
    private journalService: JournalService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadHistory();
  }

  loadHistory() {
    if (this.typeFilter) {
      this.journalService.getHistoriqueParType(this.typeFilter).subscribe({
        next: (data) => {
          this.historique = data;
          if (this.userFilter) {
            this.historique = this.historique.filter(h => h.utilisateur.toLowerCase().includes(this.userFilter.toLowerCase()));
          }
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.errorMessage = 'Impossible de charger l\'historique.';
          this.cdr.detectChanges();
        }
      });
    } else {
      this.journalService.getHistorique(this.userFilter).subscribe({
        next: (data) => {
          this.historique = data;
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.errorMessage = 'Impossible de charger l\'historique.';
          this.cdr.detectChanges();
        }
      });
    }
  }

  clearFilter() {
    this.userFilter = '';
    this.typeFilter = '';
    this.loadHistory();
  }

  isDangerAction(action: string): boolean {
    if (!action) return false;
    const lowerAction = action.toLowerCase();
    return lowerAction.includes('supprimé') || lowerAction.includes('modifié');
  }
}
