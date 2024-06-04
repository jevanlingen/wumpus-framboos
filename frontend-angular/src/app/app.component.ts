import { JsonPipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, ElementRef, OnInit, ViewChild, WritableSignal, effect, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Game } from '../model/game';
import { User } from '../model/user';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, JsonPipe],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent implements OnInit {
  title = 'frontend-angular';
  users: WritableSignal<Array<User>> = signal([]);
  allGames: WritableSignal<Array<number>> = signal([]);
  gameInformation: WritableSignal<Game | undefined> = signal(undefined);
  userInformation: WritableSignal<User | undefined> = signal(undefined);

  constructor(private http: HttpClient) {

  }

  ngOnInit(): void {
    this.getUsers();
    this.getGames();
    this.getGameInformation(1);
  }

  createAccount() {
    const name = 'user 1';
    const password = 'pw';

    this.http.post('/api/create-account', {
      name,
      password
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
