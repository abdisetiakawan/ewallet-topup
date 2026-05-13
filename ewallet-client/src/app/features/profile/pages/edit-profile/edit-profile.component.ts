import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Location } from '@angular/common';

@Component({
  selector: 'app-edit-profile',
  standalone: true,
  imports: [],
  templateUrl: './edit-profile.component.html',
  styleUrl: './edit-profile.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EditProfileComponent {
  constructor(private location: Location) {}

  goBack(): void {
    this.location.back();
  }
}
