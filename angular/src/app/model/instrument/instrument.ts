import { prop, model } from '@rxweb/reactive-form-validators';

@model([])
export class Instrument {
    @prop()
    code: string;
    @prop()
    name: string;
    constructor(code: string, name: string) {
        this.code = code;
        this.name = name;
    }
}
