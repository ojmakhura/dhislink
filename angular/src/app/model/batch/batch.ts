import { LocationVO } from '../location/location-vo';
import { Instrument } from '../instrument/instrument';
import { Specimen } from '../specimen/specimen';
import {propObject, propArray, prop, disable, model} from '@rxweb/reactive-form-validators';
import { AbstractControl } from '@angular/forms';

@model([])
export class Batch {
    @prop()
    batchId: string;

    @prop()
    projectId: number;

    @prop()
    page: string;

    @prop()
    lab: LocationVO;

    @prop()
    detectionPersonnel: string;

    @prop()
    resultingPersonnel: string;

    @prop()
    verificationPersonnel: string;

    @prop()
    detectionDateTime: Date;

    @prop()
    resultingDateTime: Date;

    @prop()
    verificationDateTime: Date;

    @prop()
    detectionSize: number;

    @prop()
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
    authorisingDateTime: Date;

    @prop()
    publishResults: boolean;

    constructor() {
        this.batchId = '';
        this.projectId = 0,
        this.page = '';
        this.detectionPersonnel = '';
        this.resultingPersonnel = '';
        this.verificationPersonnel = '';
        this.detectionDateTime = null;
        this.resultingDateTime = null;
        this.verificationDateTime = null;
        this.detectionSize = 0;
        this.instrumentBatchSize = 0;
        this.detectionStatus = '';
        this.resultingStatus = '';
        this.verificationStatus = '';
        this.detectionBatchId = '';
        this.assayBatchId = '';
        this.verifyBatchId = '';
        this.authorisingPersonnel = '';
        this.authorisingDateTime = null;
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
