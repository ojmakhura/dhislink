import { Component, Injector } from '@angular/core';
import { Batch } from 'src/app/model/batch/batch';
import { Specimen } from 'src/app/model/specimen/specimen';
import { FORM_DATA, CURRENT_ROUTE } from 'src/app/helpers/dhis-link-constants';
import { Patient } from 'src/app/model/patient/patient';
import { MatDialog } from '@angular/material/dialog';
import { DialogBoxComponent } from '../dialog-box/dialog-box.component';
import { BatchComponent } from '../batch/batch.component';

@Component({
  selector: 'app-testing-detection',
  templateUrl: './testing-detection.component.html',
  styleUrls: ['./testing-detection.component.css']
})
export class TestingDetectionComponent extends BatchComponent {

  barcode = '';
  adding = false;
  removing = false;

  searchColumns: string[] = [' ', 'batchId', 'detectionPersonnel', 'detectionDateTime', 'detectionStatus'];
  specimenColumns: string[] = [' ', 'position', 'specimen_barcode', 'patient_first_name', 'patient_surname', 'identity_no'];

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
    if (this.getItemControl('detectionStatus').value !== 'Complete') {
      const batch = this.batchForm.value;

      if (!batch.batchItems.find(item => item.specimen_barcode === this.barcode) &&
                    batch.batchItems.length <= 96) {
        const bc = this.barcode;
        this.specimenService.findSpecimenByBarcode(this.barcode).subscribe(result => {

          let sp = result;
          if (result === null) {
            sp = new Specimen();
            sp.specimen_barcode = bc;
            sp.dhis2Synched = false;
          }

          if (sp.patient === null) {
            sp.patient = new Patient();
          }

          sp.position = this.specimenService.encodePosition(batch.batchItems.length);
          batch.batchItems.push(sp);

          batch.instrumentBatchSize = batch.batchItems.length;
          batch.detectionSize = batch.batchItems.length;
          console.log(sp);

          this.batchForm = this.formBuilder.group(batch);

          this.adding = false;
        });
      } else {
        if (batch.batchItems.find(item => item.specimen_barcode === this.barcode)) {
          alert('Specimen already in the batch.');
        } else if (this.batchItems.length > 96) {
          alert('Batch is full.');
        } else {
          alert('Could not add the specimen. Unknown error occured.');
        }
        this.adding = false;
      }
    } else {
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
    const batch = this.batchForm.value;
    batch.batchItems = batch.batchItems.filter((value, key) => {
      return value.id !== rowObj.id;
    });

    this.editBatch(batch);
  }

  disableItems() {
    this.getItemControl('detectionPersonnel').disable({onlySelf: true});
    this.getItemControl('detectionDateTime').disable({onlySelf: true});
    this.getItemControl('instrumentBatchSize').disable({onlySelf: true});
  }
}
