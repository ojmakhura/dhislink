import { Component, OnInit, Injector, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from 'src/app/service/authentication/authentication.service';
import { LocationService } from 'src/app/service/location/location.service';
import { RedcapDataService } from 'src/app/service/data/redcap-data.service';
import { SpecimenService } from 'src/app/service/specimen/specimen.service';
import { RxFormBuilder } from '@rxweb/reactive-form-validators';
import { Specimen } from 'src/app/model/specimen/specimen';
import { FormGroup } from '@angular/forms';

@Component({
  selector: 'app-lab-report',
  templateUrl: './lab-report.component.html',
  styleUrls: ['./lab-report.component.css']
})
export class LabReportComponent implements OnInit {

  specimen: Specimen;
  protected router: Router;
  protected formBuilder: RxFormBuilder;

  specimenForm: FormGroup;

  constructor(injector: Injector) {
    this.router = injector.get(Router);
    this.formBuilder = injector.get(RxFormBuilder);
  }

  ngOnInit(): void {
    this.specimenForm = this.formBuilder.group(new Specimen());
  }

}
