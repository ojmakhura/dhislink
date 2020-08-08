// license-header java merge-point
/**
 * This is only generated once! It will never be overwritten.
 * You can (and have to!) safely modify it by hand.
 * TEMPLATE:    SpringServiceImpl.vsl in andromda-spring cartridge
 * MODEL CLASS: NewModel::bw.ub.ehealth.dhislink::instrument::service::InstrumenService
 * STEREOTYPE:  Service
 */
package bw.ub.ehealth.dhislink.instrument.service;

import bw.ub.ehealth.dhislink.instrument.vo.InstrumentVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @see bw.ub.ehealth.dhislink.instrument.service.InstrumenService
 */
 @Transactional(propagation = Propagation.REQUIRED, readOnly=false)
@Service("instrumenService")
public class InstrumenServiceImpl
    extends InstrumenServiceBase
{

    /**
     * @see bw.ub.ehealth.dhislink.instrument.service.InstrumenService#findById(Long)
     */
    @Override
    protected  InstrumentVO handleFindById(Long id)
        throws Exception
    {
        // TODO implement protected  InstrumentVO handleFindById(Long id)
        throw new UnsupportedOperationException("bw.ub.ehealth.dhislink.instrument.service.InstrumenService.handleFindById(Long id) Not implemented!");
    }

    /**
     * @see bw.ub.ehealth.dhislink.instrument.service.InstrumenService#saveInstrument(InstrumentVO)
     */
    @Override
    protected  InstrumentVO handleSaveInstrument(InstrumentVO instrumentVO)
        throws Exception
    {
        // TODO implement protected  InstrumentVO handleSaveInstrument(InstrumentVO instrumentVO)
        throw new UnsupportedOperationException("bw.ub.ehealth.dhislink.instrument.service.InstrumenService.handleSaveInstrument(InstrumentVO instrumentVO) Not implemented!");
    }

    /**
     * @see bw.ub.ehealth.dhislink.instrument.service.InstrumenService#removeInstrument(Long)
     */
    @Override
    protected  Boolean handleRemoveInstrument(Long id)
        throws Exception
    {
        // TODO implement protected  Boolean handleRemoveInstrument(Long id)
        throw new UnsupportedOperationException("bw.ub.ehealth.dhislink.instrument.service.InstrumenService.handleRemoveInstrument(Long id) Not implemented!");
    }

	@Override
	protected InstrumentVO handleFindByCode(String code) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}