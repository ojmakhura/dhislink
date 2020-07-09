import { prop, model } from '@rxweb/reactive-form-validators';

@model([])
export class UserDetails {
    @prop()
    username = '';

    @prop()
    password = '';
}
