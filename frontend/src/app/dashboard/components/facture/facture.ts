import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FactureService, Facture, Vente, Client } from '../../../services/facture.service';
import { AuthService } from '../../../services/auth.service';
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';

@Component({
  selector: 'app-facture',
  templateUrl: './facture.html',
  styleUrls: ['./facture.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class FactureComponent implements OnInit {
  facturesIncompletes = signal<Facture[]>([]);
  facturesTerminees = signal<Facture[]>([]);
  ventesDuJour = signal<Vente[]>([]);
  ventesDernierMois = signal<Vente[]>([]);
  
  showDialog = false;
  showDeleteConfirm = false;
  factureToDelete: Facture | null = null;
  selectedVentesIds: number[] = [];
  nomClient = '';
  telephone = '';
  avance = 0;
  explications = '';
  editingFacture: Facture | null = null;
  errorMessage = '';
  searchClient = '';
  selectedFacture: Facture | null = null;
  factureToPrint: Facture | null = null;
  typeFacture: 'CLASSIQUE' | 'DEPOT' = 'CLASSIQUE';
  clients = signal<Client[]>([]);
  clientSuggestions: Client[] = [];
  selectedClientId: number | null = null;
  
  viewSalesHistory = false;
  isAdmin = false;
  
  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;

  constructor(private factureService: FactureService, private authService: AuthService) {}

  ngOnInit() {
    this.isAdmin = this.authService.getCurrentUser()?.isAdmin || false;
    this.loadFactures();
    this.loadFacturesTerminees();
  }

  loadFactures() {
    this.factureService.getFacturesIncompletes().subscribe(data => {
      this.facturesIncompletes.set(data);
    });
  }

  loadFacturesTerminees() {
    this.factureService.getFacturesTerminees(this.page, this.size, this.searchClient).subscribe(data => {
      this.facturesTerminees.set(data.content);
      this.totalPages = data.totalPages;
      this.totalElements = data.totalElements;
    });
  }

  onSearch() {
    this.page = 0;
    this.loadFacturesTerminees();
  }

  goToPage(pageNum: string) {
    const p = parseInt(pageNum, 10);
    if (!isNaN(p) && p > 0 && p <= this.totalPages) {
      this.page = p - 1;
      this.loadFacturesTerminees();
    }
  }

  nextPage() {
    if (this.page < this.totalPages - 1) {
      this.page++;
      this.loadFacturesTerminees();
    }
  }

  prevPage() {
    if (this.page > 0) {
      this.page--;
      this.loadFacturesTerminees();
    }
  }

  openCreateDialog() {
    this.editingFacture = null;
    this.nomClient = '';
    this.telephone = '';
    this.avance = 0;
    this.explications = '';
    this.selectedVentesIds = [];
    this.errorMessage = '';
    this.viewSalesHistory = false;
    this.typeFacture = 'CLASSIQUE';
    this.clientSuggestions = [];
    this.selectedClientId = null;
    this.loadVentesDuJour();
    this.showDialog = true;
  }

  loadVentesDuJour() {
    this.factureService.getVentesDuJour().subscribe(ventes => {
      this.ventesDuJour.set(ventes);
    });
  }

  loadVentesDernierMois() {
    this.factureService.getVentesDernierMois().subscribe(ventes => {
      this.ventesDernierMois.set(ventes);
    });
  }

  toggleSalesHistory() {
    this.viewSalesHistory = !this.viewSalesHistory;
    if (this.viewSalesHistory && this.ventesDernierMois().length === 0) {
      this.loadVentesDernierMois();
    } else if (!this.viewSalesHistory && this.ventesDuJour().length === 0) {
      this.loadVentesDuJour();
    }
  }

  openEditDialog(facture: Facture) {
    this.editingFacture = facture;
    this.nomClient = facture.nomClient;
    this.telephone = facture.client?.telephone || '';
    this.selectedVentesIds = facture.ventes ? facture.ventes.map(v => v.idVente) : [];
    this.errorMessage = '';
    this.viewSalesHistory = true;
    this.typeFacture = (facture.typeFacture as 'CLASSIQUE' | 'DEPOT') || 'CLASSIQUE';
    this.clientSuggestions = [];
    this.selectedClientId = facture.client?.idClient || null;
    this.avance = facture.avance || 0;
    this.explications = facture.explications || '';
    
    this.loadVentesDernierMois();
    this.showDialog = true;
  }

  toggleVenteSelection(id: number) {
    const index = this.selectedVentesIds.indexOf(id);
    if (index > -1) {
      this.selectedVentesIds.splice(index, 1);
    } else {
      this.selectedVentesIds.push(id);
    }
  }

  isVenteSelected(id: number): boolean {
    return this.selectedVentesIds.includes(id);
  }

  saveFacture(terminee: boolean) {
    if (!this.nomClient) {
      this.errorMessage = 'Veuillez saisir un nom de client.';
      return;
    }

    if (this.typeFacture === 'CLASSIQUE' && this.selectedVentesIds.length === 0) {
      this.errorMessage = 'Veuillez sélectionner au moins une vente pour une facture classique.';
      return;
    }

    if (this.typeFacture === 'DEPOT' && (!this.avance || this.avance <= 0)) {
      this.errorMessage = 'Veuillez saisir un montant de dépôt valide.';
      return;
    }

    if (this.editingFacture) {
      this.factureService.modifierFacture(this.editingFacture.idFacture, this.nomClient, this.selectedVentesIds, terminee, this.avance, this.typeFacture, this.telephone, this.selectedClientId, this.explications).subscribe((f) => {
        this.closeDialog();
        this.loadFactures();
        this.loadFacturesTerminees();
        this.clientSuggestions = [];
        if (terminee) {
          this.generatePDF(f);
        }
      });
    } else {
      this.factureService.enregistrerFacture(this.nomClient, this.selectedVentesIds, terminee, this.avance, this.typeFacture, this.telephone, this.selectedClientId, this.explications).subscribe((f) => {
        this.closeDialog();
        this.loadFactures();
        this.loadFacturesTerminees();
        this.clientSuggestions = [];
        if (terminee) {
          this.generatePDF(f);
        }
      });
    }
  }

  getTotalFacture(f: Facture): number {
    if (!f.ventes || f.ventes.length === 0) return 0;
    return f.ventes.reduce((sum, v) => sum + v.montantTotal, 0);
  }

  getTotalSelected(): number {
    const ventes = this.viewSalesHistory ? this.ventesDernierMois() : this.ventesDuJour();
    return ventes
      .filter(v => this.selectedVentesIds.includes(v.idVente))
      .reduce((sum, v) => sum + v.montantTotal, 0);
  }

  getRemiseTotal(f: Facture): number {
    if (!f.ventes || f.ventes.length === 0) return 0;
    return f.ventes.reduce((sum, v) => sum + (v.remise || 0), 0);
  }

  amountToWords(amount: number): string {
    if (amount === 0) return 'zéro';
    
    const units = ['', 'un', 'deux', 'trois', 'quatre', 'cinq', 'six', 'sept', 'huit', 'neuf'];
    const teens = ['dix', 'onze', 'douze', 'treize', 'quatorze', 'quinze', 'seize', 'dix-sept', 'dix-huit', 'dix-neuf'];
    const tens = ['', 'dix', 'vingt', 'trente', 'quarante', 'cinquante', 'soixante', 'soixante-dix', 'quatre-vingt', 'quatre-vingt-dix'];

    const convert = (n: number): string => {
      if (n < 10) return units[n];
      if (n < 20) return teens[n - 10];
      if (n < 100) {
        const unit = n % 10;
        const ten = Math.floor(n / 10);
        if (ten === 7) return 'soixante-' + (unit === 1 ? 'et-onze' : teens[unit]);
        if (ten === 9) return 'quatre-vingt-' + teens[unit];
        return tens[ten] + (unit === 0 ? '' : (unit === 1 ? '-et-' : '-') + units[unit]);
      }
      if (n < 1000) {
        const hundred = Math.floor(n / 100);
        const remainder = n % 100;
        let s = hundred === 1 ? 'cent' : units[hundred] + ' cent';
        if (hundred > 1 && remainder === 0) s += 's';
        return s + (remainder === 0 ? '' : ' ' + convert(remainder));
      }
      if (n < 1000000) {
        const thousand = Math.floor(n / 1000);
        const remainder = n % 1000;
        let s = thousand === 1 ? 'mille' : convert(thousand) + ' mille';
        return s + (remainder === 0 ? '' : ' ' + convert(remainder));
      }
      if (n < 1000000000) {
        const million = Math.floor(n / 1000000);
        const remainder = n % 1000000;
        let s = convert(million) + (million > 1 ? ' millions' : ' million');
        return s + (remainder === 0 ? '' : ' ' + convert(remainder));
      }
      return n.toString();
    };

    return convert(amount).toUpperCase() + ' FRANCS CFA';
  }

  generatePDF(facture: Facture) {
    this.factureToPrint = facture;
    
    // Wait for the DOM to update with the factureToPrint data
    setTimeout(() => {
      const data = document.getElementById('facture-to-print');
      if (data) {
        html2canvas(data, { scale: 2 }).then(canvas => {
          const imgWidth = 208;
          const imgHeight = canvas.height * imgWidth / canvas.width;
          const contentDataURL = canvas.toDataURL('image/png');
          const pdf = new jsPDF('p', 'mm', 'a4');
          const position = 0;
          pdf.addImage(contentDataURL, 'PNG', 0, position, imgWidth, imgHeight);
          pdf.save(`Facture_${facture.nomClient}_${facture.idFacture}.pdf`);
          this.factureToPrint = null;
        });
      }
    }, 100);
  }

  supprimerFacture(f: Facture, event: Event) {
    event.stopPropagation();
    this.factureToDelete = f;
    this.showDeleteConfirm = true;
  }

  confirmDelete() {
    if (this.factureToDelete) {
      this.factureService.supprimerFacture(this.factureToDelete.idFacture).subscribe(() => {
        this.loadFactures();
        this.showDeleteConfirm = false;
        this.factureToDelete = null;
      });
    }
  }

  cancelDelete() {
    this.showDeleteConfirm = false;
    this.factureToDelete = null;
  }

  closeDialog() {
    this.showDialog = false;
    this.editingFacture = null;
    this.viewSalesHistory = false;
    this.errorMessage = '';
    this.clientSuggestions = [];
  }

  onClientNameChange() {
    if (this.nomClient.length >= 2) {
      this.factureService.rechercherClients(this.nomClient).subscribe(clients => {
        this.clientSuggestions = clients;
      });
    } else {
      this.clientSuggestions = [];
    }
  }

  selectClient(client: Client) {
    this.nomClient = client.nom;
    this.telephone = client.telephone || '';
    this.selectedClientId = client.idClient;
    // Toujours afficher le solde complet, pas juste le montant de la vente
    if (this.typeFacture === 'CLASSIQUE' && client.soldeDisponible) {
      this.avance = client.soldeDisponible;
    }
    this.clientSuggestions = [];
  }

  onClientNameInput() {
    this.selectedClientId = null;
  }
}

