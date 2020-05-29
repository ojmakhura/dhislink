import { InstrumentBatch } from './instrument-batch';
import { LocationVO } from '../location/location-vo';
import { Instrument } from '../instrument/instrument';
import { Specimen } from '../specimen/specimen';

export class Batch {
    batchId: string;
    lab: LocationVO;
    detectionPersonnel: string;
    resultingPersonnel: string;
    verificationPersonnel: string; 
    detectionDateTime: string;
    resultingDateTime: string;
    verificationDateTime: string;
    detectionSize: number;
    instrument: Instrument;
    instrumentBatchSize: number = 0;
    batchItems: Specimen[];
    detectionStatus: string;
    resultingStatus: string;
    verificationStatus: string;

    constructor() {
        this.lab = new LocationVO();
        this.batchItems = [];
        this.instrument = new Instrument('', '');
    }

}
