import { HttpClient } from '@angular/common/http';
import { Component, DestroyRef, OnDestroy, OnInit, WritableSignal, computed, effect, inject, signal } from '@angular/core';
import { ActivatedRoute, ActivatedRouteSnapshot } from '@angular/router';
import { User } from '../../model/user';
import { Game } from '../../model/game';
import { GameGridComponent } from "../game-grid/game-grid.component";
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subscription, concatMap, withLatestFrom } from 'rxjs';
import { UserService } from '../service/user.service';

interface UserWithScore extends User {
  points: number;
}

interface Score {
  points: number;
  userId: any;
  username: string;
}

interface Competition {
  id: number;
  currentGameId: number;
  gameIds: number[];
  score: Score[];
}

@Component({
  selector: 'app-competition',
  standalone: true,
  templateUrl: './competition.component.html',
  styleUrl: './competition.component.css',
  imports: [GameGridComponent]
})
export class CompetitionComponent implements OnInit, OnDestroy {

  http = inject(HttpClient);
  route = inject(ActivatedRoute);
  destroyRef = inject(DestroyRef);
  userService = inject(UserService);
  private refreshRate = 5000;

  competition: WritableSignal<Competition | undefined> = signal(undefined);
  currentGameId = computed(() => this.competition()?.currentGameId);
  sortedUsers: WritableSignal<UserWithScore[]> = signal([]);
  gameInformation: WritableSignal<Game | undefined> = signal(undefined);
  private competitionId!: any;
  private competitionTimeout?: any;
  private gameTimeout?: any;
  private gameSub?: Subscription;
  private competitionSub?: Subscription;

  constructor() {
    effect(() => {
      console.log('Current game id', this.currentGameId());
      if (this.currentGameId()) {
        this.getGame(this.currentGameId());
      }
    })
  }

  ngOnInit(): void {
    this.competitionId = this.route.snapshot.params['id'];
    this.getCompetition();
  }

  ngOnDestroy(): void {
    this.stopCompetitionRefresh();
    this.stopGameRefresh();
  }

  advance() {
    this.http
      .post(`/api/competitions/${this.competitionId}/action/advance`, {})
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(_ => {
        this.getCompetition();
      });
  }

  private getCompetition() {
    this.stopCompetitionRefresh();
    this.competitionSub = this.http
      .get<Competition>(`/api/competitions/${this.competitionId}`)
      .pipe(withLatestFrom(this.userService.users$))
      .subscribe(([competition, allUsers]) => {
        this.competition.set(competition);
        this.updateUsers(allUsers, competition.score);
        this.competitionTimeout = setTimeout(() => this.getCompetition(), this.refreshRate);
      });
  }

  private updateUsers(allUsers: User[], scores: Score[]) {
    const usersWithScore = allUsers
      .map(u => ({
        ...u,
        points: scores.find(s => s.userId === u.id)?.points ?? -Infinity
      }))
      .filter(u => u.points !== -Infinity)
      .sort((a, b) => b.points - a.points);

    this.sortedUsers.set(usersWithScore);
  }

  private getGame(id: any) {
    this.stopGameRefresh();
    console.log('get Game', id);

    this.gameSub = this.http
      .get<Game>(`/api/games/${id}`)
      .subscribe((gameInformation) => {
        this.gameInformation.set(gameInformation);
        console.log('set timeout', id);

        this.gameTimeout = setTimeout(() => this.getGame(id), this.refreshRate);
      });
  }

  private stopCompetitionRefresh() {
    this.competitionSub?.unsubscribe();
    clearTimeout(this.competitionTimeout);
  }

  private stopGameRefresh() {
    this.gameSub?.unsubscribe();
    clearTimeout(this.gameTimeout);
  }

}
