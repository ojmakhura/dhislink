import { Patient } from '../patient/patient';
import { prop, propObject } from '@rxweb/reactive-form-validators';

export class Specimen {
    @prop()
    id = 0;

    @prop()
    batch_number: string = '';

    @prop()
    date_dispatched: Date;

    @prop()
    dispatcher: string = '';

    @prop()
    location_of_dispatch: string = '';

    @prop()
    date_specimen_collected: Date;

    @prop()
    gis_lat: string = '';

    @prop()
    gis_long: string = '';

    @prop()
    specimen_barcode: string = '';

    @prop()
    outcome: string = '';

    @prop()
    receiving_personnel: string = '';

    @prop()
    receiving_condition_code: string = '';

    @prop()
    sample_status_dispatch: string = '';

    @prop()
    symptom: string = '';

    @prop()
    received_datetime: Date;

    @prop()
    specimen_type: string = '';

    @prop()
    time_dispatched: Date;

    @propObject(Patient)
    patient: Patient;

    @prop()
    testType: string = '';

    @prop()
    riskFactors: string = '';

    @prop()
    test_assay_personnel: string = '';

    @prop()
    test_assay_datetime: Date;

    @prop()
    test_verify_personnel: string = '';

    @prop()
    test_verify_datetime: Date;

    @prop()
    authorizer_personnel: string = '';

    @prop()
    authorizer_datetime: Date;

    @prop()
    notes: string = '';

    @prop()
    dispatcher_contact: string = '';

    @prop()
    dispatcher_email: string = '';

    @prop()
    dispatcher_city: string = '';

    @prop()
    lastUpdated: Date;

    @prop()
    created: Date;

    @prop()
    event: string = '';

    @prop()
    dhis2Synched: boolean  = false;

    @prop()
    receiving_lab: string = '';

    @prop()
    patient_facility: string = '';

    @prop()
    covid_number: string = '';

    @prop()
    testAssayResults: string = '';

    @prop()
    testVerifyResults: string = '';

    @prop()
    covidRnaResults: string = '';

    @prop()
    results: string = '';

    @prop()
    position: string = '';
}
