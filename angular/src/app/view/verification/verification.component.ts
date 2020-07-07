import { Component, Injector } from '@angular/core';
import { formatDate } from '@angular/common';
import { catchError, first } from 'rxjs/operators';
import { of } from 'rxjs';
import { AuthenticationResponse } from 'src/app/model/authentication/authentication-response';
import { FORM_DATA } from 'src/app/helpers/dhis-link-constants';
import { BatchComponent } from '../batch/batch.component';

@Component({
  selector: 'app-verification',
  templateUrl: './verification.component.html',
  styleUrls: ['./verification.component.css']
})
export class VerificationComponent extends BatchComponent {

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

  constructor(private injector: Injector) {
    super(injector);
  }

  beforeOnInit() {
    this.page = 'verification';
  }

  authorise() {

    this.batchForm.controls.authorisingDateTime.setValue(formatDate(new Date(), 'yyyy-MM-dd HH:mm', 'en-US'));
    if (!this.batchForm.value.authorisingPersonnel || this.batchForm.value.authorisingPersonnel.length === 0) {

      this.batchForm.controls.authorisingPersonnel.setValue(this.authService.getCurrentUser());
    }
  }

  afterOnInit() {
  }

  preSaveBatch() {
    if (this.authService.getCurrentUser() === this.batchForm.value.resultingPersonnel) {
      alert('You entered the results so you cannot verify them.');
      return false;
    } else {

      if (!this.batchForm.value.verificationPersonnel || this.batchForm.value.verificationPersonnel.length === 0) {
        this.now();
      }
    }

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
}
