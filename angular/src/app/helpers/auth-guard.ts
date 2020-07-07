import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree } from '@angular/router';
import { AuthenticationService } from '../service/authentication/authentication.service';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class AuthGuard implements CanActivate {
    constructor(
        private router: Router,
        private authService: AuthenticationService) {}


    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot):
                        boolean | UrlTree | Observable<boolean | UrlTree> | Promise<boolean | UrlTree> {

        if (!this.authService.isLoggedIn()) {
            //window.alert('Access denied. Please login!');
            this.router.navigate(['/login']);
            return false;
        } else {
            return true;
        }
    }
}
