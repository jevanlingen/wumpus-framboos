import { JsonPipe } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, ElementRef, OnInit, ViewChild, WritableSignal, effect, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { GAME_ACTIONS, Game, GameAction } from '../model/game';
import { User } from '../model/user';
import { GameCanvasComponent } from './game-canvas/game-canvas.component';

const SIMPLE_PASSWORD = 'pw';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, JsonPipe, GameCanvasComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent implements OnInit {

  title = 'frontend-angular';
  users: WritableSignal<Array<User>> = signal([]);
  allGames: WritableSignal<Array<number>> = signal([]);
  gameInformation: WritableSignal<Game | undefined> = signal(undefined);
  userInformation: WritableSignal<User | undefined> = signal(undefined);
  gameActions = GAME_ACTIONS;

  constructor(private http: HttpClient) {

  }

  ngOnInit(): void {
    this.getUsers();
    this.getGames();

    // temp for easy access
    this.getGameInformation(1);
    this.getUserInformation(1);
  }

  createAccount() {


    this.http.post('/api/create-account', {
      name: `user-${(Math.random() + 1).toString(36).substring(7)}`,
      password: SIMPLE_PASSWORD
    }).subscribe(_ => {
      this.getUsers();
    });
  }

  getGameInformation(game: number) {
    this.http.get<Game>(`/api/games/${game}`).subscribe((gameInformation) => {
      this.gameInformation.set(gameInformation);
    });
  }

  closeGameInformation() {
    this.gameInformation.set(undefined);
  }

  getUserInformation(userId: number) {
    this.http.get<User>(`/api/users/${userId}`).subscribe((userInformation) => {
      this.userInformation.set(userInformation);
    });
  }

  closeUserInformation() {
    this.userInformation.set(undefined);
  }

  performGameAction(gameId: number, action: GameAction) {
    this.closeGameInformation();
    const headers = new HttpHeaders({
      "Authorization": `Basic ${window.btoa(this.userInformation()?.name + ':' + SIMPLE_PASSWORD)}`
    });

    this.http.post(`/api/games/${gameId}/action/${action}`, undefined, { headers })
      .subscribe(_ => this.getGameInformation(gameId)
    );
  }

  private getUsers() {
    this.http.get<Array<User>>('/api/users').subscribe((users) => {
      this.users.set(users ?? []);
    });
  }

  private getGames() {
    this.http.get<Array<any>>('/api/games/ids').subscribe((allGames) => {
      this.allGames.set(allGames ?? []);
    });
  }
}
