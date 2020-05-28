import { InstrumentBatch } from './instrument-batch';
import { LocationVO } from '../location/location-vo';

export class Batch {
    batchId: string;
    lab: LocationVO;
    detectionPersonnel: string;
    resutingPersonel: string;
    verificationPersonel: string; 
    detectionDateTime: Date;
    resultingDateTime: Date;
    verificationDateTime: Date;
    detectionSize: number;
    detectionBatch1: InstrumentBatch;
    detectionBatch2: InstrumentBatch;
    detectionBatch3: InstrumentBatch;
    detectionBatch4: InstrumentBatch;
    detectionStatus: string;
    resultingStatus: string;
    verificationStatus: string;

    constructor() {
        this.lab = new LocationVO();
        this.detectionBatch1 = new InstrumentBatch();
        this.detectionBatch2 = new InstrumentBatch();
        this.detectionBatch3 = new InstrumentBatch();
        this.detectionBatch4 = new InstrumentBatch();
    }

}
