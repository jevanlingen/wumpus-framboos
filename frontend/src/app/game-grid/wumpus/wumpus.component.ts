import { Component } from '@angular/core';

@Component({
  selector: 'app-wumpus',
  standalone: true,
  imports: [],
  template: `
    <svg width="100%" height="100%" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
        <circle cx="50" cy="50" r="30" fill="#ccc" stroke="#222" stroke-width="4" />
        <ellipse cx="40" cy="40" rx="5" ry="8" fill="#fff" />
        <ellipse cx="60" cy="40" rx="5" ry="8" fill="#fff" />
        <ellipse cx="40" cy="42" rx="2" ry="3" />
        <ellipse cx="60" cy="42" rx="2" ry="3" />
        <path stroke="#000" stroke-width="2" d="M35 36h10m10 0h10" />
        <path d="M35 60q15 20 30 0-15 10-30 0" fill="none" stroke="#000" stroke-width="3" />
        <path stroke="#000" stroke-width="2" d="m45 65 2 10m8-10-2 10" />
        <path d="M20 30q10-20 20 0m20 0q10-20 20 0" fill="#ccc" stroke="#222" stroke-width="4" />
        <path d="M48 50h4l-2 5Z" />
    </svg>
  `,
  styleUrl: './wumpus.component.css'
})
export class WumpusComponent {

}
