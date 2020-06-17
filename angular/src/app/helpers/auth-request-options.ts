import { Headers, Http, BaseRequestOptions } from '@angular/http';
import { Injectable } from '@angular/core';
import { TOKEN_NAME } from './dhis-link-constants';

const AUTH_HEADER_KEY = 'Authorization';
const AUTH_PREFIX = 'Bearer';

@Injectable()
export class AuthRequestOptions extends BaseRequestOptions {
  
  constructor() {
    super();
    
    const token = localStorage.getItem(TOKEN_NAME);
    if(token) {
      this.headers.append(AUTH_HEADER_KEY, `${AUTH_PREFIX} ${token}`);
    }
  }

}