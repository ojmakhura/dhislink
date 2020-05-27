import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from 'src/app/service/authentication/authentication.service';

@Component({
  selector: 'app-verification',
  templateUrl: './verification.component.html',
  styleUrls: ['./verification.component.css']
})
export class VerificationComponent implements OnInit {

  constructor(private router: Router, 
    private authService: AuthenticationService) { }

  ngOnInit(): void {
    if(this.authService.isTokenExpired(this.authService.getToken())) {
      
      this.router.navigate(['/login']);
    }
  }

}
