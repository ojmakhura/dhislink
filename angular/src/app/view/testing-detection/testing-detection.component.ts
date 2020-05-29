import { Component, OnInit, ViewChild } from '@angular/core';
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
import { MatTableDataSource } from '@angular/material/table';
import { Specimen } from 'src/app/model/specimen/specimen';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';

@Component({
  selector: 'app-testing-detection',
  templateUrl: './testing-detection.component.html',
  styleUrls: ['./testing-detection.component.css']
})
export class TestingDetectionComponent implements OnInit {

  batch: Batch;
  batches: MatTableDataSource<Batch>;
  specimen: MatTableDataSource<Specimen>;
  locations: LocationVO[];
  searchCriteria: BatchSearchCriteria;
  selectedIndex: number = 0;
  instruments: Instrument[];
  barcode = '';
  labControl = new FormControl('', Validators.required);
  instrumentControl = new FormControl('', Validators.required);

  searchColumns: string[] = [' ', 'batchId', 'resultingPersonnel', 'resultingDateTime', 'resultingStatus'];
  specimenColumns: string[] = ['specimen_barcode', 'patient_first_name', 'patient_surname', 'identity_no'];

  @ViewChild('BatchesPaginator', {static: true}) batchesPaginator: MatPaginator;
  @ViewChild('BatchSort', {static: true}) batchSort: MatSort;

  @ViewChild('SpecimenPaginator', {static: true}) specimenPaginator: MatPaginator;

  constructor(private router: Router, 
              private authService: AuthenticationService,
              private locationService: LocationService,
              private redcaDataService: RedcapDataService,
              private specimenBarcode: SpecimenService) {
                
    this.batch = new Batch();
    this.batch.lab = new LocationVO();
    this.batches = new MatTableDataSource<Batch>();
    this.batches.paginator = this.batchesPaginator;
    this.batches.sort = this.batchSort;

    this.specimen = new MatTableDataSource<Specimen>();
    this.specimen.paginator = this.specimenPaginator;

    this.searchCriteria = new BatchSearchCriteria();
    this.instruments = InstrumentList.allIntruments();
    
    this.locationService.findAll().subscribe(results => {
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
    
    if(!this.batch.detectionPersonnel || this.batch.detectionPersonnel.length == 0) {
      this.authService.getLoggeInUser().subscribe( res => {
        this.batch.detectionPersonnel = res.username;
      });
    }

    this.batch.lab = this.locations.find(loc => loc.code == this.labControl.value)
    this.batch.instrument = this.instruments.find(loc => loc.code == this.instrumentControl.value)
  }

  newDetectionBatch() {
    this.batch = new Batch();
    this.specimen.data = this.batch.batchItems;
    this.labControl.setValue(this.batch.lab.code);
    this.instrumentControl.setValue(this.batch.instrument.code);
  }

  now() {
    this.batch.detectionDateTime = new Date();
    if(!this.batch.detectionPersonnel || this.batch.detectionPersonnel.length == 0) {
      
      this.authService.getLoggeInUser().subscribe( res => {
        this.batch.detectionPersonnel = res.username;
      });
    }
  }

  searchBatches() {
    this.redcaDataService.search(this.searchCriteria).subscribe(results => {      
      this.batches.data = results;
      this.searchCriteria = new BatchSearchCriteria();
    });
  }

  clearSearch() {
    this.batches.data = [];
  }

  editBatch(batch: Batch) {
    
    this.batch = batch;
    this.specimen.data = this.batch.batchItems;
    this.labControl.setValue(batch.lab.code);
    this.instrumentControl.setValue(batch.instrument.code);
  }

  addSpecimen() {
    if(this.batch.detectionStatus != 'Complete') {
      if(!this.batch.batchItems.find(item => item.specimen_barcode == this.barcode) && 
          this.batch.batchItems.length <= 96) {

        this.specimenBarcode.findSpecimenByBarcode(this.barcode).subscribe(result => {
          this.batch.batchItems.push(result);
          this.specimen.data  = this.batch.batchItems;
          this.batch.instrumentBatchSize = this.batch.batchItems.length;
        });
      }
    }

    this.barcode = '';
  }

  fetchBatchData() {
    console.log("Fetching data");
    if(!this.batch.detectionStatus) {
      this.redcaDataService.fetchExtractionSpecimen(this.batch.batchId).subscribe(results => {
        console.log(results)
        this.batch.batchItems = results;
        this.specimen.data  = this.batch.batchItems;
      });
    }
  }
}
