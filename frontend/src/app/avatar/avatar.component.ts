import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-avatar',
  standalone: true,
  imports: [],
  template: `
  <svg width="100%" height="100%" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
      <ellipse cx="50" cy="50" rx="40" ry="20" [attr.fill]="'#' + trouserColor" stroke="black"
          stroke-width="2" />
      <circle cx="50" cy="25" r="5" [attr.fill]="'#' + skinColor" stroke="black"
          stroke-width="1" />
      <circle cx="50" cy="50" r="25" [attr.fill]="'#' + skinColor" stroke="black"
          stroke-width="1" />
  </svg>`,
  styleUrl: './avatar.component.css'
})
export class AvatarComponent {
  @Input({ required: true }) trouserColor!: string;
  @Input({ required: true }) skinColor!: string;
}
