import { LocationVO } from '../location/location-vo';
import { Instrument } from '../instrument/instrument';
import { Specimen } from '../specimen/specimen';

export class Batch {
    batchId: string;
    projectId: number;
    page: string;
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
    detectionBatchId: string;
    assayBatchId: string;
    verifyBatchId: string;
    authorisingPersonnel: string; 
    authorisingDateTime: string;
    publishResults: boolean;

    constructor() {
        this.lab = new LocationVO();
        this.batchItems = [];
        this.instrument = new Instrument('', '');
    }

}

export class StatusItem {
    code: string;
    description: string;
}
