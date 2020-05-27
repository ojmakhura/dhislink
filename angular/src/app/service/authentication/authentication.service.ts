import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthenticationResponse } from 'src/app/model/authentication/authentication-response';
import * as jwt_decode from 'jwt-decode';
import { UserDetails } from 'src/app/model/user/user-details';

export const TOKEN_NAME: string = 'jwt_token';
export const CURRENT_ROUTE: string = 'currentRoute';
export const CURRENT_USER: string = 'currentUser';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  private url= 'http://localhost:8080/ddpcontroller/auth';

  constructor(private http: HttpClient) { }

  login(loginPayload) : Observable<AuthenticationResponse> {

    return this.http.post<AuthenticationResponse>(this.url + '/signin', loginPayload);
  }

  getToken(): string {
    return localStorage.getItem(TOKEN_NAME);
  }

  setToken(token: string): void {
    localStorage.setItem(TOKEN_NAME, token);
  }

  getTokenExpirationDate(token: string): Date {
    const decoded = jwt_decode(token);

    if (decoded.exp === undefined) return null;

    const date = new Date(0); 
    date.setUTCSeconds(decoded.exp);
    return date;
  }

  isTokenExpired(token?: string): boolean {
    if(!token) token = this.getToken();
    if(!token) return true;

    this.http.get<UserDetails>(this.url + '/me').subscribe(user => {
      if(!user || !user.username) {
        return true;
      }
    });

    const date = this.getTokenExpirationDate(token);
    if(date === undefined) return false;
    return !(date.valueOf() > new Date().valueOf());
  }
}
