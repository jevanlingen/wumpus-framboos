import { HttpClient } from '@angular/common/http';
import { Component, OnInit, WritableSignal, inject, signal } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { isDevMode } from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
  imports: [RouterOutlet, RouterLink]
})
export class AppComponent implements OnInit {
  http = inject(HttpClient);
  isDevelopment = isDevMode();

  competitions: WritableSignal<Array<number>> = signal([]);

  ngOnInit(): void {
    this.getCompetitions();
  }

  private getCompetitions() {
    this.http.get('/api/competitions/ids').subscribe(competitionsResponse => {
      this.competitions.set(competitionsResponse as number[]);
      setTimeout(() => this.getCompetitions(), 5000);
    });
  }

}
