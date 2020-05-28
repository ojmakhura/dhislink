import { TestBed } from '@angular/core/testing';

import { SpecimenService } from './specimen.service';

describe('SpecimenService', () => {
  let service: SpecimenService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SpecimenService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
