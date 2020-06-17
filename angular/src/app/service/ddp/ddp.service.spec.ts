import { TestBed } from '@angular/core/testing';

import { DdpService } from './ddp.service';

describe('DdpService', () => {
  let service: DdpService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DdpService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
