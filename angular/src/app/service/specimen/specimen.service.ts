import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Specimen } from 'src/app/model/specimen/specimen';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class SpecimenService {

  private url= 'http://localhost:8080/ddpcontroller/specimen/';
  
  constructor(private http: HttpClient) { }

  /**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.specimen.service.SpecimenService.findSpecimenByBarcode
     * @param barcode TODO: Model Documentation for
bw.ub.ehealth.dhislink.specimen.service.SpecimenService.findSpecimenByBarcode(barcode)
     * @return SpecimenVO
     */
    findSpecimenByBarcode(barcode: string): Observable<Specimen> {
      return this.http.get<Specimen>(this.url + 'barcode/' + barcode);
    }

    /**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.specimen.service.SpecimenService.deleteSpecimen
     * @param id TODO: Model Documentation for
bw.ub.ehealth.dhislink.specimen.service.SpecimenService.deleteSpecimen(id)
     */
    deleteSpecimen(id: number): Observable<void> {
      return this.http.delete<void>(this.url + 'delete/' + id);
    }

    /**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.specimen.service.SpecimenService.saveSpecimen
     * @param specimenVO TODO: Model Documentation for
bw.ub.ehealth.dhislink.specimen.service.SpecimenService.saveSpecimen(specimenVO)
     * @return SpecimenVO
     */
    saveSpecimen(specimenVO: Specimen): Observable<Specimen> {
      return this.http.post<Specimen>(this.url, specimenVO);
    }

    /**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.specimen.service.SpecimenService.findByEvent
     * @param event TODO: Model Documentation for
bw.ub.ehealth.dhislink.specimen.service.SpecimenService.findByEvent(event)
     * @return SpecimenVO
     */
    findByEvent(event: string): Observable<Specimen> {
      return this.http.get<Specimen>(this.url + 'event/' + event);
    }

    /**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.specimen.service.SpecimenService.findLatestSpecimen
     * @return SpecimenVO
     */
    findLatestSpecimen(): Observable<Specimen> {
      return this.http.get<Specimen>(this.url + 'latest');
    }

    /**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.specimen.service.SpecimenService.findUnsynchedSpecimen
     * @return Collection<SpecimenVO>
     */
    findUnsynchedSpecimen(): Observable<Specimen[]> {
      return this.http.get<Specimen[]>(this.url + 'unsynched');
    }
}
