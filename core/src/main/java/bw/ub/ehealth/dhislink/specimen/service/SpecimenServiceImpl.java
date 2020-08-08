// license-header java merge-point
/**
 * This is only generated once! It will never be overwritten.
 * You can (and have to!) safely modify it by hand.
 * TEMPLATE:    SpringServiceImpl.vsl in andromda-spring cartridge
 * MODEL CLASS: NewModel::bw.ub.ehealth.dhislink::specimen::service::SpecimenService
 * STEREOTYPE:  Service
 */
package bw.ub.ehealth.dhislink.specimen.service;

import bw.ub.ehealth.dhislink.specimen.Specimen;
import bw.ub.ehealth.dhislink.specimen.vo.SpecimenVO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @see bw.ub.ehealth.dhislink.specimen.service.SpecimenService
 */
@Transactional(propagation = Propagation.REQUIRED, readOnly=false)
@Service("specimenService")
public class SpecimenServiceImpl
    extends SpecimenServiceBase
{

    /**
     * @see bw.ub.ehealth.dhislink.specimen.service.SpecimenService#findSpecimenByBarcode(String)
     */
    @Override
    protected  SpecimenVO handleFindSpecimenByBarcode(String barcode)
        throws Exception
    {
    	
    	return getSpecimenDao().toSpecimenVO(getSpecimenDao().findByBarcode(barcode));
    }

    /**
     * @see bw.ub.ehealth.dhislink.specimen.service.SpecimenService#deleteSpecimen(Long)
     */
    @Override
    protected  void handleDeleteSpecimen(Long id)
        throws Exception
    {
    	getSpecimenDao().remove(id);
    }

    /**
     * @see bw.ub.ehealth.dhislink.specimen.service.SpecimenService#saveSpecimen(String)
     */
    @Override
    protected SpecimenVO handleSaveSpecimen(SpecimenVO specimenVO)
        throws Exception
    {
    	Specimen specimen = getSpecimenDao().createOrUpdate(getSpecimenDao().specimenVOToEntity(specimenVO));
    	
    	if(specimen.getPatient() != null && StringUtils.isBlank(specimen.getPatient().getIdentityNo())) {
    		specimen.setPatient(null);
    	}
    	
    	return getSpecimenDao().toSpecimenVO(specimen);
    }

	@Override
	protected SpecimenVO handleFindByEvent(String event) throws Exception {
		
		return getSpecimenDao().toSpecimenVO(getSpecimenDao().findByEvent(event));
	}

	@Override
	protected SpecimenVO handleFindLatestSpecimen(String programId) throws Exception {
		return getSpecimenDao().toSpecimenVO(getSpecimenDao().findLatestSpecimen(programId));
	}

	@Override
	protected Collection<SpecimenVO> handleFindUnsynchedSpecimen() throws Exception {
		
		List<SpecimenVO> vos = new ArrayList<>();
		
		for(Specimen sp : getSpecimenDao().findUnsynchedSpecimen()) {
			vos.add(getSpecimenDao().toSpecimenVO(sp));
		}
		
		return vos;
	}

	@Override
	protected Collection<SpecimenVO> handleSaveSpecimen(Set<SpecimenVO> specimens) throws Exception {
		
		Collection<Specimen> create = new ArrayList<Specimen>();
		Collection<Specimen> update = new ArrayList<Specimen>();
		
		for(SpecimenVO sp : specimens) {
			Specimen specimen = getSpecimenDao().specimenVOToEntity(sp);
	    		    	
	    	if(specimen.getId() == null) {
	    		create.add(specimen);
	    	} else {
	    		update.add(specimen);
	    	}
		}
		
		
		create = getSpecimenDao().create(create);
		getSpecimenDao().update(update);
		
		Collection<SpecimenVO> specimenVos = new ArrayList();
		
		for(Specimen sp : create) {
			specimenVos.add(getSpecimenDao().toSpecimenVO(sp));
		}
		
		for(Specimen sp : update) {
			specimenVos.add(getSpecimenDao().toSpecimenVO(sp));
		}
				
		// TODO Auto-generated method stub
		return specimenVos;
	}
}