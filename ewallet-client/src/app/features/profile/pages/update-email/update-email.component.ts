import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { AsyncPipe, Location } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { BehaviorSubject, Subscription } from 'rxjs';
import {
  EmailChangeConfirmRequest,
  EmailChangeRequest,
  UserApiService,
} from '../../../../core/services/user-api.service';
import { ProfileStoreService } from '../../../../core/services/profile-store.service';

type Step = 'request' | 'confirm';

@Component({
  selector: 'app-update-email',
  standalone: true,
  imports: [AsyncPipe, FormsModule],
  templateUrl: './update-email.component.html',
  styleUrl: './update-email.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UpdateEmailComponent implements OnInit, OnDestroy {
  private userApi = inject(UserApiService);
  private profileStore = inject(ProfileStoreService);
  private location = inject(Location);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  step: Step = 'request';

  // Step 1 — request
  newEmail = '';
  currentEmail = '';

  // Step 2 — confirm
  token = '';
  pendingEmail = '';
  expiresInMinutes = 5;
  countdown = 0;
  private countdownInterval: ReturnType<typeof setInterval> | null = null;

  // Shared state
  private readonly loadingSubject = new BehaviorSubject<boolean>(false);
  private readonly errorSubject = new BehaviorSubject<string | null>(null);
  private subs = new Subscription();

  readonly loading$ = this.loadingSubject.asObservable();
  readonly error$ = this.errorSubject.asObservable();

  ngOnInit(): void {
    const user = this.profileStore.currentUser;
    if (user) {
      this.currentEmail = user.email;
    }
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
    this.stopCountdown();
  }

  // ── Step 1 ──────────────────────────────────────────────────────────────────
  onRequestSubmit(form: NgForm): void {
    if (form.invalid) {
      form.control.markAllAsTouched();
      return;
    }

    const payload: EmailChangeRequest = { newEmail: this.newEmail };
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    this.subs.add(
      this.userApi.requestEmailChange(payload).subscribe({
        next: (res) => {
          this.pendingEmail = res.data.newEmail;
          this.expiresInMinutes = res.data.expiresInMinutes;
          this.countdown = res.data.expiresInMinutes * 60;
          this.step = 'confirm';
          this.loadingSubject.next(false);
          this.startCountdown();
          this.cdr.markForCheck();
        },
        error: (err) => {
          this.errorSubject.next(this.resolveError(err));
          this.loadingSubject.next(false);
          this.cdr.markForCheck();
        },
      })
    );
  }

  // ── Step 2 ──────────────────────────────────────────────────────────────────
  onConfirmSubmit(form: NgForm): void {
    if (form.invalid) {
      form.control.markAllAsTouched();
      return;
    }

    const payload: EmailChangeConfirmRequest = { token: this.token.trim() };
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    this.subs.add(
      this.userApi.confirmEmailChange(payload).subscribe({
        next: (res) => {
          this.profileStore.applyUser(res.data);
          this.loadingSubject.next(false);
          this.stopCountdown();
          this.router.navigate(['/account/edit-profile']);
        },
        error: (err) => {
          this.errorSubject.next(this.resolveError(err));
          this.loadingSubject.next(false);
          this.cdr.markForCheck();
        },
      })
    );
  }

  resendCode(): void {
    if (this.countdown > 0) return;
    this.token = '';
    this.errorSubject.next(null);
    this.step = 'request';
    this.cdr.markForCheck();
  }

  goBack(): void {
    if (this.step === 'confirm') {
      this.step = 'request';
      this.token = '';
      this.errorSubject.next(null);
      this.stopCountdown();
      this.cdr.markForCheck();
    } else {
      this.location.back();
    }
  }

  get formattedCountdown(): string {
    const m = Math.floor(this.countdown / 60);
    const s = this.countdown % 60;
    return `${m}:${s.toString().padStart(2, '0')}`;
  }

  // ── Helpers ─────────────────────────────────────────────────────────────────
  private startCountdown(): void {
    this.stopCountdown();
    this.countdownInterval = setInterval(() => {
      if (this.countdown > 0) {
        this.countdown--;
        this.cdr.markForCheck();
      } else {
        this.stopCountdown();
      }
    }, 1000);
  }

  private stopCountdown(): void {
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval);
      this.countdownInterval = null;
    }
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
