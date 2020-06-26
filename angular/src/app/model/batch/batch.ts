import { LocationVO } from '../location/location-vo';
import { Instrument } from '../instrument/instrument';
import { Specimen } from '../specimen/specimen';
import {propObject, propArray, prop} from '@rxweb/reactive-form-validators';

export class Batch {
    @prop()
    batchId: string;

    @prop()
    projectId: number;
    
    @prop()
    page: string;

    @propObject(LocationVO)
    lab: LocationVO;
    
    @prop()
    detectionPersonnel: string;
    
    @prop()
    resultingPersonnel: string;
    
    @prop()
    verificationPersonnel: string;
    
    @prop()
    detectionDateTime: string;
    
    @prop()
    resultingDateTime: string;
    
    @prop()
    verificationDateTime: string;
    
    @prop()
    detectionSize: number;
    
    @propObject(Instrument)
    instrument: Instrument;
    
    @prop()
    instrumentBatchSize: number = 0;

    @propArray(Specimen)
    batchItems: Array<Specimen>;

    @prop()
    detectionStatus: string;

    @prop()
    resultingStatus: string;

    @prop()
    verificationStatus: string;

    @prop()
    detectionBatchId: string;

    @prop()
    assayBatchId: string;

    @prop()
    verifyBatchId: string;

    @prop()
    authorisingPersonnel: string;

    @prop()
    authorisingDateTime: string;

    @prop()
    publishResults: boolean;

    constructor() {
        this.batchId = '';
        this.projectId = 0,
        this.page = '';
        this.detectionPersonnel = '';
        this.resultingPersonnel = '';
        this.verificationPersonnel = '';
        this.detectionDateTime = '';
        this.resultingDateTime = '';
        this.verificationDateTime = '';
        this.detectionSize = 0;
        this.instrumentBatchSize = 0;
        this.detectionStatus = '';
        this.resultingStatus = '';
        this.verificationStatus = '';
        this.detectionBatchId = '';
        this.assayBatchId = '';
        this.verifyBatchId = '';
        this.authorisingPersonnel = '';
        this.authorisingDateTime = '';
        this.publishResults = false;
        this.lab = new LocationVO();
        this.batchItems = [];
        this.instrument = new Instrument('', '');
    }

}

export class StatusItem {
    code: string;
    description: string;
}
