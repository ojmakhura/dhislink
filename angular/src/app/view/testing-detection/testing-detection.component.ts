import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from 'src/app/service/authentication/authentication.service';
import { Batch } from 'src/app/model/batch/batch';
import { LocationService } from 'src/app/service/location/location.service';
import { LocationVO } from 'src/app/model/location/location-vo';
import { BatchSearchCriteria } from 'src/app/model/batch/batch-search-criteria';

@Component({
  selector: 'app-testing-detection',
  templateUrl: './testing-detection.component.html',
  styleUrls: ['./testing-detection.component.css']
})
export class TestingDetectionComponent implements OnInit {

  detectionBatch: Batch;
  batches: Batch[];
  locations: LocationVO[];
  searchCriteria: BatchSearchCriteria;
  selectedIndex: number = 0;

  constructor(private router: Router, 
              private authService: AuthenticationService,
              private locationService: LocationService) {
                
    this.detectionBatch = new Batch();
    this.batches = [];
    this.searchCriteria = new BatchSearchCriteria();
    locationService.findAll().subscribe(results => {
      this.locations = results;
    });
  }

  ngOnInit(): void {
    if(this.authService.isTokenExpired(this.authService.getToken())) {
      this.router.navigate(['/login']);
    }
  }

  saveDetectionBatch() {

  }

  newDetectionBatch() {
    this.detectionBatch = new Batch();
  }

  now() {
    this.detectionBatch.detectionDateTime = new Date();
  }

  searchBatches() {
    console.log(this.searchCriteria);
    
  }

  clearSearch() {

  }

  editBatch(batchId: string) {

  }

}
