import { Instrument } from '../instrument/instrument';
import { Specimen } from '../specimen/specimen';

export class InstrumentBatch {
    instrument: Instrument;
    instrumentBatchSize: number = 0;
    batchItems: Specimen[];

    constructor() {
        this.batchItems = [];
        this.instrument = new Instrument('', '');
    }
}
