import { Component, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from 'src/app/service/authentication/authentication.service';
import { Batch } from 'src/app/model/batch/batch';
import { LocationVO } from 'src/app/model/location/location-vo';
import { BatchSearchCriteria } from 'src/app/model/batch/batch-search-criteria';
import { Instrument } from 'src/app/model/instrument/instrument';
import { FormControl, Validators, FormBuilder, FormGroup, FormArray } from '@angular/forms';
import { LocationService } from 'src/app/service/location/location.service';
import { RedcapDataService } from 'src/app/service/data/redcap-data.service';
import { SpecimenService } from 'src/app/service/specimen/specimen.service';
import { InstrumentBatch } from 'src/app/model/batch/instrument-batch';
import { InstrumentList } from 'src/app/model/instrument/instrument-list';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { Specimen } from 'src/app/model/specimen/specimen';
import { MatSort } from '@angular/material/sort';
import { formatDate } from '@angular/common';
import { FORM_DATA, CURRENT_ROUTE } from 'src/app/helpers/dhis-link-constants';
import { RxFormBuilder } from '@rxweb/reactive-form-validators';
import { IfStmt } from '@angular/compiler';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs';

@Component({
  selector: 'app-resulting',
  templateUrl: './resulting.component.html',
  styleUrls: ['./resulting.component.css']
})
export class ResultingComponent implements OnInit {

  batches: MatTableDataSource<Batch>;
  locations: LocationVO[];
  searchCriteria: BatchSearchCriteria;
  selectedIndex = 0;
  instruments: Instrument[];
  barcode = '';
  loading = false;
  pulling = false;
  searchColumns: string[] = [' ', 'batchId', 'resultingPersonnel', 'resultingDateTime', 'resultingStatus'];
  specimenColumns: string[] = ['position', 'specimen_barcode', 'patient_first_name', 'patient_surname', 'identity_no', 'testAssayResults'];

  resultingForm: FormGroup;

  @ViewChild('BatchesPaginator', {static: true}) batchesPaginator: MatPaginator;
  @ViewChild('BatchSort', {static: true}) batchSort: MatSort;

  @ViewChild('SpecimenPaginator', {static: true}) specimenPaginator: MatPaginator;

  constructor(private router: Router,
              private authService: AuthenticationService,
              private locationService: LocationService,
              private redcaDataService: RedcapDataService,
              private formBuilder: RxFormBuilder) {
  }

  ngOnInit(): void {

    this.locationService.findAll().subscribe(results => {
      this.locations = results;
    }, error => {
      this.authService.logout();
    });
    
    this.searchCriteria = new BatchSearchCriteria();
    this.batches = new MatTableDataSource<Batch>();

    this.resultingForm = this.formBuilder.group(new Batch());
    this.instruments = InstrumentList.allIntruments();
    const token = this.authService.getToken();
    const user = this.authService.getCurrentUser();

    if (localStorage.getItem(FORM_DATA)) {
      const batch = JSON.parse(localStorage.getItem(FORM_DATA));
      this.editBatch(batch);
      localStorage.removeItem(FORM_DATA);
    }

    window.localStorage.setItem(CURRENT_ROUTE, '/resulting');
    if (!user || this.authService.isTokenExpired(token)) {
      this.router.navigate(['/login']);
    }
  }

  disableFormInputs() {
    if (this.getItemControl('batchId')) {
      this.getItemControl('batchId').disable();
    }

    if (this.getItemControl('lab') ) {
      this.getItemControl('lab').disable();
    }

    if (this.getItemControl('resultingPersonnel')) {
      this.getItemControl('resultingPersonnel').disable();
    }

    if (this.getItemControl('resultingDateTime')) {
      this.getItemControl('resultingDateTime').disable();
    }

    if (this.getItemControl('instrument')) {
      this.getItemControl('instrument').disable();
    }

    if (this.getItemControl('instrumentBatchSize')) {
      this.getItemControl('instrumentBatchSize').disable();
    }
  }

  get resultingFormControls() {
    return this.resultingForm.controls;
  }

  saveResultingBatch() {

    if (this.resultingForm.status === 'INVALID') {
      alert('The batch cannot be saved. Please check the data.');
      return;
    }

    this.loading = true;
    this.getItemControl('assayBatchId').setValue(this.getItemControl('detectionBatchId').value);

    this.getItemControl('page').setValue('resulting');
    this.getItemControl('projectId').setValue(345);

    const cnt = this.getItemControl('resultingPersonnel');
    console.log(cnt);
    
    if (!cnt && cnt.value.length === 0) {
      this.now();
    }

    this.redcaDataService.saveBatch(this.resultingForm.value).pipe(catchError((error) => {
      this.router.navigate(['/login']);
      return of([]);
    })).subscribe( data => {
      this.loading = false;
    });

  }

  now() {
    this.getItemControl('resultingDateTime').patchValue(formatDate(new Date(), 'yyyy-MM-dd HH:mm', 'en-US'));
    const cnt = this.getItemControl('resultingPersonnel');
    cnt.patchValue(this.authService.getCurrentUser());
  }

  getItemControl(name): FormControl {
    return this.resultingForm.get(name) as FormControl;
  }

  searchBatches() {
    this.loading = true;
    this.searchCriteria.includeSpecimen = true;
    this.redcaDataService.search(this.searchCriteria).pipe().subscribe(results => {
      this.batches.data = results;
      this.searchCriteria = new BatchSearchCriteria();
      this.loading = false;
      console.log(results);
    });
  }

  clearSearch() {
    this.batches.data = [];

  }

  editBatch(batch: Batch) {
    batch.detectionSize = batch.batchItems.length;
    this.resultingForm = this.formBuilder.group(batch);
    console.log(this.resultingForm);
    console.log(this.resultingForm.value);
  }

  get batchItems(): FormArray {
    return this.resultingForm.get('batchItems') as FormArray;
  }

  toVerification() {
    const batch = this.resultingForm.value as Batch;
    if ( batch.resultingStatus !== '2' ) {
      alert('The batch is not complete and cannot be sent to resulting.');
      return;
    }
    localStorage.setItem(FORM_DATA, JSON.stringify(this.resultingForm.value));
    this.router.navigate(['/verification']);
  }

  toDetection() {
    localStorage.setItem(FORM_DATA, JSON.stringify(this.resultingForm.value));
    this.router.navigate(['/detection']);
  }

  pullSpecimenInfo() {
    this.pulling = true;
    const batch = this.resultingForm.value;
    this.redcaDataService.pullSpecimenInfo(batch.batchItems).subscribe(results => {
      this.searchCriteria.includeSpecimen = true;
      this.searchCriteria.batchId = batch.batchId;

      this.redcaDataService.search(this.searchCriteria).subscribe(batches => {
        this.searchCriteria = new BatchSearchCriteria();
        if (batches.length > 0) {
          this.editBatch(batches[0]);
        }
        this.searchCriteria = new BatchSearchCriteria();
      });

      this.pulling = false;
    });
  }
}
