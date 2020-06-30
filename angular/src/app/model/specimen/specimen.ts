import { Patient } from '../patient/patient';
import { prop, propObject, model } from '@rxweb/reactive-form-validators';

@model([])
export class Specimen {
    @prop()
    id: number;

    @prop()
    batch_number = '';

    @prop()
    date_dispatched: Date;

    @prop()
    dispatcher = '';

    @prop()
    location_of_dispatch = '';

    @prop()
    date_specimen_collected: Date;

    @prop()
    gis_lat = '';

    @prop()
    gis_long = '';

    @prop()
    specimen_barcode = '';

    @prop()
    outcome = '';

    @prop()
    receiving_personnel = '';

    @prop()
    receiving_condition_code = '';

    @prop()
    sample_status_dispatch = '';

    @prop()
    symptom = '';

    @prop()
    received_datetime: Date;

    @prop()
    specimen_type = '';

    @prop()
    time_dispatched: Date;

    @propObject(Patient)
    patient: Patient;

    @prop()
    testType = '';

    @prop()
    riskFactors = '';

    @prop()
    test_assay_personnel = '';

    @prop()
    test_assay_datetime: Date;

    @prop()
    test_verify_personnel = '';

    @prop()
    test_verify_datetime: Date;

    @prop()
    authorizer_personnel = '';

    @prop()
    authorizer_datetime: Date;

    @prop()
    notes = '';

    @prop()
    dispatcher_contact = '';

    @prop()
    dispatcher_email = '';

    @prop()
    dispatcher_city = '';

    @prop()
    lastUpdated: Date;

    @prop()
    created: Date;

    @prop()
    event = '';

    @prop()
    dhis2Synched  = false;

    @prop()
    receiving_lab = '';

    @prop()
    patient_facility = '';

    @prop()
    covid_number = '';

    @prop()
    testAssayResults = '';

    @prop()
    testVerifyResults = '';

    @prop()
    covidRnaResults = '';

    @prop()
    results = '';

    @prop()
    position = '';
}
