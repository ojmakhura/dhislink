import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { AuthenticationResponse } from 'src/app/model/authentication/authentication-response';
import * as jwt_decode from 'jwt-decode';
import { UserDetails } from 'src/app/model/user/user-details';
import { retry, catchError, map } from 'rxjs/operators';
import { Router } from '@angular/router';
import { async } from 'q';

export const TOKEN_NAME: string = 'JWT_TOKEN';
export const REFRESH_TOKEN: string = 'REFRESH_TOKEN';
export const FORM_DATA: string = 'FORM_DATA';
export const CURRENT_ROUTE: string = 'CURRENT_ROUTE';
export const CURRENT_USER: string = 'CURRENT_USER';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  private url= 'https://ehealth.ub.bw:8080/ddpcontroller/auth';
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
    
    if(!refreshPayload.accessToken 
        || !refreshPayload.refreshToken 
        || refreshPayload.refreshToken === 'undefined'
        || refreshPayload.refreshToken === 'null'
        || refreshPayload.refreshToken === null
        || refreshPayload.accessToken === 'null'
        || !refreshPayload.username
        || refreshPayload.username === 'undefined') {

      this.router.navigate(['/login']);
    }

    return this.http.post<AuthenticationResponse>(this.url + '/refresh', refreshPayload).pipe(catchError((error) => {
      this.router.navigate(['/login']);
      return  of(new AuthenticationResponse());
    }));
  }

  redirectToLogin() {
    
    return throwError('Could not refresh token.');
  }

  getToken(): string {
    return localStorage.getItem(TOKEN_NAME);
  }

  setAccessToken(token: string): void {
    localStorage.setItem(TOKEN_NAME, token);
  }

  getRefreshToken(): string {
    return localStorage.getItem(REFRESH_TOKEN);
  }

  setRefreshToken(refreshToken: string): void {
    localStorage.setItem(REFRESH_TOKEN, refreshToken);
  }

  setCurrentUser(username: string) {
    
    localStorage.setItem(CURRENT_USER, username);
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

    const date = this.getTokenExpirationDate(token);
    if(date === undefined) return false;
    return !(date.valueOf() > new Date().valueOf());
  }

  getLoggeInUser(): boolean {
    let loggedIn = false
    this.user = undefined;
    this.http.get<UserDetails>(this.url + '/me').pipe(catchError((error) => {
      this.router.navigate(['/login']);
      return  of(new UserDetails());
    })).subscribe(data => {   
      this.user = data;
      localStorage.setItem(CURRENT_USER, this.user.username);
      loggedIn = true;
    });

    if(!this.user || !this.user.username) {
      localStorage.removeItem(CURRENT_USER);
      loggedIn = false;
    }

    return loggedIn;
  }

  logout() {
    localStorage.removeItem(TOKEN_NAME);
    localStorage.removeItem(REFRESH_TOKEN);
    localStorage.removeItem(CURRENT_USER);
    localStorage.removeItem(CURRENT_ROUTE);
    localStorage.removeItem(FORM_DATA);
  }

  handleHttpError(error: HttpErrorResponse) {

  }
}