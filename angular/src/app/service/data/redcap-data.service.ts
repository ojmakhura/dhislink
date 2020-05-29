import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BatchSearchCriteria } from 'src/app/model/batch/batch-search-criteria';
import { DataSearchCriteria } from 'src/app/model/data/data-search-criteria';
import { Observable } from 'rxjs';
import { Batch } from 'src/app/model/batch/batch';
import { Specimen } from 'src/app/model/specimen/specimen';

@Injectable({
  providedIn: 'root'
})
export class RedcapDataService {

  private url= 'http://localhost:8080/ddpcontroller/data/';

  constructor(private http: HttpClient) { }

  search(criteria: BatchSearchCriteria): Observable<Batch[]> {
   
    return this.http.post<Batch[]>(this.url + 'search/batch', criteria);
    
  }

  fetchExtractionSpecimen(batchId: string): Observable<Specimen[]> {

    return this.http.get<Specimen[]>(this.url + 'extraction/specimen/' + batchId);
    
  }
}
