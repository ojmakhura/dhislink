import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Specimen } from 'src/app/model/specimen/specimen';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class SpecimenService {

  private url= 'https://ehealth.ub.bw:8080/ddpcontroller/specimen/';
  
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

    encodePosition(i: number): string {

      i = i + 1;
      var q: number = Math.floor(i / 12);
      var r: number = i % 12;
      let position = '';

      if(r === 0) {
        r = 12;
        q--;
      }

      if(q === 0) {
        position = 'A' + r;
      } else if(q === 1) {
        position = 'B' + r;
      } else if(q === 2) {
        position = 'C' + r;
      } else if(q === 3) {
        position = 'D' + r;
      } else if(q === 4) {
        position = 'E' + r;
      } else if(q === 5) {
        position = 'F' + r;
      } else if(q === 6) {
        position = 'G' + r;
      } else if(q === 7) {
        position = 'H' + r;
      }

      return position;
    }
}
