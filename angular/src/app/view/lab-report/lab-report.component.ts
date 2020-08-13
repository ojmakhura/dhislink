import { Component, OnInit, Injector, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from 'src/app/service/authentication/authentication.service';
import { LocationService } from 'src/app/service/location/location.service';
import { RedcapDataService } from 'src/app/service/data/redcap-data.service';
import { SpecimenService } from 'src/app/service/specimen/specimen.service';
import { RxFormBuilder } from '@rxweb/reactive-form-validators';
import { Specimen } from 'src/app/model/specimen/specimen';
import { FormGroup } from '@angular/forms';
import { RedcapData } from 'src/app/model/data/redcap-data';
import { BatchComponent } from '../batch/batch.component';
import { MatDialog } from '@angular/material/dialog';
import { Patient } from 'src/app/model/patient/patient';
import { BatchAuthorityStage } from 'src/app/model/batch/BatchAuthorisationStage';
import { DialogBoxComponent } from '../dialog-box/dialog-box.component';
import { Batch } from 'src/app/model/batch/batch';
import { formatDate } from '@angular/common';

@Component({
  selector: 'app-lab-report',
  templateUrl: './lab-report.component.html',
  styleUrls: ['./lab-report.component.css']
})
export class LabReportComponent extends BatchComponent {

  barcode = '';
  adding = false;
  removing = false;
  private redcapData: Array<RedcapData>;

  searchColumns: string[] = [' ', 'batchId', 'detectionPersonnel', 'detectionDateTime', 'instrumentBatchSize', 'detectionStatus'];
  specimenColumns: string[] = [' ', 'position', 'specimen_barcode', 'patient_first_name', 'patient_surname', 'identity_no', 'add_control'];

  constructor(private injector: Injector, public dialog: MatDialog) {

    super(injector);
  }

  beforeOnInit() {
    this.page = 'detection';
  }

  afterOnInit() {
  }

  postSaveBatch() {
  }

  preSaveBatch() {

    const cnt = this.getItemControl('detectionPersonnel');
    if (!cnt && cnt.value.length === 0) {
      this.now();
    }
    this.getItemControl('detectionBatchId').setValue(this.getItemControl('detectionBatchId').value);

    return true;
  }

  addSpecimen() {
    this.adding = true;
    if (this.getItemControl('detectionStatus').value !== '2') {

      const batch = this.batchForm.value;

      if (this.batchItems.length >= 96) {
        alert('Cannot add specimen to a full batch.');
        this.adding = false;
        return;
      }

      if (batch.batchItems.find(item => item.specimen_barcode === this.barcode)) {
        const conf = confirm('This specimen is already in the batch. Are you sure you want to add it?');

        if (!conf) {
          this.adding = true;
          return;
        }
      }

      const bc = this.barcode;
      this.specimenService.findSpecimenByBarcode(this.barcode).subscribe(
        result => {

          let sp = result;
          if (result === null) {

            sp = new Specimen();
            sp.specimen_barcode = bc;
            sp.dhis2Synched = false;
          }

          if (sp.patient === null || !sp.patient) {
            sp.patient = new Patient();
          }

          sp.position = this.specimenService.encodePosition(batch.batchItems.length);
          batch.batchItems.push(sp);

          batch.instrumentBatchSize = batch.batchItems.length;
          batch.detectionSize = batch.batchItems.length;
          this.batchForm = this.formBuilder.group(batch);

          this.adding = false;
        }, error => {
          this.adding = false;
        }
      );

    } else {
      this.adding = false;
      alert('Cannot add specimen to a completed batch.');
    }

    this.barcode = '';
  }

  openDialog(action, obj) {

    this.removing = true;
    this.searchCriteria.batchId = this.getItemControl('batchId').value;
    this.searchCriteria.includeSpecimen = false;
    this.searchCriteria.lab = null;
    this.searchCriteria.specimenBarcode = null;
    this.searchCriteria.page = BatchAuthorityStage.DETECTION;
    // We don't want to remove from a saved batch
    this.redcaDataService.search(this.searchCriteria).subscribe(results => {

      if (results.length > 0 && this.getItemControl('detectionStatus').value === '2') {
        alert('Cannot remove specimen from a completed batch.');
      } else {

        obj.action = action;
        const dialogRef = this.dialog.open(DialogBoxComponent, {
          width: '250px',
          data: obj
        });

        dialogRef.afterClosed().subscribe(result => {
          if (result.event === 'Delete') {
            console.log(result);

            this.deleteRowData(result.data);
          }
        });
      }
    });
  }

  deleteRowData(rowObj) {
    const batch = this.batchForm.value;
    batch.batchItems.forEach((element, index) => {

      if (element.position === rowObj.value.position) {
        batch.batchItems.splice(index, 1);
      }
    });

    batch.batchItems.forEach((element, index) => {
      element.position = this.specimenService.encodePosition(index);
    });
    batch.instrumentBatchSize = batch.batchItems.length;
    this.editBatch(batch, false);
  }

  addControl(row: string, barcode: string) {
    console.log(this.getItemControl('detectionStatus').value);

    if (this.getItemControl('detectionStatus').value !== '2') {
      if (barcode === 'C000000000') {
        alert('The location ' + row + ' is already a control.');
        return;
      }

      const batch: Batch = this.batchForm.value;

      if (batch.batchItems.length === 96) {
        alert('The batch is already full.');
        return;
      }

      const items: Array<Specimen> = [];
      let position: number = this.specimenService.decodePosition(row);
      let idx = position - 1;
      let control = this.createControl();
      control.position = this.specimenService.encodePosition(idx);

      batch.batchItems.splice(idx, 0, control);

      for (let i = position; i < batch.batchItems.length; i++) {
        batch.batchItems[i].position = this.specimenService.encodePosition(i);
      }

      batch.batchItems.forEach((element, index) => {
        element.position = this.specimenService.encodePosition(index);
      });

      batch.instrumentBatchSize = batch.batchItems.length;
      this.editBatch(batch, false);
    } else {
      alert('Cannot add specimen to a completed batch.');
    }
  }

  isControl(barcode) {
    return barcode === 'C000000000';
  }

  private createControl() {

    const control: Specimen = new Specimen();
    control.patient = new Patient();
    control.patient.patient_first_name = 'Control';
    control.patient.patient_surname = 'Control';
    control.patient.identity_no = '000000000';
    control.specimen_barcode = 'C000000000';
    return control;
  }

  disableItems() {
    this.getItemControl('detectionPersonnel').disable({ onlySelf: true });
    this.getItemControl('detectionDateTime').disable({ onlySelf: true });
    this.getItemControl('instrumentBatchSize').disable({ onlySelf: true });
  }
}
