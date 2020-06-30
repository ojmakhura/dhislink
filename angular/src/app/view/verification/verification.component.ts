import { Component, OnInit, ViewChild } from '@angular/core';
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
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { Specimen } from 'src/app/model/specimen/specimen';
import { formatDate } from '@angular/common';
import { NgForm, FormControl, Validators, FormGroup, FormArray }   from '@angular/forms';
import { catchError, first } from 'rxjs/operators';
import { of } from 'rxjs';
import { AuthenticationResponse } from 'src/app/model/authentication/authentication-response';
import { FORM_DATA, CURRENT_ROUTE } from 'src/app/helpers/dhis-link-constants';
import { error } from 'protractor';
import { RxFormBuilder } from '@rxweb/reactive-form-validators';

@Component({
  selector: 'app-verification',
  templateUrl: './verification.component.html',
  styleUrls: ['./verification.component.css']
})
export class VerificationComponent implements OnInit {

  batches: MatTableDataSource<Batch>;
  locations: LocationVO[];
  searchCriteria: BatchSearchCriteria;
  selectedIndex = 0;
  instruments: Instrument[];
  barcode = '';
  loading = false;
  labControl = new FormControl('', Validators.required);
  instrumentControl = new FormControl('', Validators.required);
  searchColumns: string[] = [' ', 'batchId', 'resultingPersonnel', 'resultingDateTime', 'resultingStatus'];
  specimenColumns: string[] = [
    'position',
    'specimen_barcode',
    'patient_first_name',
    'patient_surname',
    'identity_no',
    'covidRnaResults',
    'testVerifyResults'
  ];

  // ----------------------------
  verificationForm: FormGroup;

  @ViewChild('BatchesPaginator', {static: true}) batchesPaginator: MatPaginator;
  @ViewChild('BatchSort', {static: true}) batchSort: MatSort;

  @ViewChild('SpecimenPaginator') specimenPaginator: MatPaginator;


  constructor(private router: Router,
              private authService: AuthenticationService,
              private locationService: LocationService,
              private redcaDataService: RedcapDataService,
              private specimenService: SpecimenService,
              private formBuilder: RxFormBuilder) {

  }

  ngOnInit(): void {
    this.locationService.findAll().subscribe(results => {
      this.locations = results;
    }, error => {
      console.log(error);
      this.authService.logout();
    });
    const token = this.authService.getToken();
    const user = this.authService.getCurrentUser();
    this.authService.getLoggeInUser();
    window.localStorage.setItem(CURRENT_ROUTE, '/verification');

    if (!user || this.authService.isTokenExpired(token)) {
      this.authService.logout();
    }

    this.batches = new MatTableDataSource<Batch>();
    this.searchCriteria = new BatchSearchCriteria();
    this.instruments = InstrumentList.allIntruments();
    this.verificationForm = this.formBuilder.group(new Batch());

    if (localStorage.getItem(FORM_DATA)) {
      const batch = JSON.parse(localStorage.getItem(FORM_DATA));
      this.editBatch(batch);
      localStorage.removeItem(FORM_DATA);
    }
  }

  /**
   * This publishes the verified results to DHIS2
   */
  publish() {
    this.loading = true;
    const batch = this.verificationForm.value;

    if (batch.verificationStatus !== '2' &&
      !batch.authorisingPersonnel) {
        alert('Could not publish results. Either verification is not complete or authorising personel is not set.')
    } else {
      batch.publishResults = true;

      let data: any;
      this.redcaDataService.saveBatch(batch).pipe().subscribe(results => {
          data = results;
        }, error => {
          this.router.navigate(['/login']);
          return of(new AuthenticationResponse());
        }
      );
      this.verificationForm.patchValue(batch);
      return data;
    }
    this.loading = false;
  }

  authorise() {
    const batch = this.verificationForm.value;
    batch.authorisingDateTime = formatDate(new Date(), 'yyyy-MM-dd HH:mm', 'en-US');
    batch.authorisingPersonnel = this.authService.getCurrentUser();
    this.verificationForm.patchValue(batch);
  }

  getItemControl(name): FormControl {
    return this.verificationForm.get(name) as FormControl;
  }

  saveVerificationBatch() {

    this.loading = true;
    if (this.authService.getCurrentUser() === this.verificationForm.value.resultingPersonnel) {
      alert('Cannot verify the results you entered.');
    } else {

      this.getItemControl('page').setValue('verification');
      this.getItemControl('projectId').setValue(345);
      if (!this.verificationForm.value.verificationPersonnel || this.verificationForm.value.verificationPersonnel.length === 0) {
        this.now();
      }

      this.redcaDataService.saveBatch(this.verificationForm.value).pipe(catchError((error) => {
        this.router.navigate(['/login']);
        return of(new AuthenticationResponse());
      })).subscribe(
        data => {
          this.loading = false;
        }
      );
    }

  }

  now() {
    const batch = this.verificationForm.value;
    batch.verificationDateTime = formatDate(new Date(), 'yyyy-MM-dd HH:mm', 'en-US');
    if (!batch.verificationPersonnel || batch.verificationPersonnel.length === 0) {

      batch.verificationPersonnel = this.authService.getCurrentUser();
    }

    this.verificationForm.patchValue(batch);
  }

  searchBatches() {
    this.loading = true;
    this.searchCriteria.includeSpecimen = true;
    this.redcaDataService.search(this.searchCriteria).pipe().subscribe(results => {

      this.batches.data = results;
      this.searchCriteria = new BatchSearchCriteria();
      this.loading = false;
    });

  }

  clearSearch() {
    this.batches.data = [];
  }

  editBatch(batch: Batch) {

    this.labControl.setValue(batch.lab.code);
    this.instrumentControl.setValue(batch.instrument.code);
    this.verificationForm = this.formBuilder.group(batch);
  }

  verified(): boolean {
    const batch = this.verificationForm.value;
    if (batch.verificationStatus === '2' &&
      batch.authorisingPersonnel) {

      return false;
    }

    return true;
  }

  toResulting() {
    localStorage.setItem(FORM_DATA, JSON.stringify(this.verificationForm.value));
    this.router.navigate(['/resulting']);
  }

  get batchItems(): FormArray {
    return this.verificationForm.get('batchItems') as FormArray;
  }

  getResults(code) {
    if (code === '1') {
      return 'Positive';
    } else if (code === '2') {
      return 'Negative';
    } else if (code === '3') {
      return 'Inconclusive';
    } else if (code === '4') {
      return 'No Results';
    } else {
      return '';
    }
  }
}
