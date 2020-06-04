import { Component, OnInit } from '@angular/core';
import {Router} from "@angular/router";
import { 
  AuthenticationService, 
  TOKEN_NAME, 
  CURRENT_ROUTE,
  CURRENT_USER, 
  REFRESH_TOKEN} from '../../service/authentication/authentication.service';
import { RedcapAuth } from '../../model/authentication/redcap-auth';
import { NgForm }   from '@angular/forms';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  invalidLogin: boolean = false;
  redcapAuth: RedcapAuth;
  hide = true;

  constructor(private router: Router, private authService: AuthenticationService ) {
    this.redcapAuth = new RedcapAuth();
  }

  ngOnInit(): void {
  }

  getCurrentUser() : string {
    return window.localStorage.getItem(CURRENT_USER);
  }

  onSubmit() {
    
    this.authService.login(this.redcapAuth).subscribe(data => {
      
      console.log('**************************************');
      
      if(data.status === 200) {
        console.log(data);
        this.redcapAuth.username = data.username;
        window.localStorage.setItem(TOKEN_NAME, data.accessToken);
        window.localStorage.setItem(CURRENT_USER, data.username);
        window.localStorage.setItem(REFRESH_TOKEN, data.refreshToken);
        
        if(window.localStorage.getItem(CURRENT_ROUTE)) {
          this.router.navigate([window.localStorage.getItem(CURRENT_ROUTE)]);
        } else {
          this.router.navigate(['/detection']);
        }
      }else {
        this.invalidLogin = true;
        alert("Login failed!");
      }
    });
  }

}
