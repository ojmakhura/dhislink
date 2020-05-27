import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from 'src/app/service/authentication/authentication.service';
import { Batch } from 'src/app/model/batch/batch';

@Component({
  selector: 'app-resulting',
  templateUrl: './resulting.component.html',
  styleUrls: ['./resulting.component.css']
})
export class ResultingComponent implements OnInit {

  resultingBatch: Batch;

  constructor(private router: Router, 
    private authService: AuthenticationService) {

      this.resultingBatch = new Batch();
  }

  ngOnInit(): void {
    if(this.authService.isTokenExpired(this.authService.getToken())) {
      
      this.router.navigate(['/login']);
    }
  }

}
