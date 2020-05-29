import { CanActivate, Router } from '@angular/router';
import { AuthenticationService } from '../service/authentication/authentication.service';
import { Injectable } from '@angular/core';

@Injectable()
export class AuthGuard implements CanActivate {
    constructor(
        private router: Router, 
        private authService: AuthenticationService) {}
        
    canActivate(route: import("@angular/router").ActivatedRouteSnapshot, state: import("@angular/router").RouterStateSnapshot): boolean | import("@angular/router").UrlTree | import("rxjs").Observable<boolean | import("@angular/router").UrlTree> | Promise<boolean | import("@angular/router").UrlTree> {
        if (!this.authService.isTokenExpired()) {
            return true;
        }
      
        this.router.navigate(['/login']);
        return false;
        
    }
}
