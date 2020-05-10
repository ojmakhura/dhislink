// license-header java merge-point
/**
 * This is only generated once! It will never be overwritten.
 * You can (and have to!) safely modify it by hand.
 * TEMPLATE:    SpringServiceImpl.vsl in andromda-spring cartridge
 * MODEL CLASS: NewModel::bw.ub.ehealth.dhislink::patient::service::PatientService
 * STEREOTYPE:  Service
 */
package bw.ub.ehealth.dhislink.patient.service;

import bw.ub.ehealth.dhislink.patient.Patient;
import bw.ub.ehealth.dhislink.patient.vo.PatientVO;
import bw.ub.ehealth.dhislink.specimen.Specimen;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @see bw.ub.ehealth.dhislink.patient.service.PatientService
 */
 @Transactional(propagation = Propagation.REQUIRED, readOnly=false)
@Service("patientService")
public class PatientServiceImpl
    extends PatientServiceBase
{

    /**
     * @see bw.ub.ehealth.dhislink.patient.service.PatientService#savePatient(PatientVO)
     */
    @Override
    protected  PatientVO handleSavePatient(PatientVO patientVO)
        throws Exception
    {
    	
    	if(patientVO.getFirstName() == null) {
    		patientVO.setFirstName(patientVO.getSurname());
    	}
    	
    	if(patientVO.getSurname() == null) {
    		patientVO.setSurname(patientVO.getFirstName());
    	}
    	
    	Patient patient = getPatientDao().patientVOToEntity(patientVO);

    	
    	if(patient.getId() == null) {
    		patient = getPatientDao().create(patient);
    	} else {
    		getPatientDao().update(patient); 
    	}
    	
    	if(patient.getSpecimen() != null) {
    		for(Specimen s : patient.getSpecimen()) {
    			s = getSpecimenDao().createOrUpdate(s);
    		}
    	}
    	
    	return getPatientDao().toPatientVO(patient);
    }

    /**
     * @see bw.ub.ehealth.dhislink.patient.service.PatientService#findById(Long)
     */
    @Override
    protected  PatientVO handleFindById(Long id)
        throws Exception
    {
    	return getPatientDao().toPatientVO(getPatientDao().load(id));
    }

    /**
     * @see bw.ub.ehealth.dhislink.patient.service.PatientService#findByIdentityNo(String)
     */
    @Override
    protected  PatientVO handleFindByIdentityNo(String identityNo)
        throws Exception
    {
    	return getPatientDao().toPatientVO(getPatientDao().getPatientByIdentityNo(identityNo));
    }

}