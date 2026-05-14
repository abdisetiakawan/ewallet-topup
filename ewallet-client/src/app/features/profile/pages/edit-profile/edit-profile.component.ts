import { ChangeDetectionStrategy, Component, inject, OnInit } from '@angular/core';
import { AsyncPipe, Location } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ProfileStoreService } from '../../../../core/services/profile-store.service';

@Component({
  selector: 'app-edit-profile',
  standalone: true,
  imports: [AsyncPipe, FormsModule, RouterLink],
  templateUrl: './edit-profile.component.html',
  styleUrl: './edit-profile.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EditProfileComponent implements OnInit {
  private profileStore = inject(ProfileStoreService);
  private location = inject(Location);

  name = '';
  email = '';

  readonly loading$ = this.profileStore.loading$;
  readonly error$ = this.profileStore.error$;

  ngOnInit(): void {
    const user = this.profileStore.currentUser;
    if (user) {
      this.name = user.name;
      this.email = user.email;
    }
  }

  onSubmit(form: NgForm): void {
    if (form.invalid) {
      form.control.markAllAsTouched();
      return;
    }

    this.profileStore.updateProfile({ name: this.name }).subscribe({
      next: () => this.location.back(),
    });
  }

  goBack(): void {
    this.location.back();
  }
}
