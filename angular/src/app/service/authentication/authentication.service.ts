import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { AuthenticationResponse } from 'src/app/model/authentication/authentication-response';
import * as jwt_decode from 'jwt-decode';
import { UserDetails } from 'src/app/model/user/user-details';
import { retry, catchError } from 'rxjs/operators';
import { Router } from '@angular/router';

export const TOKEN_NAME: string = 'jwt_token';
export const REFRESH_TOKEN: string = 'REFRESH_TOKEN';
export const CURRENT_ROUTE: string = 'currentRoute';
export const CURRENT_USER: string = 'currentUser';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  private url= 'http://localhost:8080/ddpcontroller/auth';
  user: UserDetails;

  constructor (private router: Router,private http: HttpClient) {
    this.user = new UserDetails();
  }

  login(loginPayload) : Observable<AuthenticationResponse> {

    return this.http.post<AuthenticationResponse>(this.url + '/signin', loginPayload);
  }

  refreshToken(): Observable<AuthenticationResponse> {
    let refreshPayload = new AuthenticationResponse();
    refreshPayload.accessToken = this.getToken();
    refreshPayload.refreshToken = this.getRefreshToken();
    refreshPayload.username = this.getCurrentUser();

    return this.http.post<AuthenticationResponse>(this.url + '/refresh', refreshPayload).pipe(
      retry(1),
      catchError(this.redirectToLogin)
    );
  }

  redirectToLogin() {
    
    return throwError('Could not refresh token.');
  }

  getToken(): string {
    return localStorage.getItem(TOKEN_NAME);
  }

  setToken(token: string): void {
    localStorage.setItem(TOKEN_NAME, token);
  }

  getRefreshToken(): string {
    return localStorage.getItem(REFRESH_TOKEN);
  }

  setRefreshToken(refreshToken: string): void {
    localStorage.setItem(REFRESH_TOKEN, refreshToken);
  }

  setCurrentUser() {
    localStorage.setItem(CURRENT_USER, this.getToken());
  }

  getCurrentUser(): string {
    return localStorage.getItem(CURRENT_USER);
  }

  getCurrentRoute(): string {
    return localStorage.getItem(CURRENT_ROUTE);
  }

  getTokenExpirationDate(token: string): Date {
    const decoded = jwt_decode(token);

    if (decoded.exp === undefined) return null;

    const date = new Date(); 
    date.setUTCSeconds(decoded.exp);
    return date;
  }

  isTokenExpired(token?: string): boolean {
    if(!token) token = this.getToken();
    if(!token) return true;

    this.http.get<UserDetails>(this.url + '/me').subscribe(user => {
      if(!user || !user.username) {
        return true;
      } else {
        return false;
      }
    });

    const date = this.getTokenExpirationDate(token);
    if(date === undefined) return false;
    return !(date.valueOf() > new Date().valueOf());
  }

  getLoggeInUser(): Observable<UserDetails> {

    return this.http.get<UserDetails>(this.url + '/me');
  }

  logout() {
    localStorage.removeItem(TOKEN_NAME);
    localStorage.removeItem(REFRESH_TOKEN);
    localStorage.removeItem(CURRENT_USER);
    localStorage.removeItem(CURRENT_ROUTE);
  }
}
