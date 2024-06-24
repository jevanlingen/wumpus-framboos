import { HttpClient } from '@angular/common/http';
import { Component, OnInit, WritableSignal, inject, signal } from '@angular/core';
import { ActivatedRoute, ActivatedRouteSnapshot } from '@angular/router';
import { User } from '../../model/user';
import { Game } from '../../model/game';
import { GameGridComponent } from "../game-grid/game-grid.component";

interface UserWithScore {
  userId: number;
  username: string;
  points: number;
}

interface Competition {
  id: number;
  currentGameId: number;
  gameIds: number[];
  score: UserWithScore[];
}

@Component({
    selector: 'app-competition',
    standalone: true,
    templateUrl: './competition.component.html',
    styleUrl: './competition.component.css',
    imports: [GameGridComponent]
})
export class CompetitionComponent implements OnInit {
  http = inject(HttpClient);
  route = inject(ActivatedRoute);
  private refreshRate = 500;

  competition: WritableSignal<Competition | undefined> = signal(undefined);
  sortedUsers: WritableSignal<UserWithScore[]> = signal([]);
  gameInformation: WritableSignal<Game | undefined> = signal(undefined);
  private competitionId!: any;

  ngOnInit(): void {
    this.competitionId = this.route.snapshot.params['id'];
    this.getCompetition();
  }

  advance() {
    this.http.post(`/api/competitions/${this.competitionId}/action/advance`, {}).subscribe(_ => {
      this.getCompetition();
    });
  }

  private getCompetition() {
    this.http.get<Competition>(`/api/competitions/${this.competitionId}`).subscribe(c => {
      this.competition.set(c);
      this.sortedUsers.set(c.score.sort((a, b) => a.points - b.points));
      if(c.currentGameId >= 0){
        this.getGame(c.currentGameId);
      }
      setTimeout(() => this.getCompetition(), this.refreshRate);
    });
  }

  private getGame(id: any){
    this.http.get<Game>(`/api/games/${id}`).subscribe((gameInformation) => {
      this.gameInformation.set(gameInformation);
      setTimeout(() => this.getGame(id), this.refreshRate);
    });
  }
  

}
