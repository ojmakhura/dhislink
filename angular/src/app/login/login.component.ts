import { Component, OnInit } from '@angular/core';
import {FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import { AuthenticationService } from '../service/authentication/authentication.service';
import { RedcapAuth } from '../model/authentication/redcap-auth';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  invalidLogin: boolean = false;
  redcapAuth: RedcapAuth;
  hide = true;

  constructor(private router: Router, 
              private authService: AuthenticationService
               ) {
    this.redcapAuth = new RedcapAuth();
  }

  ngOnInit(): void {
    window.localStorage.removeItem('jwt_token');
  }

  onSubmit() {
    
    this.authService.login(this.redcapAuth).subscribe(data => {
      debugger;
      if(data.status === 200) {
        window.localStorage.setItem('jwt_token', data.accessToken);
        ///this.router.navigate(['list-user']);
      }else {
        this.invalidLogin = true;
        alert("Login failed!");
      }
    });
  }

}
