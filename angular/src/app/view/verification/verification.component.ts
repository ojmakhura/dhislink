import { Component, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService, CURRENT_ROUTE } from 'src/app/service/authentication/authentication.service';
import { Batch } from 'src/app/model/batch/batch';
import { LocationVO } from 'src/app/model/location/location-vo';
import { BatchSearchCriteria } from 'src/app/model/batch/batch-search-criteria';
import { Instrument } from 'src/app/model/instrument/instrument';
import { LocationService } from 'src/app/service/location/location.service';
import { RedcapDataService } from 'src/app/service/data/redcap-data.service';
import { SpecimenService } from 'src/app/service/specimen/specimen.service';
import { InstrumentList } from 'src/app/model/instrument/instrument-list';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { Specimen } from 'src/app/model/specimen/specimen';
import { formatDate } from '@angular/common';
import { NgForm }   from '@angular/forms';

@Component({
  selector: 'app-verification',
  templateUrl: './verification.component.html',
  styleUrls: ['./verification.component.css']
})
export class VerificationComponent implements OnInit {

  batch: Batch;
  batches: MatTableDataSource<Batch>;
  specimen: MatTableDataSource<Specimen>;
  locations: LocationVO[];
  searchCriteria: BatchSearchCriteria;
  selectedIndex: number = 0;
  instruments: Instrument[];
  barcode = '';
  searchColumns: string[] = [' ', 'batchId', 'resultingPersonnel', 'resultingDateTime', 'resultingStatus'];
  specimenColumns: string[] = ['position', 'specimen_barcode', 'patient_first_name', 'patient_surname', 'identity_no', 'covidRnaResults', 'testVerifyResults'];

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
    
    locationService.findAll().subscribe(results => {
      this.locations = results;
    });
  }

  ngOnInit(): void {
    let token = this.authService.getToken();
    window.localStorage.setItem(CURRENT_ROUTE, '/verification')

    if(!this.authService.getCurrentUser() ||this.authService.isTokenExpired(token)) {      
      this.router.navigate(['/login']);
    }
  }

  saveVerificationBatch() {
    
  }

  now() {
    this.batch.verificationDateTime = formatDate(new Date(), 'dd-MM-yyyy HH:mm:ss', 'en-US');
    if(!this.batch.verificationPersonnel || this.batch.verificationPersonnel.length == 0) {
      
      this.batch.verificationPersonnel = this.authService.getCurrentUser();
    }
  }

  searchBatches() {
    this.redcaDataService.search(this.searchCriteria).subscribe(results => {      
      console.log(results)
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
  }

}
