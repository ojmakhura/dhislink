import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { AuthenticationService, REFRESH_TOKEN } from '../service/authentication/authentication.service';
import { Injectable } from '@angular/core';
import { catchError, filter, take, switchMap } from 'rxjs/operators'; 

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
    constructor(private authenticationService: AuthenticationService) { }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        
        window.localStorage.setItem('isIntercept', 'true');
        let username = this.authenticationService.getCurrentUser();

        let token = this.authenticationService.getToken();
        
        if (token) {            
            request = this.addToken(request, token);
        }

        return next.handle(request).pipe(catchError(error => {
            if (error instanceof HttpErrorResponse && error.status === 401) {
                console.log(error);
                
                return this.handle401Error(request, next);
            } else {
                console.log('other');
                
                return throwError(error);
            }           
          }));
    }

    private addToken(request: HttpRequest<any>, token: string) {
        return request.clone({
            setHeaders: {
                'Authorization': `Bearer ${token}`
            }
        });
    }

    private isRefreshing = false;
    private refreshTokenSubject: BehaviorSubject<any> = new BehaviorSubject<any>(null);

    private handle401Error(request: HttpRequest<any>, next: HttpHandler) {
        if (!this.isRefreshing) {
            this.isRefreshing = true;
            this.refreshTokenSubject.next(null);
            
            return this.authenticationService.refreshToken().pipe(
                switchMap((token: any) => {
                    this.isRefreshing = false;
                    this.refreshTokenSubject.next(token.jwt);
                    return next.handle(this.addToken(request, token.jwt));
                })
            );

        } else {
            return this.refreshTokenSubject.pipe(
                filter(token => token != null),
                take(1),
                switchMap(jwt => {                     
                    return next.handle(this.addToken(request, jwt));
            }));
        }
    }
}
