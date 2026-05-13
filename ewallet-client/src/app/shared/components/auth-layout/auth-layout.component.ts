import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

export type AuthTab = 'login' | 'register';

@Component({
  selector: 'app-auth-layout',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './auth-layout.component.html',
  styleUrl: './auth-layout.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AuthLayoutComponent {
  /** Route yang dituju tombol back */
  @Input() backRoute: string = '/';

  /** Tab aktif di auth bottom nav */
  @Input() activeTab: AuthTab = 'login';
}
