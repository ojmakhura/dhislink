import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BatchSearchCriteria } from 'src/app/model/batch/batch-search-criteria';
import { DataSearchCriteria } from 'src/app/model/data/data-search-criteria';
import { Observable, of } from 'rxjs';
import { Batch } from 'src/app/model/batch/batch';
import { Specimen } from 'src/app/model/specimen/specimen';
import { RedcapData } from 'src/app/model/data/redcap-data';
import { BASE_URL } from 'src/app/helpers/dhis-link-constants';
import { FormArray, FormGroup } from '@angular/forms';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class RedcapDataService {

  private url = BASE_URL + 'data/';
  constructor(private http: HttpClient) { }

  search(criteria: BatchSearchCriteria): Observable<Batch[]> {

    return this.http.post<Batch[]>(this.url + 'search/batch', criteria);
  }

  fetchExtractionSpecimen(batchId: string): Observable<Specimen[]> {

    return this.http.get<Specimen[]>(this.url + 'extraction/specimen/' + batchId);
  }

  fetchBatchSpecimen(criteria: DataSearchCriteria): Observable<Specimen[]> {

    return this.http.post<Specimen[]>(this.url + 'batch/specimen', criteria);
  }

  saveBatch(batch: Batch): Observable<Specimen[]> {

    if (batch.authorisingDateTime === '') {
      batch.authorisingDateTime = null;
    }

    if (batch.authorisingPersonnel === '') {
      batch.authorisingPersonnel = null;
    }

    if (batch.resultingDateTime === '') {
      batch.resultingDateTime = null;
    }

    if (batch.resultingPersonnel === '') {
      batch.resultingPersonnel = null;
    }

    if (batch.verificationDateTime === '') {
      batch.verificationDateTime = null;
    }

    if (batch.verificationPersonnel === '') {
      batch.verificationPersonnel = null;
    }

    //return of([]);
    return this.http.post<Specimen[]>(this.url + 'savebatch', batch);
  }

  pullSpecimenInfo(specimens: Specimen): Observable<Specimen[]> {
    return this.http.post<Specimen[]>(this.url + 'pullspecimen', specimens);
  }

  getDescription(code: string): string {
    let description = '';

    if (code === '1') {
      description = 'Positive';
    } else if (code === '2') {
      description = 'Negative';
    }

    return description;
  }

}
