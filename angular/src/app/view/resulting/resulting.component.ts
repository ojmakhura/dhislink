import { Component, Injector } from '@angular/core';
import { Batch } from 'src/app/model/batch/batch';
import { BatchSearchCriteria } from 'src/app/model/batch/batch-search-criteria';
import { FORM_DATA, CURRENT_ROUTE } from 'src/app/helpers/dhis-link-constants';
import { BatchComponent } from '../batch/batch.component';
import { BatchAuthorityStage } from 'src/app/model/batch/BatchAuthorisationStage';

@Component({
  selector: 'app-resulting',
  templateUrl: './resulting.component.html',
  styleUrls: ['./resulting.component.css']
})
export class ResultingComponent extends BatchComponent {

  searchColumns: string[] = [' ', 'batchId', 'resultingPersonnel', 'resultingDateTime', 'instrumentBatchSize', 'resultingStatus'];
  specimenColumns: string[] = ['position', 'specimen_barcode', 'patient_first_name', 'patient_surname', 'identity_no', 'testAssayResults'];

  constructor(private injector: Injector) {
    super(injector);
  }

  afterOnInit() {
  }

  preSaveBatch() {
    const cnt = this.getItemControl('resultingPersonnel');

    if (!cnt && cnt.value.length === 0) {
      this.now();
    }
    this.getItemControl('assayBatchId').setValue(this.getItemControl('detectionBatchId').value);

    return true;
  }

  pullSpecimenInfo() {
    this.pulling = true;
    const batch = this.batchForm.value;
    this.redcaDataService.pullSpecimenInfo(batch.batchItems).subscribe(results => {
      this.searchCriteria.includeSpecimen = true;
      this.searchCriteria.batchId = batch.batchId;
      this.searchCriteria.page = BatchAuthorityStage.RESULTING

      this.redcaDataService.search(this.searchCriteria).subscribe(batches => {
        this.searchCriteria = new BatchSearchCriteria();
        if (batches.length > 0) {
          this.editBatch(batches[0], false);
        }
        this.searchCriteria = new BatchSearchCriteria();
      });

      this.pulling = false;
    });
  }

  beforeOnInit() {
    this.page = 'resulting';
  }

  postSaveBatch() {
  }
}
