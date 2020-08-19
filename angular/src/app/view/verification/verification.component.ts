import { Component, Injector } from '@angular/core';
import { formatDate } from '@angular/common';
import { catchError, first } from 'rxjs/operators';
import { of } from 'rxjs';
import { AuthenticationResponse } from 'src/app/model/authentication/authentication-response';
import { FORM_DATA } from 'src/app/helpers/dhis-link-constants';
import { BatchComponent } from '../batch/batch.component';
import { Batch } from 'src/app/model/batch/batch';
import { RedcapData } from 'src/app/model/data/redcap-data';
import { Specimen } from 'src/app/model/specimen/specimen';

@Component({
  selector: 'app-verification',
  templateUrl: './verification.component.html',
  styleUrls: ['./verification.component.css']
})
export class VerificationComponent extends BatchComponent {

  searchColumns: string[] = [' ', 'batchId', 'resultingPersonnel', 'resultingDateTime', 'instrumentBatchSize', 'resultingStatus'];
  specimenColumns: string[] = [
    'position',
    'specimen_barcode',
    'patient_first_name',
    'patient_surname',
    'identity_no',
    'covidRnaResults',
    'testVerifyResults'
  ];

  constructor(private injector: Injector) {
    super(injector);
  }

  beforeOnInit() {
    this.page = 'verification';
  }

  authorise() {

    const batch: Batch = this.batchForm.value;

    if (this.authService.getCurrentUser() === batch.resultingPersonnel) {
      alert('You entered the results so you cannot verify them.');
      return;
    }

    this.batchForm.controls.authorisingDateTime.setValue(formatDate(new Date(), 'yyyy-MM-ddTHH:mm', 'en-US'));
    if (!this.batchForm.value.authorisingPersonnel || this.batchForm.value.authorisingPersonnel.length === 0) {

      this.batchForm.controls.authorisingPersonnel.setValue(this.authService.getCurrentUser());
    }
  }

  afterOnInit() {
  }

  preSaveBatch(batch: Batch) {

    batch.batchItems.forEach(
      sp => {

        if(batch.verificationDateTime && batch.verificationPersonnel) {
          sp.test_verify_datetime = batch.verificationDateTime;
          sp.test_verify_personnel = batch.verificationPersonnel;

          if(sp.testVerifyResults === '5') {
            sp.covidRnaResults = sp.testAssayResults;
          }
        }

        if(batch.authorisingDateTime && batch.authorisingPersonnel) {
          sp.authorizer_datetime = batch.authorisingDateTime;
          sp.authorizer_personnel = batch.authorisingPersonnel;
        }
      }
    );

    return true;
  }

  postSaveBatch() {
  }

  verified(): boolean {
    const batch = this.batchForm.value;
    if (batch.verificationStatus === '2' &&
      batch.authorisingPersonnel) {

      return false;
    }

    return true;
  }

  onVerifySelectionChange(specimen: Specimen, event) {
    
  }
}
