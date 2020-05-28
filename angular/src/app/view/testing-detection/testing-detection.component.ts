import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from 'src/app/service/authentication/authentication.service';
import { Batch } from 'src/app/model/batch/batch';
import { LocationService } from 'src/app/service/location/location.service';
import { LocationVO } from 'src/app/model/location/location-vo';
import { BatchSearchCriteria } from 'src/app/model/batch/batch-search-criteria';
import { RedcapDataService } from 'src/app/service/data/redcap-data.service';
import { InstrumentBatch } from 'src/app/model/batch/instrument-batch';
import { InstrumentList } from 'src/app/model/instrument/instrument-list';
import { Instrument } from 'src/app/model/instrument/instrument';
import { SpecimenService } from 'src/app/service/specimen/specimen.service';
import { UserDetails } from 'src/app/model/user/user-details';
import { FormControl, Validators } from '@angular/forms';

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
  instruments: Instrument[];
  barcode = '';
  labControl = new FormControl('', Validators.required);
  instrumentControl = new FormControl('', Validators.required);

  constructor(private router: Router, 
              private authService: AuthenticationService,
              private locationService: LocationService,
              private redcaDataService: RedcapDataService,
              private specimenBarcode: SpecimenService) {
                
    this.detectionBatch = new Batch();
    this.detectionBatch.detectionBatch1 = new InstrumentBatch();
    this.detectionBatch.detectionBatch2 = new InstrumentBatch();
    this.detectionBatch.detectionBatch3 = new InstrumentBatch();
    this.detectionBatch.detectionBatch4 = new InstrumentBatch();
    this.detectionBatch.lab = new LocationVO();
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

  saveDetectionBatch() {
    
    if(!this.detectionBatch.detectionPersonnel || this.detectionBatch.detectionPersonnel.length == 0) {
      this.authService.getLoggeInUser().subscribe( res => {
        this.detectionBatch.detectionPersonnel = res.username;
      });
    }

    this.detectionBatch.lab = this.locations.find(loc => loc.code == this.labControl.value)
    this.detectionBatch.detectionBatch1.instrument = this.instruments.find(loc => loc.code == this.instrumentControl.value)
  }

  newDetectionBatch() {
    this.detectionBatch = new Batch();
    this.labControl.setValue(this.detectionBatch.lab.code);
    this.instrumentControl.setValue(this.detectionBatch.detectionBatch1.instrument.code);
  }

  now() {
    this.detectionBatch.detectionDateTime = new Date();
    if(!this.detectionBatch.detectionPersonnel || this.detectionBatch.detectionPersonnel.length == 0) {
      
      this.authService.getLoggeInUser().subscribe( res => {
        this.detectionBatch.detectionPersonnel = res.username;
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
    
    this.detectionBatch = batch;
    this.labControl.setValue(batch.lab.code);
    this.instrumentControl.setValue(batch.detectionBatch1.instrument.code);
  }

  addSpecimen() {
    if(this.detectionBatch.detectionStatus != 'Complete') {
      if(!this.detectionBatch.detectionBatch1.batchItems.find(item => item.specimen_barcode == this.barcode) && 
          this.detectionBatch.detectionBatch1.batchItems.length <= 96) {

        this.specimenBarcode.findSpecimenByBarcode(this.barcode).subscribe(result => {
          this.detectionBatch.detectionBatch1.batchItems.push(result);
          this.detectionBatch.detectionBatch1.instrumentBatchSize = this.detectionBatch.detectionBatch1.batchItems.length;
        });
      }
    }

    this.barcode = '';
  }
}
