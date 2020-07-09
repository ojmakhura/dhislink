import { Injectable } from '@angular/core';
import { prop, required } from '@rxweb/reactive-form-validators';

@Injectable()
export class RedcapAuth {

    @prop()
    @required()
    username: string;

    @prop()
    @required()
    password: string;
}
