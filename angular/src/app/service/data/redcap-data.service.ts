import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BatchSearchCriteria } from 'src/app/model/batch/batch-search-criteria';
import { DataSearchCriteria } from 'src/app/model/data/data-search-criteria';

@Injectable({
  providedIn: 'root'
})
export class RedcapDataService {

  private url= 'http://localhost:8080/ddpcontroller/data/';

  constructor(private http: HttpClient) { }

  search(criteria: BatchSearchCriteria) {
    DataSearchCriteria dsc = new BatchSearchCriteria();
    
  }
}
