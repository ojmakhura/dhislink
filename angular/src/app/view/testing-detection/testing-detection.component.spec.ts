import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TestingDetectionComponent } from './testing-detection.component';

describe('TestingDetectionComponent', () => {
  let component: TestingDetectionComponent;
  let fixture: ComponentFixture<TestingDetectionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TestingDetectionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestingDetectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
