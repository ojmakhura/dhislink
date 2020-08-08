import { BatchAuthorityStage } from './BatchAuthorisationStage';

export class BatchSearchCriteria {
    batchId: string;
    specimenBarcode: string;
    lab: string;
    includeSpecimen = true;
    page: BatchAuthorityStage;
}
