import { BatchItem } from './batch-item';

export class InstrumentBatch {
    instrument: string;
    instrumentBatchSize: number = 0;
    instrumentItems: BatchItem[];
}
