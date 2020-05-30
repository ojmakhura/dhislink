import { ErrorHandler, Injectable, Injector } from '@angular/core';
import { Router } from '@angular/router';

@Injectable()
export class AuthErrorHandler implements ErrorHandler {

  constructor(private injector: Injector) { }

  handleError(error) {
    const router = this.injector.get(Router);
    console.log(error);
    console.log(error.status)
    
    if (error.status === 401 || error.status === 403 || error.status === 500) {
      router.navigate(['/login']);
    }
    
    throw error;
  }
}