import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LocationVO } from 'src/app/model/location/location-vo';
import { BASE_URL } from 'src/app/helpers/dhis-link-constants';

@Injectable({
  providedIn: 'root'
})
export class LocationService {

  private url= BASE_URL + 'location/';

  constructor(private http: HttpClient) { }

  createLocation(locationVO: LocationVO ): Observable<LocationVO> {
    
    return this.http.post<LocationVO>(this.url + 'new', locationVO);
  }

  updateLocation(locationVO: LocationVO ) {
    this.http.post<LocationVO>(this.url + 'new', locationVO);
  }

  deleteLocation(id: number): Observable<void> {
    return this.http.delete<void>(this.url + 'delete/' + id);
  }

  findById(id: number): Observable<LocationVO> {
    return this.http.get<LocationVO>(this.url + id);
  }

  searchByName(name: string) : Observable<LocationVO[]> {

    return this.http.get<LocationVO[]>(this.url + 'name/' + name);
  }

  searchByCode(code: string) : Observable<LocationVO> {

    return this.http.get<LocationVO>(this.url + 'code/' + code);
  }

  findAll(): Observable<LocationVO[]> {
    return this.http.get<LocationVO[]>(this.url + 'all');
  }


}
