import { InstrumentBatch } from './instrument-batch';

export class Batch {
    batchId: string;
    lab: string;
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

}
