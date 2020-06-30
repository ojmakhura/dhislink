import { prop } from '@rxweb/reactive-form-validators';

export class Patient {

    @prop()
    id: number = 0;

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
