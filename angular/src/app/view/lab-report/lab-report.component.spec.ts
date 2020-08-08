import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LabReportComponent } from './lab-report.component';

describe('LabReportComponent', () => {
  let component: LabReportComponent;
  let fixture: ComponentFixture<LabReportComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LabReportComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LabReportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
