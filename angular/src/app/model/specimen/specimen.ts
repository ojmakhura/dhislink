import { Patient } from '../patient/patient';
import { FormGroup, FormControl, Validators } from '@angular/forms';

export class Specimen {
    id: number;
    batch_number: string;
    date_dispatched: Date;
    dispatcher: string;
    location_of_dispatch: string;
    date_specimen_collected: Date;
    gis_lat: string;
    gis_long: string;
    specimen_barcode: string;
    outcome: string;
    receiving_personnel: string;
    receiving_condition_code: string;
    sample_status_dispatch: string;
    symptom: string;
    received_datetime: Date;
    specimen_type: string;
    time_dispatched: Date;
    patient: Patient;
    testType: string;
    riskFactors: string;
    test_assay_personnel: string;
    test_assay_datetime: Date;
    test_verify_personnel: string;
    test_verify_datetime: Date;
    authorizer_personnel: string;
    authorizer_datetime: Date;
    notes: string;
    dispatcher_contact: string;
    dispatcher_email: string;
    dispatcher_city: string;
    lastUpdated: Date;
    created: Date;
    event: string;
    dhis2Synched: boolean  = false;
    receiving_lab: string;
    patient_facility: string;
    covid_number: string;
    testAssayResults: string;
    testVerifyResults: string;
    covidRnaResults: string;
    results: string;
    position: string;
}

export class DhislinkCode {

    constructor(code: string, description: string) {

    }
}

