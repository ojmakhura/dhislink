import { TestBed } from '@angular/core/testing';

import { RedcapDataService } from './redcap-data.service';

describe('RedcapDataService', () => {
  let service: RedcapDataService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RedcapDataService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
