import { Component, OnInit } from '@angular/core';
import {Router} from "@angular/router";
import { 
  AuthenticationService } from '../../service/authentication/authentication.service';
import { RedcapAuth } from '../../model/authentication/redcap-auth';
import { NgForm }   from '@angular/forms';
import { CURRENT_USER, TOKEN_NAME, REFRESH_TOKEN, CURRENT_ROUTE } from 'src/app/helpers/dhis-link-constants';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  invalidLogin: boolean = false;
  redcapAuth: RedcapAuth;
  hide = true;
  loading = false;

  constructor(private router: Router, private authService: AuthenticationService ) {
    this.redcapAuth = new RedcapAuth();
  }

  ngOnInit(): void {
  }

  getCurrentUser() : string {
    return window.localStorage.getItem(CURRENT_USER);
  }

  onSubmit() {
    
    this.loading = true;
    this.authService.login(this.redcapAuth).pipe().subscribe(data => {
      
      if(data.status === 200) {
        this.redcapAuth.username = data.username;
        window.localStorage.setItem(TOKEN_NAME, data.accessToken);
        window.localStorage.setItem(CURRENT_USER, data.username);
        window.localStorage.setItem(REFRESH_TOKEN, data.refreshToken);
        
        if(window.localStorage.getItem(CURRENT_ROUTE)) {
          this.router.navigate([window.localStorage.getItem(CURRENT_ROUTE)]);
        } else {
          this.router.navigate(['/detection']);
        }
        this.invalidLogin = false;
      }else {
        this.invalidLogin = true;
        alert("Login failed!");
      }
    }, error => {
      if(error.status === 500 || error.status === 401) {
        this.invalidLogin = true;
      } else {
        alert('Unknown error has occurred. Please contact the administrator.');
      }
      this.loading = false;
    });
  }

}
