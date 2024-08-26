import { Component } from '@angular/core';

@Component({
  selector: 'app-treasure',
  standalone: true,
  imports: [],
  template: `
  <svg width="100%" height="100%" viewBox="0 -3.99 48.891 48.891" xmlns="http://www.w3.org/2000/svg">
      <g data-name="gold bars shiny">
          <path data-name="Path 7" d="m24.446 40.409-3.991-11.973H4.491L.5 40.409Z" fill="#ffe959"
              fill-rule="evenodd" />
          <path data-name="Path 8" d="M48.391 40.409 44.4 28.436H28.436l-3.99 11.973Z" fill="#ffe959"
              fill-rule="evenodd" />
          <path data-name="Path 9" d="m36.418 28.436-3.99-11.973H16.464l-3.991 11.973Z" fill="#ffe959"
              fill-rule="evenodd" />
          <path
              d="m12.473 3.708 3.991 6.913M3.707 12.472l6.914 3.991M.5 24.445h7.982m39.909 0h-7.982m4.773-11.973-6.912 3.991M36.418 3.708l-3.99 6.911M24.447.498v7.981m11.971 19.957-3.99-11.973H16.464l-3.991 11.973Zm9.31 3.991-1.331-3.991H28.436l-3.99 11.973h23.945l-1.331-3.99m-45.229 0L.5 40.41h23.946l-3.991-11.973H4.491L3.16 32.428"
              fill="none" stroke="#0f0e0b" stroke-linecap="round" stroke-linejoin="round" />
      </g>
  </svg>
  `,
  styleUrl: './treasure.component.css'
})
export class TreasureComponent {

}
