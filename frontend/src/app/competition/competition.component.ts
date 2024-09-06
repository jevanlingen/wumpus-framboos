import { HttpClient } from '@angular/common/http';
import { Component, DestroyRef, OnDestroy, OnInit, WritableSignal, computed, effect, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { Game, Player } from '../../model/game';
import { AvatarComponent } from "../avatar/avatar.component";
import { GameGridComponent } from "../game-grid/game-grid.component";
import { TreasureComponent } from '../game-grid/treasure/treasure.component';

interface Score {
  points: number;
  userId: any;
  username: string;
  skinColor: string;
  trouserColor: string;
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
  imports: [GameGridComponent, AvatarComponent, TreasureComponent]
})
export class CompetitionComponent implements OnInit, OnDestroy {



  http = inject(HttpClient);
  route = inject(ActivatedRoute);
  destroyRef = inject(DestroyRef);
  private refreshRate = 150;

  competition: WritableSignal<Competition | undefined> = signal(undefined);
  currentGameId = computed(() => this.competition()?.currentGameId);
  sortedScores: WritableSignal<Score[]> = signal([]);
  gameInformation: WritableSignal<Game | undefined> = signal(undefined);
  isFinished = false;
  highlightUserId: WritableSignal<string | undefined> = signal(undefined);
  private competitionId!: any;
  private competitionTimeout?: any;
  private gameTimeout?: any;
  private gameSub?: Subscription;
  private competitionSub?: Subscription;

  constructor() {
    effect(() => {
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
    if (this.isFinalGame) {
      this.isFinished = true;
    }
    this.http
      .post(`/api/competitions/${this.competitionId}/action/advance`, {})
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(_ => {
        this.getCompetition();
      });
  }

  highlight(userId: string | undefined) {
    this.highlightUserId.set(userId);
  }

  highlightAndScroll(userId: string | undefined) {
    this.highlightUserId.set(userId);
    if(userId){
      document?.getElementById(`row-${userId}`)?.scrollIntoView();
    }
  }

  get currentGameNumber(): number {
    return (this.competition()?.gameIds?.indexOf(this.competition()?.currentGameId!!) ?? -1) + 1;
  }

  get numberOfGames(): number {
    return this.competition()?.gameIds?.length ?? Infinity;
  }

  get isFinalGame(): boolean {
    return this.currentGameNumber === this.numberOfGames;
  }

  getGameUserState(userId: number): Player | undefined {
    return this.gameInformation()?.players.find(p => p.user.id === userId);
  }

  private getCompetition() {
    this.stopCompetitionRefresh();
    this.competitionSub = this.http
      .get<Competition>(`/api/competitions/${this.competitionId}`)
      .subscribe((competition) => {
        this.competition.set(competition);
        this.sortedScores.set(competition.score.sort((a, b) => b.points - a.points));
        this.competitionTimeout = setTimeout(() => this.getCompetition(), this.refreshRate);
      });
  }

  private getGame(id: any) {
    this.stopGameRefresh();

    this.gameSub = this.http
      .get<Game>(`/api/games/${id}`)
      .subscribe((gameInformation) => {
        this.gameInformation.set(gameInformation);
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
