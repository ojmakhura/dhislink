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

  //batch: Batch;
  batches: MatTableDataSource<Batch>;
  //specimen: MatTableDataSource<Specimen>;
  locations: LocationVO[];
  searchCriteria: BatchSearchCriteria;
  selectedIndex = 0;
  instruments: Instrument[];
  barcode = '';
  //labControl = new FormControl('', Validators.required);
  //instrumentControl = new FormControl('', Validators.required);
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
    this.locationService.findAll().subscribe(results => {
      this.locations = results;
    });
    this.batches = new MatTableDataSource<Batch>();
    this.searchCriteria = new BatchSearchCriteria();
    this.detectionForm = this.formBuilder.group(new Batch());
    
    this.detectionForm.addControl('lab', this.formBuilder.control(new LocationVO()));
    this.detectionForm.addControl('instrument', this.formBuilder.control());
    this.instruments = InstrumentList.allIntruments();

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

    /*if (!this.batch.detectionPersonnel || this.batch.detectionPersonnel.length === 0) {
      this.now();
    }*/

    const cnt = this.getItemControl('detectionPersonnel');
    if (!cnt && cnt.value.length === 0) {
      this.now();
    }

    this.getItemControl('page').setValue('resulting');
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
    console.log('Editing batch: ', batch);
    console.log('my form ==== ', this.detectionForm);

    this.detectionForm = this.formBuilder.group(batch);
    //this.getItemControl('labControl').setValue(batch.lab);
    //this.getItemControl('instrumentControl').setValue(batch.instrument);
    /*if(batch.lab && batch.lab.code && batch.lab.code.length > 0) {
      this.getItemGroup('lab').patchValue(batch.lab);
      this.labControl.setValue(batch.lab);
      console.log('real => ', this.labControl);
      
    }

    if(batch.instrument && batch.instrument.code && batch.instrument.code.length > 0) {
      this.getItemControl('instrument').setValue(batch.instrument);
    }

    this.getItemControl('detectionStatus').setValue(batch.detectionStatus);

    this.getItemControl('detectionSize').setValue(batch.batchItems.length);
    this.getItemControl('instrumentBatchSize').setValue(batch.batchItems.length);

    if (batch.detectionDateTime && batch.detectionDateTime.length > 0) {
      this.getItemControl('detectionDateTime').setValue(formatDate(batch.detectionDateTime, 'yyyy-MM-dd HH:mm', 'en-US'));
      this.getItemControl('detectionPersonnel').setValue(batch.detectionPersonnel);
    }*/

    console.log('my form ', this.detectionForm);

  }

  addSpecimen() {
    this.adding = true;
    if (this.getItemControl('detectionStatus').value !== 'Complete') {
      const batch = this.detectionForm.value;

      if (!batch.batchItems.find(item => item.specimen_barcode === this.barcode) &&
                    batch.batchItems.length <= 96) {
        const bc = this.barcode;
        this.specimenService.findSpecimenByBarcode(this.barcode).subscribe(result => {
          console.log(result);
          let sp = result;
          if (result === null) {
            console.log('bc => ', bc);
            sp = new Specimen();
            sp.specimen_barcode = bc;
            sp.dhis2Synched = false;
            sp.patient = new Patient();
          }

          sp.position = this.specimenService.encodePosition(batch.batchItems.length);
          batch.batchItems.push(sp);

          batch.instrumentBatchSize = batch.batchItems.length;
          batch.detectionSize = batch.batchItems.length;
          console.log('bcc => ', batch);
          this.detectionForm = this.formBuilder.group(batch);
          console.log('bcc3 => ', this.detectionForm);
          this.adding = false;
        });
      }
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
          console.log('Got saved batch');
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
  }

  toResulting() {
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
      });

      this.pulling = false;
    });
  }
}
