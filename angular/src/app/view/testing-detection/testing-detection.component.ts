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
import { FormControl, Validators, FormGroup, FormArray } from '@angular/forms';
import { MatTableDataSource } from '@angular/material/table';
import { Specimen } from 'src/app/model/specimen/specimen';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { formatDate } from '@angular/common';
import { NgForm } from '@angular/forms';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { AuthenticationResponse } from 'src/app/model/authentication/authentication-response';
import { FORM_DATA, CURRENT_ROUTE } from 'src/app/helpers/dhis-link-constants';
import { Patient } from 'src/app/model/patient/patient';
import { MatDialog } from '@angular/material/dialog';
import { DialogBoxComponent } from '../dialog-box/dialog-box.component';
import { RxFormBuilder } from '@rxweb/reactive-form-validators';

@Component({
  selector: 'app-testing-detection',
  templateUrl: './testing-detection.component.html',
  styleUrls: ['./testing-detection.component.css']
})
export class TestingDetectionComponent implements OnInit {

  batches: MatTableDataSource<Batch>;
  locations: LocationVO[];
  searchCriteria: BatchSearchCriteria;
  selectedIndex = 0;
  instruments: Instrument[];
  barcode = '';
  loading = false;
  adding = false;
  removing = false;
  pulling = false;
  detectionForm: FormGroup;

  searchColumns: string[] = [' ', 'batchId', 'detectionPersonnel', 'detectionDateTime', 'detectionStatus'];
  specimenColumns: string[] = [' ', 'position', 'specimen_barcode', 'patient_first_name', 'patient_surname', 'identity_no'];

  @ViewChild('BatchesPaginator', {static: true}) batchesPaginator: MatPaginator;
  @ViewChild('BatchSort', {static: true}) batchSort: MatSort;

  @ViewChild('SpecimenPaginator', {static: true}) specimenPaginator: MatPaginator;

  constructor(private router: Router,
              private authService: AuthenticationService,
              private locationService: LocationService,
              private redcaDataService: RedcapDataService,
              private specimenService: SpecimenService,
              public dialog: MatDialog,
              private formBuilder: RxFormBuilder) {

  }

  ngOnInit(): void {

    this.authService.isLoggedIn.subscribe(
      data => {
        if(!data) {
          this.router.navigate(['/login']);
        }
      }
    );

    this.locationService.findAll().subscribe(results => {
      this.locations = results;
    }, error => {
      console.log(error);
      this.authService.logout();
      this.router.navigate(['/login']);
    });
    this.instruments = InstrumentList.allIntruments();

    this.batches = new MatTableDataSource<Batch>();
    this.searchCriteria = new BatchSearchCriteria();
    this.editBatch(new Batch());

    if (localStorage.getItem(FORM_DATA)) {
      const batch = JSON.parse(localStorage.getItem(FORM_DATA));
      this.editBatch(batch);
      localStorage.removeItem(FORM_DATA);
    }

    const token = this.authService.getToken();
    const user = this.authService.getCurrentUser();

    window.localStorage.setItem(CURRENT_ROUTE, '/detection');

    if (!user || this.authService.isTokenExpired(token)) {
      this.router.navigate(['/login']);
    }
  }

  get formInvalid() {
    return this.detectionForm.invalid;
  }

  saveDetectionBatch() {
    this.loading = true;

    if (this.detectionForm.invalid) {
      alert('The batch cannot be saved. Please check the data.');
      return;
    }

    const cnt = this.getItemControl('detectionPersonnel');
    if (!cnt && cnt.value.length === 0) {
      this.now();
    }

    this.getItemControl('page').setValue('testing_detection');
    this.getItemControl('projectId').setValue(345);
    this.getItemControl('detectionBatchId').setValue(this.getItemControl('detectionBatchId').value);

    this.redcaDataService.saveBatch(this.detectionForm.value).pipe(catchError((error) => {
      this.router.navigate(['/login']);
      return of(new AuthenticationResponse());
    })).subscribe(
      data => {
        this.loading = false;
      }
    );
  }

  getItemControl(name): FormControl {
    return this.detectionForm.get(name) as FormControl;
  }

  getItemGroup(name): FormGroup {
    return this.detectionForm.get(name) as FormGroup;
  }

  newDetectionBatch() {
    this.editBatch(new Batch());
  }

  now() {

    this.getItemControl('detectionDateTime').patchValue(formatDate(new Date(), 'yyyy-MM-dd HH:mm', 'en-US'));
    const cnt = this.getItemControl('detectionPersonnel');
    cnt.patchValue(this.authService.getCurrentUser());
  }

  searchBatches() {
    this.loading = true;
    this.searchCriteria.includeSpecimen = true;
    this.redcaDataService.search(this.searchCriteria).subscribe(results => {
      this.batches.data = results;
      this.searchCriteria = new BatchSearchCriteria();
      this.loading = false;
    });
  }

  clearSearch() {
    this.batches.data = [];
  }

  editBatch(batch: Batch) {

    this.detectionForm = this.formBuilder.group(batch);

  }

  addSpecimen() {
    this.adding = true;
    if (this.getItemControl('detectionStatus').value !== 'Complete') {
      const batch = this.detectionForm.value;

      if (!batch.batchItems.find(item => item.specimen_barcode === this.barcode) &&
                    batch.batchItems.length <= 96) {
        const bc = this.barcode;
        this.specimenService.findSpecimenByBarcode(this.barcode).subscribe(result => {

          let sp = result;
          if (result === null) {
            sp = new Specimen();
            sp.specimen_barcode = bc;
            sp.dhis2Synched = false;
            sp.patient = new Patient();
          }

          sp.position = this.specimenService.encodePosition(batch.batchItems.length);
          batch.batchItems.push(sp);

          batch.instrumentBatchSize = batch.batchItems.length;
          batch.detectionSize = batch.batchItems.length;
          this.detectionForm = this.formBuilder.group(batch);
          this.adding = false;
        });
      }
    } else {
      alert('Cannot add specimen to a completed batch.');
    }

    this.barcode = '';
  }

  fetchBatchData() {
    const batch = this.detectionForm.value;
    this.searchCriteria.batchId = batch.batchId;
    this.searchCriteria.includeSpecimen = true;
    this.searchCriteria.lab = null;
    this.searchCriteria.specimenBarcode = null;

    if (!batch.detectionStatus) {
      this.redcaDataService.search(this.searchCriteria).subscribe(results => {
        if (results.length === 1) {
          console.log('Got saved batch', results);

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

  toResulting() {
    const batch = this.detectionForm.value as Batch;

    if ( batch.detectionStatus !== '2' ) { 
      alert('The batch is not complete and cannot be sent to resulting.');
      return;
    }

    localStorage.setItem(FORM_DATA, JSON.stringify(this.detectionForm.value));
    this.router.navigate(['/resulting']);
  }

  openDialog(action, obj) {

    this.removing = true;
    this.searchCriteria.batchId = this.getItemControl('batchId').value;
    this.searchCriteria.includeSpecimen = false;
    this.searchCriteria.lab = null;
    this.searchCriteria.specimenBarcode = null;

    // We don't want to remove from a saved batch
    this.redcaDataService.search(this.searchCriteria).subscribe(results => {
      console.log(results);
      if (results.length > 0) {
        alert('Cannot remove specimen from saved batch.');
      } else {

        obj.action = action;
        const dialogRef = this.dialog.open(DialogBoxComponent, {
          width: '250px',
          data: obj
        });

        dialogRef.afterClosed().subscribe(result => {
          if (result.event === 'Delete') {
            this.deleteRowData(result.data);
          }
        });
      }
    });
  }

  deleteRowData(rowObj) {
    const batch = this.detectionForm.value;
    batch.batchItems = batch.batchItems.filter((value, key) => {
      return value.id !== rowObj.id;
    });

    this.editBatch(batch);
  }

  get batchItems(): FormArray {
    return this.detectionForm.get('batchItems') as FormArray;
  }

  pullSpecimenInfo() {
    this.pulling = true;
    const batch = this.detectionForm.value;
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

  disableItems() {
    this.getItemControl('detectionPersonnel').disable({onlySelf: true});
    this.getItemControl('detectionDateTime').disable({onlySelf: true});
    this.getItemControl('instrumentBatchSize').disable({onlySelf: true});
  }
}
