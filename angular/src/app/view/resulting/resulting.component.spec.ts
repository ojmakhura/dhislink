import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ResultingComponent } from './resulting.component';

describe('ResultingComponent', () => {
  let component: ResultingComponent;
  let fixture: ComponentFixture<ResultingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ResultingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ResultingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
