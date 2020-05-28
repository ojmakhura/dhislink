import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from 'src/app/service/authentication/authentication.service';
import { Batch } from 'src/app/model/batch/batch';
import { LocationVO } from 'src/app/model/location/location-vo';
import { BatchSearchCriteria } from 'src/app/model/batch/batch-search-criteria';
import { Instrument } from 'src/app/model/instrument/instrument';
import { LocationService } from 'src/app/service/location/location.service';
import { RedcapDataService } from 'src/app/service/data/redcap-data.service';
import { SpecimenService } from 'src/app/service/specimen/specimen.service';
import { InstrumentList } from 'src/app/model/instrument/instrument-list';

@Component({
  selector: 'app-verification',
  templateUrl: './verification.component.html',
  styleUrls: ['./verification.component.css']
})
export class VerificationComponent implements OnInit {

  batch: Batch;
  batches: Batch[];
  locations: LocationVO[];
  searchCriteria: BatchSearchCriteria;
  selectedIndex: number = 0;
  instruments: Instrument[];
  barcode = '';

  constructor(private router: Router, 
              private authService: AuthenticationService,
              private locationService: LocationService,
              private redcaDataService: RedcapDataService,
              private specimenBarcode: SpecimenService) {
                
    this.batch = new Batch();
    this.batch.lab = new LocationVO();
    this.batches = [];
    this.searchCriteria = new BatchSearchCriteria();
    this.instruments = InstrumentList.allIntruments();
    
    locationService.findAll().subscribe(results => {
      this.locations = results;
    });
  }

  ngOnInit(): void {
    let token = this.authService.getToken();
    
    if(this.authService.isTokenExpired(token)) {
      this.router.navigate(['/login']);
    }
  }

  saveResultingBatch() {
    
  }

  now() {
    this.batch.verificationDateTime = new Date();
    if(!this.batch.verificationPersonnel || this.batch.verificationPersonnel.length == 0) {
      
      this.authService.getLoggeInUser().subscribe( res => {
        this.batch.verificationPersonnel = res.username;
      });
    }
  }

  searchBatches() {
    this.redcaDataService.search(this.searchCriteria).subscribe(results => {      
      this.batches = results;
      this.searchCriteria = new BatchSearchCriteria();
    });
  }

  clearSearch() {
    this.batches = [];
  }

  editBatch(batch: Batch) {
    
    this.batch = batch;
  }

}
