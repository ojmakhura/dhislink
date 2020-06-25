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
import { formatDate } from '@angular/common';
import { NgForm }   from '@angular/forms';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { AuthenticationResponse } from 'src/app/model/authentication/authentication-response';
import { FORM_DATA, CURRENT_ROUTE } from 'src/app/helpers/dhis-link-constants';
import { Patient } from 'src/app/model/patient/patient';
import { MatDialog } from '@angular/material/dialog';
import { DialogBoxComponent } from '../dialog-box/dialog-box.component';

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
  loading: boolean = false;
  adding = false;
  removing = false;

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
              public dialog: MatDialog) {

    this.batches = new MatTableDataSource<Batch>();
    this.specimen = new MatTableDataSource<Specimen>();
    this.searchCriteria = new BatchSearchCriteria();
    this.instruments = InstrumentList.allIntruments();

    this.locationService.findAll().subscribe(results => {
      this.locations = results;
    });

    if (localStorage.getItem(FORM_DATA)) {
      const batch: Batch = JSON.parse(localStorage.getItem(FORM_DATA));
      this.editBatch(batch);
      localStorage.removeItem(FORM_DATA);
    } else {
      this.batch = new Batch();
      this.batch.lab = new LocationVO();
    }
  }

  ngOnInit(): void {

    const token = this.authService.getToken();
    const user = this.authService.getCurrentUser();

    window.localStorage.setItem(CURRENT_ROUTE, '/detection');
    // this.authService.getLoggeInUser();

    if (!user || this.authService.isTokenExpired(token)) {
      this.router.navigate(['/login']);
    }
  }

  ngAfterViewInit() {

    //this.batches.paginator = this.batchesPaginator;
    //this.batches.sort = this.batchSort;

    //this.specimen.paginator = this.specimenPaginator;
  }

  saveDetectionBatch() {
    this.loading = true;
    if (!this.batch.detectionPersonnel || this.batch.detectionPersonnel.length === 0) {
      this.now();
    }

    this.batch.lab = this.locations.find(loc => loc.code === this.labControl.value)
    this.batch.instrument = this.instruments.find(loc => loc.code === this.instrumentControl.value);
    this.batch.page = 'testing_detection';
    this.batch.projectId = 345;
    this.batch.detectionBatchId = this.batch.batchId;
    this.batch.assayBatchId = this.batch.batchId;
    this.batch.verifyBatchId = this.batch.batchId;

    this.redcaDataService.saveBatch(this.batch).pipe(catchError((error) => {
      this.router.navigate(['/login']);
      return of(new AuthenticationResponse());
    })).subscribe(
      data => {
        this.loading = false;
      }
    );
  }

  newDetectionBatch() {
    this.batch = new Batch();
    this.specimen.data = this.batch.batchItems;
    this.labControl.setValue(this.batch.lab.code);
    this.instrumentControl.setValue(this.batch.instrument.code);
  }

  now() {
    this.batch.detectionDateTime = formatDate(new Date(), "yyyy-MM-dd HH:mm", 'en-US');
    if (!this.batch.detectionPersonnel || this.batch.detectionPersonnel.length === 0) {

      this.batch.detectionPersonnel = this.authService.getCurrentUser();

    }
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

    this.batch = batch;
    this.batch.detectionDateTime = formatDate(batch.detectionDateTime, 'yyyy-MM-dd HH:mm', 'en-US');
    this.specimen.data = this.batch.batchItems;
    this.labControl.setValue(batch.lab.code);
    this.instrumentControl.setValue(batch.instrument.code);
    this.batch.instrumentBatchSize = this.batch.batchItems.length;
    this.batch.detectionSize = this.specimen.data.length;

  }

  addSpecimen() {
    this.adding = true;
    if (this.batch.detectionStatus !== 'Complete') {
      if (!this.batch.batchItems.find(item => item.specimen_barcode === this.barcode) &&
          this.batch.batchItems.length <= 96) {
        const bc = this.barcode;
        this.specimenService.findSpecimenByBarcode(this.barcode).subscribe(result => {
          console.log(result);
          let sp = result;
          if (result === null) {
            console.log(bc);
            sp = new Specimen();
            sp.specimen_barcode = bc;
            sp.dhis2Synched = false;
            sp.patient = new Patient();
          }
          
          sp.position = this.specimenService.encodePosition(this.batch.batchItems.length);
          this.batch.batchItems.push(sp);
          console.log(this.batch);
          
          this.specimen.data  = this.batch.batchItems;
          this.batch.instrumentBatchSize = this.batch.batchItems.length;
          this.batch.detectionSize = this.specimen.data.length;
          this.adding = false;
        });
      }
    }

    this.barcode = '';
  }

  fetchBatchData() {

    this.searchCriteria.batchId = this.batch.batchId;
    this.searchCriteria.includeSpecimen = false;
    this.searchCriteria.lab = null;
    this.searchCriteria.specimenBarcode = null;

    if (!this.batch.detectionStatus) {
      this.redcaDataService.search(this.searchCriteria).subscribe(results => {
        if (results.length === 1) {
          console.log('Got saved batch');
          this.editBatch(results[0]);
        } else {
          this.redcaDataService.fetchExtractionSpecimen(this.batch.batchId).subscribe(results => {

            this.batch.batchItems = results;
            for (let i = 0; i < results.length; i++) {
              results[i].position = this.specimenService.encodePosition(i);
            }

            this.specimen.data  = this.batch.batchItems;
            this.batch.instrumentBatchSize = this.specimen.data.length;
            this.batch.detectionSize = this.specimen.data.length;
          });
        }
      });

    }
  }

  toResulting() {
    localStorage.setItem(FORM_DATA, JSON.stringify(this.batch));
    this.router.navigate(['/resulting']);
  }

  openDialog(action, obj) {

    this.removing = true;
    this.searchCriteria.batchId = this.batch.batchId;
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
    this.specimen.data = this.specimen.data.filter((value, key) => {
      return value.id !== rowObj.id;
    });
  }
}
