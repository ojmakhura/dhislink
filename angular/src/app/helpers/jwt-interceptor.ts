import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, BehaviorSubject, Subject } from 'rxjs';
import { AuthenticationService } from '../service/authentication/authentication.service';
import { Injectable } from '@angular/core';
import { catchError, filter, take, switchMap } from 'rxjs/operators'; 

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
    private refreshTokenInProgress = false;
    private refreshTokenSubject: Subject<any> = new BehaviorSubject<any>(null);

    constructor(public authService: AuthenticationService) { }
    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

        if (request.url.indexOf('refresh') !== -1) {
            return next.handle(request);
        }

        const accessExpired = this.authService.isTokenExpired();
        if (accessExpired) {
            return next.handle(request);
        }
        if (accessExpired) {
            
            if (!this.refreshTokenInProgress) {
                this.refreshTokenInProgress = true;
                this.refreshTokenSubject.next(null);
                return this.authService.refreshToken().pipe(
                    switchMap((authResponse) => {

                        this.authService.setAccessToken(authResponse.accessToken);
                        this.authService.setRefreshToken(authResponse.refreshToken);
                        this.refreshTokenInProgress = false;
                        this.refreshTokenSubject.next(authResponse.refreshToken);
                        return next.handle(this.injectToken(request));
                    }),
                );
            } else {
                return this.refreshTokenSubject.pipe(
                    filter(result => result !== null),
                    take(1),
                    switchMap((res) => {
                        return next.handle(this.injectToken(request))
                    })
                );
            }
        }

        if (!accessExpired) {
            return next.handle(this.injectToken(request));
        }
    }

    injectToken(request: HttpRequest<any>) {
        const token = this.authService.getToken();
        return request.clone({
            setHeaders: {
                Authorization: `Bearer ${token}`
            }
        });
    }
}