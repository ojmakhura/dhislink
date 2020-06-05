import { Component, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService, CURRENT_ROUTE, FORM_DATA } from 'src/app/service/authentication/authentication.service';
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
import { NgForm, FormControl, Validators }   from '@angular/forms';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { AuthenticationResponse } from 'src/app/model/authentication/authentication-response';

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
  
  labControl = new FormControl('', Validators.required);
  instrumentControl = new FormControl('', Validators.required);
  searchColumns: string[] = [' ', 'batchId', 'resultingPersonnel', 'resultingDateTime', 'resultingStatus'];
  specimenColumns: string[] = ['position', 'specimen_barcode', 'patient_first_name', 'patient_surname', 'identity_no', 'covidRnaResults', 'testVerifyResults'];

  @ViewChild('BatchesPaginator', {static: true}) batchesPaginator: MatPaginator;
  @ViewChild('BatchSort', {static: true}) batchSort: MatSort;

  @ViewChild('SpecimenPaginator') specimenPaginator: MatPaginator;


  constructor(private router: Router, 
              private authService: AuthenticationService,
              private locationService: LocationService,
              private redcaDataService: RedcapDataService,
              private specimenService: SpecimenService) {
    
    this.batches = new MatTableDataSource<Batch>();
    this.specimen = new MatTableDataSource<Specimen>();    
    this.searchCriteria = new BatchSearchCriteria();
    this.instruments = InstrumentList.allIntruments();
    
    locationService.findAll().subscribe(results => {
      this.locations = results;
    });

    if(localStorage.getItem(FORM_DATA)) {
      let batch: Batch = JSON.parse(localStorage.getItem(FORM_DATA));
      this.editBatch(batch);
      localStorage.removeItem(FORM_DATA);
    } else {
      this.batch = new Batch();
      this.batch.lab = new LocationVO();
    }
  }

  ngOnInit(): void {
    let token = this.authService.getToken();
    let user = this.authService.getCurrentUser();
    //this.authService.getLoggeInUser();
    window.localStorage.setItem(CURRENT_ROUTE, '/verification'); 
    
    if(!user || this.authService.isTokenExpired(token)) {   
      this.router.navigate(['/login']);
    }
  }

  ngAfterViewInit() {
    
    //this.batches.paginator = this.batchesPaginator;
    //this.batches.sort = this.batchSort;
    //this.specimen.paginator = this.specimenPaginator;
  }

  publish() {

    if(!this.batch.authorisingPersonnel || this.batch.authorisingPersonnel.length == 0) {
      this.batch.authorisingDateTime = formatDate(new Date(), 'yyyy-MM-dd HH:mm', 'en-US');        
      this.batch.authorisingPersonnel = this.authService.getCurrentUser();
    }

    this.batch.publishResults = true;
    this.redcaDataService.saveBatch(this.batch).pipe(catchError((error) => {
      this.router.navigate(['/login']);
      return of(new AuthenticationResponse());
    })).subscribe();
  }

  saveVerificationBatch() {

    if(this.authService.getCurrentUser() === this.batch.resultingPersonnel) {
      alert('Cannot verify the results you entered.');
    } else {

      this.batch.page = 'verification';
      this.batch.projectId = 345;
      if(!this.batch.verificationPersonnel || this.batch.verificationPersonnel.length == 0) {
        this.now();
      }

      this.redcaDataService.saveBatch(this.batch).pipe(catchError((error) => {
        this.router.navigate(['/login']);
        return of(new AuthenticationResponse());
      })).subscribe();
    }
  }

  now() {
    this.batch.verificationDateTime = formatDate(new Date(), 'yyyy-MM-dd HH:mm', 'en-US');
    if(!this.batch.verificationPersonnel || this.batch.verificationPersonnel.length == 0) {
      
      this.batch.verificationPersonnel = this.authService.getCurrentUser();
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

  verified(): boolean {
    if(this.batch.verificationStatus === '2') {
      return false;
    }

    return true;
  }

  toResulting() {
    localStorage.setItem(FORM_DATA, JSON.stringify(this.batch));
    this.router.navigate(['/resulting']);
  }
}
