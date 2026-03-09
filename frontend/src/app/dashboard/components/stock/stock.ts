import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProduitService, Produit, Page } from '../../../services/produit.service';
import { AuthService } from '../../../services/auth.service';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

@Component({
  selector: 'app-stock',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './stock.html',
  styleUrls: ['./stock.css']
})
export class StockComponent implements OnInit {
  produits = signal<Produit[]>([]);
  currentPage = signal<number>(0);
  pageSize = 6;
  totalPages = signal<number>(0);
  totalElements = signal<number>(0);
  error = signal<string | null>(null);
  
  isAdmin = signal<boolean>(false);
  editingProduit = signal<Produit | null>(null);
  enlargedImage = signal<string | null>(null);
  isAdding = signal<boolean>(false);
  editForm: FormGroup;
  
  targetPage = '';
  highlightStock = signal<boolean>(true);

  // Recherche
  searchKeyword = '';
  searchType = 'designation';
  isSearching = signal<boolean>(false);
  filterCritique = signal<boolean>(false);
  private searchSubject = new Subject<string>();

  constructor(
    private produitService: ProduitService,
    private authService: AuthService,
    private fb: FormBuilder
  ) {
    this.editForm = this.fb.group({
      designation: ['', Validators.required],
      code: [''],
      photo: [''],
      quantite: [0, [Validators.required, Validators.min(0)]],
      prixUnitaire: [0, [Validators.required, Validators.min(0)]],
      dernierPrix: [0, [Validators.required, Validators.min(0)]],
      motDePasseAdmin: ['', Validators.required]
    });
  }

  ngOnInit() {
    this.isAdmin.set(this.authService.getCurrentUser()?.isAdmin || false);
    
    this.updateLocalSettings();
    
    // Ecouter les changements de paramètres
    window.addEventListener('settingsChanged', () => {
      this.updateLocalSettings();
    });

    this.loadProducts(this.currentPage());

    // Debounce de recherche
    this.searchSubject.pipe(
      debounceTime(400),
      distinctUntilChanged()
    ).subscribe(term => {
      if (term.length >= 2) {
        this.performSearch(term);
      } else if (term.length === 0) {
        this.resetSearch();
      }
    });
  }

  updateLocalSettings() {
    const saved = localStorage.getItem('app_settings');
    if (saved) {
      this.highlightStock.set(JSON.parse(saved).highlightStock ?? true);
    }
  }

  onSearchChange() {
    this.searchSubject.next(this.searchKeyword);
  }

  performSearch(term: string) {
    this.isSearching.set(true);
    this.error.set(null);
    
    const obs = this.searchType === 'code' 
      ? this.produitService.rechercherParCode(term)
      : this.produitService.rechercherProduits(term);

    obs.subscribe({
      next: (results) => {
        this.produits.set(results);
        this.totalPages.set(0); // Pas de pagination en recherche simple
      },
      error: (err) => {
        console.error('Erreur recherche', err);
        this.error.set("Aucun résultat trouvé ou erreur serveur.");
        this.produits.set([]);
      }
    });
  }

  resetSearch() {
    this.searchKeyword = '';
    this.isSearching.set(false);
    this.loadProducts(0);
  }

  loadProducts(page: number) {
    this.error.set(null);
    const obs = this.filterCritique() 
      ? this.produitService.getProduitsCritiquesPaginated(page, this.pageSize)
      : this.produitService.getProduitsPaginated(page, this.pageSize);

    obs.subscribe({
      next: (response: Page<Produit>) => {
        this.produits.set(response.content);
        this.totalPages.set(response.totalPages);
        this.totalElements.set(response.totalElements);
        this.currentPage.set(response.number);
      },
      error: (err: any) => {
        console.error('Erreur lors du chargement des produits', err);
        this.error.set("Impossible de charger les produits.");
      }
    });
  }

  toggleFilterCritique() {
    this.filterCritique.set(!this.filterCritique());
    this.searchKeyword = ''; // On reset la recherche quand on change de filtre
    this.isSearching.set(false);
    this.loadProducts(0);
  }

  onPageChange(newPage: number) {
    if (newPage >= 0 && newPage < this.totalPages()) {
      this.loadProducts(newPage);
    }
  }

  goToPage() {
    const pageNum = parseInt(this.targetPage, 10);
    if (isNaN(pageNum) || pageNum < 1 || pageNum > this.totalPages()) {
      this.error.set(`Numéro de page invalide (1-${this.totalPages()})`);
      return;
    }
    this.loadProducts(pageNum - 1);
    this.targetPage = '';
  }

  startAdd() {
    if (!this.isAdmin()) return;
    this.isAdding.set(true);
    this.editingProduit.set(null);
    this.editForm.reset({
      quantite: 0,
      prixUnitaire: 0,
      dernierPrix: 0,
      motDePasseAdmin: ''
    });
  }

  startEdit(produit: Produit) {
    if (!this.isAdmin()) return;
    
    this.isAdding.set(false);
    this.editingProduit.set(produit);
    this.editForm.patchValue({
      designation: produit.designation,
      code: produit.code,
      photo: produit.photo,
      quantite: produit.quantite,
      prixUnitaire: produit.prixUnitaire,
      dernierPrix: produit.dernierPrix,
      motDePasseAdmin: ''
    });
  }

  cancelEdit() {
    this.editingProduit.set(null);
    this.isAdding.set(false);
    this.editForm.reset();
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = () => {
        this.editForm.patchValue({
          photo: reader.result as string
        });
      };
      reader.readAsDataURL(file);
    }
  }

  toggleEnlarge(photo: string) {
    this.enlargedImage.set(photo);
  }

  closeEnlarge() {
    this.enlargedImage.set(null);
  }

  deleteProduit(id: number) {
    if (!this.isAdmin()) return;
    if (confirm('Êtes-vous sûr de vouloir supprimer ce produit ?')) {
      this.produitService.deleteProduit(id).subscribe({
        next: () => {
          this.loadProducts(this.currentPage());
        },
        error: (err: any) => {
          alert(err.error?.message || "Erreur lors de la suppression");
        }
      });
    }
  }

  save() {
    if (this.editForm.invalid) return;

    if (this.isAdding()) {
      this.produitService.createProduit(this.editForm.value).subscribe({
        next: () => {
          this.cancelEdit();
          this.loadProducts(0);
        },
        error: (err: any) => {
          alert(err.error?.message || "Erreur lors de l'ajout");
        }
      });
    } else if (this.editingProduit()) {
      const id = this.editingProduit()!.idProduit;
      this.produitService.updateProduit(id, this.editForm.value).subscribe({
        next: () => {
          this.cancelEdit();
          this.loadProducts(this.currentPage());
        },
        error: (err: any) => {
          alert(err.error?.message || "Erreur lors de la modification");
        }
      });
    }
  }
}
