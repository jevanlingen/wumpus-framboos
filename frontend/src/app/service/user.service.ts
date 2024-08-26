import { Injectable } from '@angular/core';
import { ReplaySubject, Subscription } from 'rxjs';
import { User } from '../../model/user';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private _users = new ReplaySubject<Array<User>>();
  private userTimeout?: any;
  private userSub?: Subscription;

  users$ = this._users.asObservable();

  constructor(private http: HttpClient) {
    this.refreshUsers();
  }

  private refreshUsers() {
    clearTimeout(this.userTimeout);
    this.userSub?.unsubscribe();

    this.userSub = this.http.get<Array<User>>(`/api/users`)
      .subscribe(users => {
        this._users.next(users);
        this.userTimeout = setTimeout(() => {
          this.refreshUsers();
        }, 5000);
      });
  }
}
