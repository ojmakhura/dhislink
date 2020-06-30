import { prop, model } from '@rxweb/reactive-form-validators';

@model([])
export class Patient {

    @prop()
    id;

    @prop()
    identity_no = '';

    @prop()
    patient_first_name = '';

    @prop()
    patient_surname = '';

    @prop()
    patient_contact = '';

    @prop()
    date_birth: Date;

    @prop()
    sex = '';

    @prop()
    plotNo = '';

    @prop()
    transportRegistration = '';

    @prop()
    travelDestination = '';

    @prop()
    countryDeparture = '';

    @prop()
    nextOfKin = '';

    @prop()
    city = '';

    @prop()
    departureDate: Date;

    @prop()
    kinContact = '';

    @prop()
    nationality = '';

    @prop()
    lastUpdated: Date;

    @prop()
    created: Date;

    @prop()
    trackedEntityInstance = '';
}
