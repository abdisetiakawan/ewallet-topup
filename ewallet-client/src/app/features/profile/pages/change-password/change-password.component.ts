import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
} from '@angular/core';
import { AsyncPipe, Location } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { BehaviorSubject, Subscription } from 'rxjs';
import { ChangePasswordRequest, UserApiService } from '../../../../core/services/user-api.service';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [AsyncPipe, FormsModule],
  templateUrl: './change-password.component.html',
  styleUrl: './change-password.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ChangePasswordComponent implements OnDestroy {
  private userApi = inject(UserApiService);
  private location = inject(Location);
  private cdr = inject(ChangeDetectorRef);

  oldPassword = '';
  newPassword = '';
  confirmPassword = '';

  showOld = false;
  showNew = false;
  showConfirm = false;

  private readonly loadingSubject = new BehaviorSubject<boolean>(false);
  private readonly errorSubject = new BehaviorSubject<string | null>(null);
  private subs = new Subscription();

  readonly loading$ = this.loadingSubject.asObservable();
  readonly error$ = this.errorSubject.asObservable();

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  get passwordStrength(): 'weak' | 'medium' | 'strong' {
    const p = this.newPassword;
    if (p.length < 8) return 'weak';
    const hasLetter = /[a-zA-Z]/.test(p);
    const hasNumber = /[0-9]/.test(p);
    const hasSymbol = /[^a-zA-Z0-9]/.test(p);
    if ((hasLetter && hasNumber) || (hasLetter && hasSymbol) || (hasNumber && hasSymbol)) {
      return p.length >= 12 ? 'strong' : 'medium';
    }
    return 'weak';
  }

  get passwordsMatch(): boolean {
    return this.confirmPassword.length > 0 && this.newPassword === this.confirmPassword;
  }

  get passwordsMismatch(): boolean {
    return this.confirmPassword.length > 0 && this.newPassword !== this.confirmPassword;
  }

  onSubmit(form: NgForm): void {
    if (form.invalid || this.passwordsMismatch) {
      form.control.markAllAsTouched();
      return;
    }

    const payload: ChangePasswordRequest = {
      oldPassword: this.oldPassword,
      newPassword: this.newPassword,
    };

    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    this.subs.add(
      this.userApi.changePassword(payload).subscribe({
        next: () => {
          this.loadingSubject.next(false);
          this.location.back();
        },
        error: (err) => {
          this.errorSubject.next(this.resolveError(err));
          this.loadingSubject.next(false);
          this.cdr.markForCheck();
        },
      })
    );
  }

  goBack(): void {
    this.location.back();
  }

  private resolveError(error: unknown): string {
    if (
      typeof error === 'object' &&
      error !== null &&
      'error' in error &&
      typeof (error as { error?: { message?: unknown } }).error?.message === 'string'
    ) {
      return (error as { error: { message: string } }).error.message;
    }
    return 'Terjadi kesalahan. Silakan coba lagi.';
  }
}
