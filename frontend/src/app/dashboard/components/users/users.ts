import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UsersService, UserUpdateData, UserCreateData } from '../../../services/users.service';
import { AuthService, UserResponse } from '../../../services/auth.service';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './users.html',
  styleUrl: './users.css',
})
export class UsersComponent implements OnInit {
  users: UserResponse[] = [];
  selectedUser: UserResponse | null = null;
  editData: UserUpdateData = { nom: '', motDePasse: '', isAdmin: false, motDePasseAdmin: '' };

  // Create mode
  showCreateForm = false;
  newData: UserCreateData = { nom: '', motDePasse: '', isAdmin: false };

  // UI state
  isActionLoading = false;
  actionMessage = '';
  isError = false;

  // Modals
  showDeleteConfirm = false;
  showAdminPassConfirm = false;
  showDeleteAdminPassConfirm = false;
  showCreatePassConfirm = false;
  adminPasswordForPromotion = '';
  adminPasswordForDeletion = '';
  adminPasswordForCreation = '';

  constructor(
    private usersService: UsersService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    this.usersService.getUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Erreur chargement users', err),
    });
  }

  toggleCreateForm() {
    this.showCreateForm = !this.showCreateForm;
    this.actionMessage = '';
    this.newData = { nom: '', motDePasse: '', isAdmin: false };
    this.cdr.detectChanges();
  }

  selectUser(user: UserResponse) {
    this.showCreateForm = false;
    this.selectedUser = user;
    this.editData = {
      nom: user.nom,
      motDePasse: '',
      isAdmin: user.isAdmin,
      motDePasseAdmin: '',
    };
    this.actionMessage = '';
    this.cdr.detectChanges();
  }

  // --- CRÉATION ---
  onCreate() {
    if (!this.newData.nom || !this.newData.motDePasse) {
      this.showMessage('Veuillez remplir tous les champs', true);
      return;
    }
    this.showCreatePassConfirm = true;
    this.adminPasswordForCreation = '';
    this.cdr.detectChanges();
  }

  confirmCreate() {
    if (!this.adminPasswordForCreation) {
      this.showMessage('Mot de passe requis', true);
      return;
    }

    this.isActionLoading = true;
    this.showCreatePassConfirm = false;

    const payload = { ...this.newData, motDePasseAdmin: this.adminPasswordForCreation };

    this.usersService.createUser(payload).subscribe({
      next: () => {
        this.showMessage('Utilisateur créé avec succès !');
        this.loadUsers();
        this.showCreateForm = false;
        this.isActionLoading = false;
        this.adminPasswordForCreation = '';
      },
      error: (err) => {
        const msg = this.getErrorMessage(err);
        this.showMessage(msg, true);
        this.isActionLoading = false;
      },
    });
  }

  // --- MODIFICATION ---
  onPrepareUpdate() {
    if (!this.selectedUser) return;

    // Check if toggling admin rights (granting OR revoking)
    if (this.editData.isAdmin !== this.selectedUser.isAdmin) {
      this.showAdminPassConfirm = true;
      this.adminPasswordForPromotion = '';
    } else {
      this.confirmUpdate();
    }
    this.cdr.detectChanges();
  }

  cancelUpdate() {
    this.showAdminPassConfirm = false;
    this.adminPasswordForPromotion = '';
    this.cdr.detectChanges();
  }

  confirmUpdate() {
    if (!this.selectedUser) return;

    this.isActionLoading = true;
    this.showAdminPassConfirm = false;

    // Inclure le mdp admin si nécessaire
    const payload = { ...this.editData, motDePasseAdmin: this.adminPasswordForPromotion };

    this.usersService.updateUser(this.selectedUser.idUsers, payload).subscribe({
      next: () => {
        this.showMessage('Utilisateur mis à jour !');
        this.loadUsers();
        this.isActionLoading = false;
        this.adminPasswordForPromotion = '';
      },
      error: (err) => {
        const msg = this.getErrorMessage(err);
        this.showMessage(msg, true);
        this.isActionLoading = false;
      },
    });
  }

  // --- SUPPRESSION ---
  onPrepareDelete() {
    if (!this.selectedUser) return;

    // Vérifier si c'est soi-même
    const currentUserId = this.authService.getCurrentUser()?.idUsers;
    if (this.selectedUser.idUsers === currentUserId) {
      this.showMessage('Vous ne pouvez pas supprimer votre propre compte.', true);
      return;
    }

    if (this.selectedUser.isAdmin) {
      this.showDeleteAdminPassConfirm = true;
      this.adminPasswordForDeletion = '';
    } else {
      this.showDeleteConfirm = true;
    }
    this.cdr.detectChanges();
  }

  confirmDelete() {
    if (!this.selectedUser) return;

    // Si c'est un admin et que le mdp n'est pas saisi
    if (this.selectedUser.isAdmin && !this.adminPasswordForDeletion) {
      this.showMessage('Mot de passe requis pour supprimer un admin', true);
      return;
    }

    this.isActionLoading = true;
    this.showDeleteConfirm = false;
    this.showDeleteAdminPassConfirm = false;

    this.usersService.deleteUser(this.selectedUser.idUsers, this.adminPasswordForDeletion).subscribe({
      next: () => {
        this.showMessage('Utilisateur supprimé');
        this.selectedUser = null;
        this.loadUsers();
        this.isActionLoading = false;
        this.adminPasswordForDeletion = '';
      },
      error: (err) => {
        const msg = this.getErrorMessage(err);
        this.showMessage(msg, true);
        this.isActionLoading = false;
      },
    });
  }

  private getErrorMessage(err: any): string {
    if (err.error?.message) {
      return err.error.message;
    }
    if (typeof err.error === 'object' && err.error !== null) {
      const errorValues = Object.values(err.error);
      if (errorValues.length > 0) {
        return String(errorValues[0]);
      }
    }
    return 'Une erreur est survenue';
  }

  private showMessage(msg: string, error = false) {
    this.actionMessage = msg;
    this.isError = error;
    this.cdr.detectChanges();
    setTimeout(() => {
      this.actionMessage = '';
      this.cdr.detectChanges();
    }, 4000);
  }
}
