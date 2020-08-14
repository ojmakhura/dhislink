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
import { FORM_DATA, CURRENT_ROUTE, RAW_DATA } from 'src/app/helpers/dhis-link-constants';
import { formatDate } from '@angular/common';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { AuthenticationResponse } from 'src/app/model/authentication/authentication-response';
import { MatTableDataSource } from '@angular/material/table';
import { BatchAuthorityStage } from 'src/app/model/batch/BatchAuthorisationStage';
import { Specimen } from 'src/app/model/specimen/specimen';
import { Patient } from 'src/app/model/patient/patient';
import { RedcapData } from 'src/app/model/data/redcap-data';
import { element } from 'protractor';

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
  spLoading = false;
  isNewBatch = true;
  publishing = false;
  printing = false;

  batchForm: FormGroup;
  searchForm: FormGroup;
  batchesForm: FormGroup;

  @ViewChild('batchesPaginator', { static: true }) batchesPaginator: MatPaginator;
  @ViewChild('batchSort', { static: true }) batchSort: MatSort;

  @ViewChild('specimenPaginator', { static: true }) specimenPaginator: MatPaginator;

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

    const batch = new Batch();
    if(batch.a1Control) {
      let control = new Specimen();
      control.patient = new Patient();
      control.position = this.specimenService.encodePosition(0);
      batch.batchItems.push(control);
    }
    this.batchForm = this.formBuilder.group(batch);
    this.searchForm = this.formBuilder.group(new BatchSearchCriteria());
    this.batches = new MatTableDataSource<Batch>();
    this.searchCriteria = new BatchSearchCriteria();
    this.instruments = InstrumentList.allIntruments();

    if (localStorage.getItem(FORM_DATA)) {
      const batch = JSON.parse(localStorage.getItem(FORM_DATA));
      this.editBatch(batch, false);
      localStorage.removeItem(FORM_DATA);
    }

    window.localStorage.setItem(CURRENT_ROUTE, '/' + this.page);
    this.afterOnInit();
  }

  ngAfterViewInit() {
    this.batches.paginator = this.batchesPaginator;
    this.batches.sort = this.batchSort;
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
    this.searchCriteria.includeSpecimen = false;

    if (this.page === 'detection') {
      this.searchCriteria.page = BatchAuthorityStage.DETECTION;
    } else if (this.page === 'resulting') {
      this.searchCriteria.page = BatchAuthorityStage.RESULTING;
    } else {
      this.searchCriteria.page = BatchAuthorityStage.AUTHORISATION;
    }

    this.redcaDataService.search(this.searchCriteria).pipe().subscribe(
      results => {
        //this.batchesForm = this.formBuilder.array([results]);
        this.batches.data = results;
        this.searchCriteria = new BatchSearchCriteria();
        this.loading = false;
      }, error => {
        this.loading = false;
      }
    );
  }

  clearSearch() {
    this.batches.data = [];

  }

  now() {
    const dt = formatDate(new Date(), 'yyyy-MM-ddTHH:mm', 'en-US');
    this.getItemControl(this.page + 'DateTime').patchValue(dt);

    const usr = this.authService.getCurrentUser();
    const cnt = this.getItemControl(this.page + 'Personnel');
    cnt.patchValue(usr);
  }

  selectBatchEdit(batch: Batch, fetchSpecimen: boolean) {
    this.isNewBatch = false;
    this.editBatch(batch, fetchSpecimen);
  }

  editBatch(batch: Batch, fetchSpecimen: boolean) {

    this.spLoading = true;
    if (this.page === 'detection') {
      batch.page = BatchAuthorityStage.DETECTION;
    } else if (this.page === 'resulting') {
      batch.page = BatchAuthorityStage.RESULTING;
    } else {
      batch.page = BatchAuthorityStage.AUTHORISATION;
    }

    batch.projectId = 345;

    // If there are no specimen in the batch
    if(fetchSpecimen && (!batch.batchItems || batch.batchItems.length === 0)) {
      
      this.redcaDataService.fetchBatchSpecimen(batch).subscribe(
        results => {
          batch.batchItems = results;
          batch.detectionSize = batch.batchItems.length;
          batch.instrumentBatchSize = batch.batchItems.length;
          
          this.batchForm = this.formBuilder.group(batch);
          this.spLoading = false;
          this.selectedTab = 0;
        }, error => {
          this.spLoading = false;
        }
      );
    } else {

      batch.detectionSize = batch.batchItems.length;
      batch.instrumentBatchSize = batch.batchItems.length;
      
      this.batchForm = this.formBuilder.group(batch);
      this.spLoading = false;
      this.selectedTab = 0;
    }

  }

  abstract preSaveBatch(batch: Batch): boolean;

  saveBatch() {

    const batch: Batch = this.batchForm.value;

    this.loading = true;
    if (!this.preSaveBatch(batch)) {
      return;
    }

    if (this.batchForm.invalid) {
      alert('The batch cannot be saved. Please check the data.');
      return;
    }

    let pageValue;

    if (this.page === 'detection') {
      pageValue = BatchAuthorityStage.DETECTION;
    } else if (this.page === 'resulting') {
      pageValue = BatchAuthorityStage.RESULTING;
    } else {
      pageValue = BatchAuthorityStage.AUTHORISATION;
    }

    this.redcaDataService.saveRawBatch(this.toRawBatch(this.batchForm.value), 345).subscribe(
      results => {
        this.loading = false;
      }, error => {
        this.loading = false;
      }
    );

    // Lazily save to the staging area
    this.redcaDataService.saveBatch(this.batchForm.value).subscribe();
    
    this.postSaveBatch();
  }

  abstract postSaveBatch();

  newBatch() {
    this.isNewBatch = true;
    this.editBatch(new Batch(), false);
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
    this.redcaDataService.pullSpecimenInfo(batch.batchItems).subscribe(
      results => {
      
        this.searchCriteria.includeSpecimen = true;
        this.searchCriteria.batchId = batch.batchId;

        if (this.page === 'detection') {
          this.searchCriteria.page = BatchAuthorityStage.DETECTION;
        } else if (this.page === 'resulting') {
          this.searchCriteria.page = BatchAuthorityStage.RESULTING;
        } else {
          this.searchCriteria.page = BatchAuthorityStage.AUTHORISATION;
        }

        batch.batchItems = results;
        this.doEditBatch(batch);
        this.pulling = false;
      }, error => {

        this.pulling = false;
      }
    );
  }

  async fetchBatchData() {
    const batch = this.batchForm.value;
    
    this.searchCriteria.batchId = batch.batchId;
    this.searchCriteria.includeSpecimen = true;
    this.searchCriteria.lab = null;
    this.searchCriteria.specimenBarcode = null;
    
    if (this.page === 'detection') {
      this.searchCriteria.page = BatchAuthorityStage.DETECTION;
    } else if (this.page === 'resulting') {
      this.searchCriteria.page = BatchAuthorityStage.RESULTING;
    } else {
      this.searchCriteria.page = BatchAuthorityStage.AUTHORISATION;
    }

    this.redcaDataService.dataSearch(this.searchCriteria).subscribe(
      results => {
        console.log(results);
        
        if(Object.keys(results).length === 1) {
          Object.keys(results).forEach((element, index) => {
            let bt: Batch = new Batch();
            const data: RedcapData[] = results[element];
            
            data.forEach((val, idx) => {
              bt = this.composeBatch(val, data, bt);            
            });

            bt.batchId = element;
            bt.instrumentBatchSize = bt.batchItems.length;
            bt.detectionSize = bt.batchItems.length;
            
            this.doEditBatch(bt);
          });
          
        } else {
          this.searchCriteria.page = BatchAuthorityStage.EXTRACTION;
          this.searchCriteria.batchId = this.getItemControl('batchId').value;
          
          this.redcaDataService.dataSearch(this.searchCriteria).subscribe(
            results => {
              
              if(Object.keys(results).length === 1) {
                Object.keys(results).forEach( (element, index) => {

                  let bt: Batch = new Batch();
                  
                  const data = results[element];
                  bt.batchId = element;

                  bt.detectionBatchId = element;
                  bt.assayBatchId = element;
                  bt.verifyBatchId = element;

                  data.forEach(val => {
                    if(val['field_name'] === 'extraction_lab') {
                      bt.lab = this.locations.find(loc => loc.code === val.value);

                    } else if (val['field_name'].substring(0, 17) === 'test_ext_barcode_') {

                      const pos = +val['field_name'].substring(17);
                      const specimen = new Specimen();
                      specimen.patient = new Patient();
                      specimen.position = this.specimenService.encodePosition(pos - 1);
                      specimen.specimen_barcode = val.value;
                      bt.batchItems.push(specimen);

                    }                    
                  });
                  
                  this.doEditBatch(bt);
                });
              }
              this.searchCriteria = new BatchSearchCriteria();
            }
            
          );
        }
        this.searchCriteria = new BatchSearchCriteria();
      }
    );

    this.searchCriteria = new BatchSearchCriteria();
  }

  tabChanged(event) {
    this.selectedTab = event;
  }

  /**
   * This publishes the verified results to DHIS2
   */
  publish() {
    this.publishing = true;
    const batch: Batch = this.batchForm.value;
    batch.page = 'verification';

    if (batch.verificationStatus !== '2' &&
      !batch.authorisingPersonnel) {
      alert('Could not publish results. Either verification is not complete or authorising personel is not set.')
    } else {
      batch.publishResults = true;

      let data: any;
      this.redcaDataService.saveBatch(batch).pipe().subscribe(results => {
        data = results;
        this.publishing = false;
      }, error => {
        this.publishing = false
        this.router.navigate(['/login']);
        return of(new AuthenticationResponse());
      }
      );
      this.batchForm.patchValue(batch);
      return data;
    }
  }

  toResulting() {
    const batch = this.batchForm.value as Batch;

    if (batch.detectionStatus !== '2') {
      alert('The batch is not complete and cannot be sent to resulting.');
      return;
    }

    localStorage.setItem(FORM_DATA, JSON.stringify(this.batchForm.value));
    this.router.navigate(['/resulting']);
  }

  toVerification() {
    const batch = this.batchForm.value as Batch;
    if (batch.resultingStatus !== '2') {
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

  composeBatch(val: RedcapData, data: any, bt: Batch): Batch {

    /// Detection
    if (val['field_name'] === 'detection_lab') {
      bt.lab = this.locations.find(loc => loc.code === val.value);
    }

    if (val['field_name'] === 'test_det_instrument') {
      bt.detectionBatchId = val.record;
      bt.instrument = this.instruments.find(inst => inst.code === val.value);
    }

    if (val['field_name'] === 'test_det_personnel') {
      bt.detectionPersonnel = val.value;
    }

    if (val['field_name'] === 'test_det_datetime') {
      bt.detectionDateTime = formatDate(new Date(val.value), 'yyyy-MM-ddTHH:mm', 'en-US');
    }

    if (val['field_name'] === 'testing_detection_complete') {
      bt.detectionStatus = val.value;
    }

    if (val['field_name'] === 'test_det_batchsize') {
      bt.instrumentBatchSize = +val.value;
      bt.detectionSize = bt.instrumentBatchSize;
      
    }

    /// Resulting
    if (val['field_name'] === 'test_assay_personnel') {
      bt.assayBatchId = val.record;
      bt.resultingPersonnel = val.value;
    }

    if (val['field_name'] === 'test_assay_datetime') {
      bt.resultingDateTime = formatDate(new Date(val.value), 'yyyy-MM-ddTHH:mm', 'en-US');
    }

    if (val['field_name'] === 'resulting_complete') {
      bt.resultingStatus = val.value;
    }

    /// Verification
    if (val['field_name'] === 'test_verify_personnel') {
      bt.verifyBatchId = val.record;
      bt.verificationPersonnel = val.value;
    }

    if (val['field_name'] === 'test_verify_datetime') {
      bt.verificationDateTime = formatDate(new Date(val.value), 'yyyy-MM-ddTHH:mm', 'en-US');
    }

    if (val['field_name'] === 'verification_complete') {
      bt.verificationStatus = val.value;
    }

    /// Spcimen
    if (val['field_name'].substring(0, 17) === 'test_det_barcode_') {
      let specimen: Specimen = bt.batchItems.find(sp => sp.specimen_barcode === val.value);

      if (!specimen) {
        specimen = new Specimen();
      }

      specimen.patient = new Patient();

      const pos = +val['field_name'].substring(17);
      specimen.specimen_barcode = val.value;
      specimen.position = this.specimenService.encodePosition(pos-1);

      val = data.find(d => d['field_name'] === 'test_assay_result_' + pos);
      if (val) {
        specimen.testAssayResults = val.value;
      }

      val = data.find(d => d['field_name'] === 'test_verify_result_' + pos);
      if (val) {
        specimen.testVerifyResults = val.value;
      }

      val = data.find(d => d['field_name'] === 'covid_rna_results' + pos);
      if (val) {
        specimen.covidRnaResults = val.value;
      } else {
        specimen.covidRnaResults = specimen.testAssayResults;
      }
      
      if(!bt.authorisingPersonnel && specimen.authorizer_personnel) {
        bt.authorisingPersonnel = specimen.authorizer_personnel;
      }

      if(!bt.authorisingDateTime && specimen.authorizer_datetime) {
        bt.authorisingDateTime = specimen.authorizer_datetime;
      }

      bt.batchItems.push(specimen);
    }
    
    return bt;
  }

  doSearch() {
    
    this.loading = true;
    this.searchCriteria.page = BatchAuthorityStage.DETECTION;
    this.redcaDataService.dataSearch(this.searchCriteria).subscribe(
      async results => {
        let bts: Array<Batch> = [];
        
        Object.keys(results).forEach((element, index) => {
          let bt: Batch = new Batch();
          bt.batchId = element;
          const data: RedcapData[] = results[element];
          
          data.forEach(async (val, idx) => {
            bt = await this.composeBatch(val, data, bt);            
          });

          bts.push(bt);
        });
        
       await bts.sort(
          (a, b) => (a.batchId < b.batchId) ? 1 : -1
        );
        
        this.batches.data = bts;
        this.loading = false;

        this.searchCriteria = new BatchSearchCriteria();
      }
    );
  }

  doEditBatch(batch: Batch) {

    this.spLoading = true;
    batch.batchItems.sort(
      (a, b) => (this.specimenService.decodePosition(a.position) > this.specimenService.decodePosition(b.position)) ? 1 : -1
    );

    this.spLoading = false;
    this.editBatch(batch, false);
    
    // Lazily load specimen information
    this.batchItems.controls.forEach(
      control => {
        const specimen: Specimen = control.value;
        
        this.specimenService.findSpecimenByBarcode(specimen.specimen_barcode).subscribe(
          found => {
            
            if(!found || found === null) {
              found = new Specimen();
            }

            if(!found.patient || found.patient === null) {
              found.patient = new Patient();
            } else {
              if(!found.patient.patient_first_name) {
                found.patient.patient_first_name = '';
              }

              if(!found.patient.patient_surname) {
                found.patient.patient_surname = '';
              }

              if(!found.patient.identity_no) {
                found.patient.identity_no = '';
              }
            }

            (<FormGroup>control).setControl('patient', this.formBuilder.group(found.patient));
            (<FormGroup>control).setControl('id', this.formBuilder.control(found.id));
            //(<FormGroup>control).setControl('program', this.formBuilder.control(found.));

            if(found.authorizer_datetime && !this.batchForm.value.authorisingDateTime) {
              console.log(new Date(found.authorizer_datetime).toLocaleString());
              
              this.batchForm.setControl('authorisingDateTime', this.formBuilder.control(formatDate(found.authorizer_datetime, 'yyyy-MM-ddTHH:mm', 'en-US')))
            }

            if(found.authorizer_personnel && !this.batchForm.value.authorisingPersonnel) {
              this.batchForm.setControl('authorisingPersonnel', this.formBuilder.control(found.authorizer_personnel))
            }
            
          }
        );

      }
    );
  }

  toRawBatch(batch: Batch): Array<RedcapData> {

    const raw: Array<RedcapData> = [];

    let val: RedcapData;
    if(batch.lab) {
      val = new RedcapData()
      val['field_name'] = 'test_det_id';
      val['project_id'] = 345;
      val['record'] = batch.batchId;
      val['value'] = batch.batchId;
      raw.push(val);
      
      if(this.page === 'detection') {
        val = new RedcapData()
        val['field_name'] = 'test_det_batch_id';
        val['project_id'] = 345;
        val['record'] = batch.batchId;
        val['value'] = batch.batchId;
        raw.push(val);
      }

      if(this.page === 'resulting') {
      val = new RedcapData()
      val['field_name'] = 'test_assay_batch_id';
      val['project_id'] = 345;
      val['record'] = batch.batchId;
      val['value'] = batch.batchId;
      raw.push(val);
      }

      if(this.page === 'verification') {
        val = new RedcapData()
        val['field_name'] = 'test_verify_batch_id';
        val['project_id'] = 345;
        val['record'] = batch.batchId;
        val['value'] = batch.batchId;
        raw.push(val);
      }
    }

    /// Detection
    if(this.page === 'detection') {
      if(batch.lab) {
        val = new RedcapData()
        val['field_name'] = 'detection_lab';
        val['project_id'] = 345;
        val['record'] = batch.batchId;
        val['value'] = batch.lab.code;
        raw.push(val);
      }

      if(batch.instrument) {
        val = new RedcapData()
        val['field_name'] = 'test_det_instrument';
        val['project_id'] = 345;
        val['record'] = batch.batchId;
        val['value'] = batch.instrument.code;
        raw.push(val);
      }
      
      if(batch.detectionPersonnel) {
        val = new RedcapData()
        val['field_name'] = 'test_det_personnel';
        val['project_id'] = 345;
        val['record'] = batch.batchId;
        val['value'] = batch.detectionPersonnel;
        raw.push(val);
      }
      
      if((batch.detectionDateTime)) {
        val = new RedcapData()
        val['field_name'] = 'test_det_datetime';
        val['project_id'] = 345;
        val['record'] = batch.batchId;
        val['value'] = formatDate(new Date(batch.detectionDateTime), 'yyyy-MM-dd HH:mm', 'en-US');
        raw.push(val);
      }
      
      if(batch.detectionStatus) {
        val = new RedcapData()
        val['field_name'] = 'testing_detection_complete';
        val['project_id'] = 345;
        val['record'] = batch.batchId;
        val['value'] = batch.detectionStatus;
        raw.push(val);
      }
    }
    
    if(batch.instrumentBatchSize) {
      if(this.page === 'detection') {
        val = new RedcapData()
        val['field_name'] = 'test_det_batchsize';
        val['project_id'] = 345;
        val['record'] = batch.batchId;
        val['value'] = ''+batch.instrumentBatchSize;
        raw.push(val);
      }

      if(this.page === 'verification') {
        val = new RedcapData()
        val['field_name'] = 'test_verify_batchsize';
        val['project_id'] = 345;
        val['record'] = batch.batchId;
        val['value'] = ''+batch.instrumentBatchSize;
        raw.push(val);
      }

      if(this.page === 'resulting') {
        val = new RedcapData()
        val['field_name'] = 'test_assay_batchsize';
        val['project_id'] = 345;
        val['record'] = batch.batchId;
        val['value'] = ''+batch.instrumentBatchSize;
        raw.push(val);
      }
    }

    /// Resulting
    if(this.page === 'resulting') {
      if(batch.resultingPersonnel) {
        val = new RedcapData()
        val['field_name'] = 'test_assay_personnel';
        val['project_id'] = 345;
        val['record'] = batch.batchId;
        val['value'] = ''+batch.resultingPersonnel;
        raw.push(val);
      }

      if(batch.resultingDateTime) {
        val = new RedcapData()
        val['field_name'] = 'test_assay_datetime';
        val['project_id'] = 345;
        val['record'] = batch.batchId;
        val['value'] = formatDate(new Date(batch.resultingDateTime), 'yyyy-MM-dd HH:mm', 'en-US');
        raw.push(val);
      }

      if(batch.resultingStatus) {
        val = new RedcapData()
        val['field_name'] = 'resulting_complete';
        val['project_id'] = 345;
        val['record'] = batch.batchId;
        val['value'] = batch.resultingStatus;
        raw.push(val);
      }
    }

    /// Verification
    if(this.page === 'verification') {
      if(batch.verificationPersonnel) {
        val = new RedcapData()
        val['field_name'] = 'test_verify_personnel';
        val['project_id'] = 345;
        val['record'] = batch.batchId;
        val['value'] = batch.verificationPersonnel;
        raw.push(val);
      }

      if(batch.verificationDateTime) {
        val = new RedcapData()
        val['field_name'] = 'test_verify_datetime';
        val['project_id'] = 345;
        val['record'] = batch.batchId;
        val['value'] = formatDate(new Date(batch.verificationDateTime), 'yyyy-MM-dd HH:mm', 'en-US');
        raw.push(val);
      }

      if(batch.verificationStatus) {
        val = new RedcapData()
        val['field_name'] = 'verification_complete';
        val['project_id'] = 345;
        val['record'] = batch.batchId;
        val['value'] = batch.verificationStatus;
        raw.push(val);
      }
    }

    /// Specimen
    batch.batchItems.forEach(
      specimen => {

        let pos = this.specimenService.decodePosition(specimen.position);

        val = new RedcapData()
        val['field_name'] = 'test_det_barcode_' + pos;
        val['project_id'] = 345;
        val['record'] = batch.batchId;
        val['value'] = specimen.specimen_barcode;
        raw.push(val);

          if(specimen.testAssayResults) {
            val = new RedcapData()
            val['field_name'] = 'test_assay_result_' + pos;
            val['project_id'] = 345;
            val['record'] = batch.batchId;
            val['value'] = specimen.testAssayResults;
            raw.push(val);
          }

          if(specimen.testVerifyResults) {
            val = new RedcapData()
            val['field_name'] = 'test_verify_result_' + pos;
            val['project_id'] = 345;
            val['record'] = batch.batchId;
            val['value'] = specimen.testVerifyResults;
            raw.push(val);
          }

          if(specimen.covidRnaResults) {
            val = new RedcapData()
            val['field_name'] = 'covid_rna_results' + pos;
            val['project_id'] = 345;
            val['record'] = batch.batchId;
            val['value'] = specimen.covidRnaResults;
            raw.push(val);
          }
        
      }
    );

    return raw;
  }

  createReport() {
    this.printing = true;
    const batch: Batch = this.batchForm.value;

    batch.batchItems.forEach((element) => {

      if(element.specimen_barcode == 'C0000000001') {
        element.specimen_barcode = '1'
      } else if(element.specimen_barcode == 'C0000000002') {
        element.specimen_barcode = '2'
      }
    });

    this.redcaDataService.createReport(batch).subscribe(
      (data: Blob) => {
        const file = new Blob([data], { type: 'application/pdf' });
        const downloadURL = window.URL.createObjectURL(file);
        const link = document.createElement('a');
        link.href = downloadURL;
        link.download = batch.batchId + ".pdf";
        link.click();
        this.printing = false;
      },
      (error) => {
        console.log('getPDF error: ',error);
        this.printing = false;
      }
    );
  }

  isControl(barcode) {
    return barcode === 'C0000000001' || barcode === 'C0000000002';
  }
}
