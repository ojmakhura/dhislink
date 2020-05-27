import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LocationVO } from 'src/app/model/location/location-vo';

@Injectable({
  providedIn: 'root'
})
export class LocationService {

  private url= 'http://localhost:8080/ddpcontroller/location/';

  constructor(private http: HttpClient) { }

  createLocation(locationVO: LocationVO ): Observable<LocationVO> {
    console.log(locationVO);
    return this.http.post<LocationVO>(this.url + 'new', locationVO);
  }

  updateLocation(locationVO: LocationVO ): Observable<void> {
    return this.http.put<void>(this.url + 'update', locationVO);
  }

  deleteLocation(id: number): Observable<void> {
    return this.http.delete<void>(this.url + 'delete/' + id);
  }

  findById(id: number): Observable<LocationVO> {
    return this.http.get<LocationVO>(this.url + id);
  }

  searchByName(name: string) : Observable<LocationVO[]> {

    return this.http.get<LocationVO[]>(this.url + 'search/' + name);
  }

  findAll(): Observable<LocationVO[]> {
    return this.http.get<LocationVO[]>(this.url + 'all');
  }


}
