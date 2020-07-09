import { Component, OnInit, Injector, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from 'src/app/service/authentication/authentication.service';
import { LocationService } from 'src/app/service/location/location.service';
import { RedcapDataService } from 'src/app/service/data/redcap-data.service';
import { SpecimenService } from 'src/app/service/specimen/specimen.service';
import { RxFormBuilder } from '@rxweb/reactive-form-validators';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { LocationVO } from 'src/app/model/location/location-vo';
import { Instrument } from 'src/app/model/instrument/instrument';
import { FormControl, FormGroup, FormArray } from '@angular/forms';
import { BatchSearchCriteria } from 'src/app/model/batch/batch-search-criteria';
import { Batch } from 'src/app/model/batch/batch';
import { InstrumentList } from 'src/app/model/instrument/instrument-list';
import { FORM_DATA, CURRENT_ROUTE } from 'src/app/helpers/dhis-link-constants';
import { formatDate } from '@angular/common';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { AuthenticationResponse } from 'src/app/model/authentication/authentication-response';
import { MatTableDataSource } from '@angular/material/table';

@Component({
  selector: 'app-batch',
  template: ''
})
export abstract class BatchComponent implements OnInit {

  protected router: Router;
  protected authService: AuthenticationService;
  protected locationService: LocationService;
  protected redcaDataService: RedcapDataService;
  protected specimenService: SpecimenService;
  protected formBuilder: RxFormBuilder;
  locations: LocationVO[];
  instruments: Instrument[];
  loading = false;
  pulling = false;
  searchCriteria: BatchSearchCriteria;
  batches: MatTableDataSource<Batch>;
  selectedTab = 0;
  page = '';

  batchForm: FormGroup;
  searchForm: FormGroup;
  batchesForm: FormGroup;

  @ViewChild('BatchesPaginator', { static: true }) batchesPaginator: MatPaginator;
  @ViewChild('BatchSort', { static: true }) batchSort: MatSort;

  @ViewChild('SpecimenPaginator', { static: true }) specimenPaginator: MatPaginator;

  constructor(injector: Injector) {
    this.router = injector.get(Router);
    this.authService = injector.get(AuthenticationService);
    this.locationService = injector.get(LocationService);
    this.redcaDataService = injector.get(RedcapDataService);
    this.specimenService = injector.get(SpecimenService);
    this.formBuilder = injector.get(RxFormBuilder);
  }

  abstract beforeOnInit();

  ngOnInit(): void {
    this.beforeOnInit();

    this.locationService.findAll().subscribe(results => {
      this.locations = results;
    }, error => {
      console.log(error);
      this.authService.logout();
    });

    this.batchForm = this.formBuilder.group(new Batch());
    this.searchForm = this.formBuilder.group(new BatchSearchCriteria());
    this.batches = new MatTableDataSource<Batch>();
    this.searchCriteria = new BatchSearchCriteria();
    this.instruments = InstrumentList.allIntruments();

    if (localStorage.getItem(FORM_DATA)) {
      const batch = JSON.parse(localStorage.getItem(FORM_DATA));
      this.editBatch(batch);
      localStorage.removeItem(FORM_DATA);
    }

    window.localStorage.setItem(CURRENT_ROUTE, '/' + this.page);
    this.afterOnInit();
  }

  abstract afterOnInit();


  getItemControl(name): FormControl {
    return this.batchForm.get(name) as FormControl;
  }

  getGroupControl(name): FormGroup {
    return this.batchForm.get(name) as FormGroup;
  }

  getArrayControl(name): FormArray {
    return this.batchForm.get(name) as FormArray;
  }

  get batchItems(): FormArray {
    return this.getArrayControl('batchItems');
  }

  searchBatches() {
    this.loading = true;
    this.searchCriteria.includeSpecimen = true;
    this.redcaDataService.search(this.searchCriteria).pipe().subscribe(results => {
      //this.batchesForm = this.formBuilder.array([results]);
      this.batches.data = results;
      this.searchCriteria = new BatchSearchCriteria();
      this.loading = false;
    });
  }

  clearSearch() {
    this.batches.data = [];

  }

  now() {

    this.getItemControl(this.page + 'DateTime').patchValue(formatDate(new Date(), 'yyyy-MM-ddTHH:mm', 'en-US'));
    const cnt = this.getItemControl(this.page + 'Personnel');
    cnt.patchValue(this.authService.getCurrentUser());
  }

  editBatch(batch: Batch) {
    batch.detectionSize = batch.batchItems.length;
    this.batchForm = this.formBuilder.group(batch);
    this.selectedTab = 0;
  }

  abstract preSaveBatch(): boolean;

  saveBatch() {

    this.loading = true;
    if (!this.preSaveBatch()) {
      return;
    }

    if (this.batchForm.invalid) {
      alert('The batch cannot be saved. Please check the data.');
      return;
    }

    let pageValue = this.page;
    if (this.page === 'detection') {
      pageValue = 'testing_detection';
    }

    this.getItemControl('page').setValue(pageValue);
    this.getItemControl('projectId').setValue(345);

    this.redcaDataService.saveBatch(this.batchForm.value).pipe(catchError((error) => {
      this.router.navigate(['/login']);
      return of(new AuthenticationResponse());
    })).subscribe(
      data => {
        this.loading = false;
      }
    );

    this.postSaveBatch();
  }

  abstract postSaveBatch();

  newBatch() {
    this.editBatch(new Batch());
  }

  getItemGroup(name): FormGroup {
    return this.batchForm.get(name) as FormGroup;
  }

  get formInvalid() {
    return this.batchForm.invalid;
  }

  onLabSelectionChange(event) {

    if (event.value === '') {
      return;
    }

    const lab = this.locations.find(loc => loc.code === event.value);

    if (lab !== undefined) {
      this.getItemGroup('lab').setValue(lab);
    }

  }

  onInstrumentSelectionChange(event) {

    if (event.value === '') {
      return;
    }

    const instrument = this.instruments.find(inst => inst.code === event.value);

    if (instrument !== undefined) {
      this.getItemGroup('instrument').setValue(instrument);
    }
  }

  pullSpecimenInfo() {
    this.pulling = true;
    const batch = this.batchForm.value;
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

  fetchBatchData() {
    const batch = this.batchForm.value;
    this.searchCriteria.batchId = batch.batchId;
    this.searchCriteria.includeSpecimen = true;
    this.searchCriteria.lab = null;
    this.searchCriteria.specimenBarcode = null;

    if (!batch.detectionStatus) {
      this.redcaDataService.search(this.searchCriteria).subscribe(results => {
        if (results.length === 1) {

          if (results[0].instrument === null) {
            results[0].instrument = new Instrument('', '');
          }

          if (results[0].lab === null) {
            results[0].lab = new LocationVO();
          }

          if (results[0].batchItems === null ) {
            results[0].batchItems = [];
          }

          this.editBatch(results[0]);
        } else {
          this.redcaDataService.fetchExtractionSpecimen(this.getItemControl('batchId').value).subscribe(results => {

            batch.batchItems = results;
            for (let i = 0; i < results.length; i++) {
              results[i].position = this.specimenService.encodePosition(i);
            }

            batch.instrumentBatchSize = batch.batchItems.length;
            batch.detectionSize = batch.batchItems.length;
            this.editBatch(batch);
          });
        }
      });
    }

    this.searchCriteria = new BatchSearchCriteria();
  }

  tabChanged(event) {
    this.selectedTab = event;
  }

  /**
   * This publishes the verified results to DHIS2
   */
  publish() {
    this.loading = true;
    const batch = this.batchForm.value;

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
      this.batchForm.patchValue(batch);
      return data;
    }
    this.loading = false;
  }

  toResulting() {
    const batch = this.batchForm.value as Batch;

    if ( batch.detectionStatus !== '2' ) {
      alert('The batch is not complete and cannot be sent to resulting.');
      return;
    }

    localStorage.setItem(FORM_DATA, JSON.stringify(this.batchForm.value));
    this.router.navigate(['/resulting']);
  }

  toVerification() {
    const batch = this.batchForm.value as Batch;
    if ( batch.resultingStatus !== '2' ) {
      alert('The batch is not complete and cannot be sent to resulting.');
      return;
    }
    localStorage.setItem(FORM_DATA, JSON.stringify(this.batchForm.value));
    this.router.navigate(['/verification']);
  }

  toDetection() {
    localStorage.setItem(FORM_DATA, JSON.stringify(this.batchForm.value));
    this.router.navigate(['/detection']);
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
