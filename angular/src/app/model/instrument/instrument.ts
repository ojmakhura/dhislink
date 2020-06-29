import { prop } from '@rxweb/reactive-form-validators';

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
