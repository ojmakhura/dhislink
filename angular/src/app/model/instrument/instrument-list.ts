import { Instrument } from './instrument';

export class InstrumentList {

    static allIntruments(): Instrument[] {
        return [
            new Instrument('122', 'NHL ROCHE Z480 (122)'),
            new Instrument('221', 'BHHRL ABI 7500 S/N 750S8180106 (221)'),
            new Instrument('222', 'BHHRL m2000rt S/N 275020775 (222)'),
            new Instrument('321', 'UB ABI 7500 (321)'),
            new Instrument('421', 'BNVL ABI 7500 FAST (421)'),
            new Instrument('521', 'BVI ABI 7500 (521)'),
            new Instrument('999', 'Other detection machine(specify) (999))')
        ];
    }

    static getInstrument(code: string): Instrument {
        return InstrumentList.allIntruments().find(inst => inst.code == code);
    }
}
