import { Component, OnInit } from '@angular/core';
import {Router} from '@angular/router';
import {
  AuthenticationService } from '../../service/authentication/authentication.service';
import { RedcapAuth } from '../../model/authentication/redcap-auth';
import { NgForm, FormGroup } from '@angular/forms';
import { CURRENT_USER, TOKEN_NAME, REFRESH_TOKEN, CURRENT_ROUTE } from 'src/app/helpers/dhis-link-constants';
import { RxFormBuilder, async } from '@rxweb/reactive-form-validators';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  invalidLogin = false;
  hide = true;
  loading = false;
  loginForm: FormGroup;

  constructor(private router: Router,
              private authService: AuthenticationService,
              private formBuilder: RxFormBuilder ) {
    if (authService.isLoggedIn) {
      this.router.navigate(['/detection']);
    }
  }

  ngOnInit(): void {
    this.loginForm = this.formBuilder.formGroup(new RedcapAuth());
  }

  getCurrentUser(): string {
    return window.localStorage.getItem(CURRENT_USER);
  }

  onSubmit() {
    this.loading = true;
    this.authService.login(this.loginForm.value).subscribe(data => {

      if (data.status === 200) {

        window.localStorage.setItem(TOKEN_NAME, data.accessToken);
        window.localStorage.setItem(CURRENT_USER, data.username);
        window.localStorage.setItem(REFRESH_TOKEN, data.refreshToken);

        if (window.localStorage.getItem(CURRENT_ROUTE)) {
          this.router.navigate([window.localStorage.getItem(CURRENT_ROUTE)]);
        } else {

          this.router.navigate(['detection']);
        }
        this.invalidLogin = false;
      } else {
        this.invalidLogin = true;
        alert('Login failed!');
      }
    }, error => {
      if (error.status === 500 || error.status === 401) {
        this.invalidLogin = true;
      } else {
        console.log(error);
        alert('Unknown error has occurred. Please contact the administrator.\n' + error);
      }
      this.loading = false;
    });
  }

}
