import { prop } from '@rxweb/reactive-form-validators';

export class LocationVO {

    @prop()
    code: string;

    @prop()
    name: string ;

    @prop()
    description: string;

    @prop()
    id: number;

    constructor() {
        this.code = '';
        this.name = '';
        this.description = '';
        this.id = 0;
    }
}
