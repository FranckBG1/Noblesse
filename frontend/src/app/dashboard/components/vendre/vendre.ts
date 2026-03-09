import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VenteService, VenteRequest } from '../../../services/ventes.service';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-vendre',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ventes.html',
  styleUrls: ['./ventes.css'],
})
export class VendreComponent implements OnInit {
  // Formulaire
  searchKeyword = '';
  searchType = 'designation';
  selectedProduit: any = null;
  quantite = 1;
  prixVente = 0;
  remise = 0;

  // États UI
  searchResults: any[] = [];
  isPrixModifiable = false;
  errorMessage = '';
  successMessage = '';
  isStockInsuffisant = false;
  showConfirmation = false;
  isVenteReussie = false;
  isLoading = false;

  private searchSubject = new Subject<string>();

  constructor(
    private venteService: VenteService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    // Logique de recherche "LIKE" avec un petit délai (debounce) pour ne pas saturer le serveur
    this.searchSubject
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((term) => {
          if (this.searchType === 'code') {
            return this.venteService.rechercherParCode(term);
          } else {
            return this.venteService.rechercherProduits(term);
          }
        }),
      )
      .subscribe((results) => {
        this.searchResults = results;
      });
  }

  onSearchChange() {
    if (this.searchKeyword.length >= 2) {
      this.searchSubject.next(this.searchKeyword);
    } else {
      this.searchResults = [];
    }
    // Si l'utilisateur change le texte, on déselectionne le produit précédent
    this.selectedProduit = null;
    this.checkStock();
  }

  selectProduit(produit: any) {
    this.selectedProduit = produit;
    this.searchKeyword = this.searchType === 'code' ? produit.code : produit.designation;
    this.prixVente = produit.prixUnitaire;
    this.searchResults = [];
    this.isPrixModifiable = false;
    this.checkStock();
  }

  checkStock() {
    if (this.selectedProduit) {
      this.isStockInsuffisant = this.quantite > this.selectedProduit.quantite;
    } else {
      this.isStockInsuffisant = false;
    }
  }

  toggleModifierPrix() {
    this.isPrixModifiable = !this.isPrixModifiable;
  }

  validerVente() {
    this.showConfirmation = true;
  }

  confirmVente() {
    if (this.isLoading) return;
    this.isLoading = true;

    const request: VenteRequest = {
      idProduit: this.selectedProduit?.idProduit || null,
      nomProduitHorsStock: !this.selectedProduit ? this.searchKeyword : undefined,
      prixVente: this.prixVente,
      quantite: this.quantite,
      remise: this.remise > 0 ? this.remise : undefined,
    };

    this.venteService.enregistrerVente(request).subscribe({
      next: () => {
        this.showConfirmation = false;
        this.isLoading = false;
        this.isVenteReussie = true;
        this.successMessage = 'Vente effectuée avec succès !';
        this.cdr.detectChanges();
        
        // On attend 2 secondes pour que l'utilisateur voit le message de réussite (placeholder)
        setTimeout(() => {
          this.resetForm();
          this.isVenteReussie = false; // Réinitialise l'état pour la prochaine vente
          this.cdr.detectChanges();
        }, 2000);
      },
      error: (err) => {
        this.isLoading = false;
        this.showConfirmation = false;
        this.errorMessage = err.error?.message || 'Erreur lors de la vente';
        this.cdr.detectChanges();
        setTimeout(() => {
          this.errorMessage = '';
          this.cdr.detectChanges();
        }, 5000);
      },
    });
  }

  cancelVente() {
    this.showConfirmation = false;
  }

  resetForm() {
    this.searchKeyword = '';
    this.selectedProduit = null;
    this.quantite = 1;
    this.prixVente = 0;
    this.remise = 0;
    this.isPrixModifiable = false;
    this.isStockInsuffisant = false;
    this.showConfirmation = false;
    this.cdr.detectChanges();
  }
}
