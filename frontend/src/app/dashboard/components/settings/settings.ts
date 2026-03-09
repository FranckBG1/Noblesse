import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="settings-container">
      <div class="settings-header">
        <h2>⚙️ Paramètres de l'Application</h2>
        <p>Options d'affichage locales</p>
      </div>

      <div class="settings-grid">
        <!-- THEME SECTION -->
        <div class="settings-card">
          <div class="card-icon">🎨</div>
          <div class="card-content">
            <h3>Apparence</h3>
            <p>Options visuelles de l'interface.</p>
            
            <div class="setting-item">
              <label class="switch">
                <input type="checkbox" [(ngModel)]="darkMode" (change)="toggleDarkMode()">
                <span class="slider round"></span>
              </label>
              <span>Mode Sombre</span>
            </div>
            
            <div class="setting-item">
              <label class="switch">
                <input type="checkbox" [(ngModel)]="showAnimation" (change)="toggleAnimation()">
                <span class="slider round"></span>
              </label>
              <span>Activer les transitions</span>
            </div>
          </div>
        </div>

        <!-- NOTIFICATIONS SECTION -->
        <div class="settings-card">
          <div class="card-icon">🔔</div>
          <div class="card-content">
            <h3>Alertes Stock</h3>
            <p>Paramètres visuels pour les alertes.</p>
            <div class="setting-item">
              <label class="switch">
                <input type="checkbox" [(ngModel)]="highlightStock" (change)="saveSettings()">
                <span class="slider round"></span>
              </label>
              <span>Mettre en évidence le stock critique (Rouge)</span>
            </div>
          </div>
        </div>

        <!-- ABOUT SECTION -->
        <div class="settings-card info">
          <div class="card-icon">🏢</div>
          <div class="card-content">
            <h3>À propos</h3>
            <p><strong>ETS Noblesse - Gestion d'Inventaire</strong></p>
            <p>Version 2.0.0 (High-Contrast)</p>
            <p>© 2026 Tous droits réservés.</p>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .settings-container { display: flex; flex-direction: column; gap: 1.5rem; }

    .settings-header h2 { margin: 0; font-size: 1.5rem; font-weight: 700; color: var(--text-main); letter-spacing: -0.025em; }
    .settings-header p { margin: 4px 0 0; color: var(--text-muted); font-weight: 400; font-size: 0.875rem; }

    .settings-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 1rem; }

    .settings-card {
      background: #ffffff;
      border-radius: 12px;
      padding: 1.5rem;
      display: flex;
      gap: 1.5rem;
      border: 1px solid var(--panel-border);
      box-shadow: var(--shadow-sm);
      transition: all 0.2s;
    }
    .settings-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); border-color: var(--primary); }
    .settings-card.info { background: #f8fafc; }

    .card-icon {
      width: 48px; height: 48px; border-radius: 10px; background: #f1f5f9;
      display: grid; place-items: center; font-size: 1.5rem; flex-shrink: 0;
      color: var(--primary);
    }

    .card-content h3 { margin: 0 0 8px; font-size: 1.125rem; font-weight: 700; color: var(--text-main); letter-spacing: -0.025em; }
    .card-content p { margin: 0 0 1.25rem; font-size: 0.875rem; color: var(--text-muted); font-weight: 400; line-height: 1.5; }

    .setting-item { display: flex; align-items: center; gap: 12px; margin-bottom: 0.75rem; font-size: 0.9375rem; font-weight: 600; color: var(--text-main); }

    /* SWITCH TOGGLE REFINED */
    .switch { position: relative; display: inline-block; width: 44px; height: 24px; }
    .switch input { opacity: 0; width: 0; height: 0; }
    .slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #e2e8f0; transition: .2s; border-radius: 9999px; }
    .slider:before { position: absolute; content: ""; height: 18px; width: 18px; left: 3px; bottom: 3px; background-color: #ffffff; transition: .2s; border-radius: 50%; box-shadow: 0 1px 2px rgba(0,0,0,0.1); }
    input:checked + .slider { background-color: var(--primary); }
    input:checked + .slider:before { transform: translateX(20px); }
  `]
})
export class SettingsComponent implements OnInit {
  darkMode = false;
  showAnimation = true;
  highlightStock = true;

  ngOnInit() {
    const saved = localStorage.getItem('app_settings');
    if (saved) {
      const config = JSON.parse(saved);
      this.darkMode = config.darkMode || false;
      this.showAnimation = config.showAnimation !== undefined ? config.showAnimation : true;
      this.highlightStock = config.highlightStock !== undefined ? config.highlightStock : true;
      this.applyTheme();
    } else {
      // Default values
      this.darkMode = false;
      this.showAnimation = true;
      this.highlightStock = true;
    }
  }

  toggleDarkMode() {
    this.saveSettings();
    this.applyTheme();
  }

  toggleAnimation() {
    this.saveSettings();
    this.applyTheme();
  }

  applyTheme() {
    const body = document.body;
    if (this.darkMode) {
      body.classList.add('dark-theme');
    } else {
      body.classList.remove('dark-theme');
    }

    const root = document.querySelector('.dashboard-root');
    if (root) {
      if (this.showAnimation) {
        root.classList.remove('no-animation');
      } else {
        root.classList.add('no-animation');
      }
    }
  }

  saveSettings() {
    const config = {
      darkMode: this.darkMode,
      showAnimation: this.showAnimation,
      highlightStock: this.highlightStock
    };
    localStorage.setItem('app_settings', JSON.stringify(config));
    
    // Dispatch custom event to notify other components (like Stock)
    window.dispatchEvent(new Event('settingsChanged'));
  }
}
