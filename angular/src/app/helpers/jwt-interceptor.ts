import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthenticationService } from '../service/authentication/authentication.service';
import { Injectable } from '@angular/core';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
    constructor(private authenticationService: AuthenticationService) { }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        window.localStorage.setItem('isIntercept', 'true');
        let token = this.authenticationService.getToken();
        if (token) {
            
            request = request.clone({
                setHeaders: { Authorization: 'Bearer ' + token }
            });
        }

        return next.handle(request);
    }
}
