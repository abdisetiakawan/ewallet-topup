import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-top-app-bar',
  standalone: true,
  imports: [],
  templateUrl: './top-app-bar.component.html',
  styleUrl: './top-app-bar.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TopAppBarComponent {}
