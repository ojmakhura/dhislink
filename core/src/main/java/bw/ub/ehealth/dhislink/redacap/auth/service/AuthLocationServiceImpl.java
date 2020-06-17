// license-header java merge-point
/**
 * This is only generated once! It will never be overwritten.
 * You can (and have to!) safely modify it by hand.
 * TEMPLATE:    SpringServiceImpl.vsl in andromda-spring cartridge
 * MODEL CLASS: NewModel::bw.ub.ehealth.dhislink::redacap::auth::service::AuthLocationService
 * STEREOTYPE:  Service
 */
package bw.ub.ehealth.dhislink.redacap.auth.service;

import bw.ub.ehealth.dhislink.redacap.auth.RedcapAuthLocation;
import bw.ub.ehealth.dhislink.redacap.auth.vo.RedcapAuthLocationVO;
import bw.ub.ehealth.dhislink.redacap.auth.vo.RedcapAuthVO;
import bw.ub.ehealth.dhislink.redacap.location.vo.LocationVO;
import java.util.Collection;

import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @see bw.ub.ehealth.dhislink.redacap.auth.service.AuthLocationService
 */
 @Transactional(propagation = Propagation.REQUIRED, readOnly=false)
@Service("authLocationService")
public class AuthLocationServiceImpl
    extends AuthLocationServiceBase
{

    /**
     * @see bw.ub.ehealth.dhislink.redacap.auth.service.AuthLocationService#createAuthLocation(RedcapAuthLocationVO)
     */
    @Override
    protected  RedcapAuthLocationVO handleCreateAuthLocation(RedcapAuthLocationVO authLocationVO)
        throws Exception
    {
    	RedcapAuthLocation loc = getRedcapAuthLocationDao().redcapAuthLocationVOToEntity(authLocationVO);
    	
    	if(loc != null && loc.getId() == null) {
    		loc = getRedcapAuthLocationDao().create(loc);
    		return getRedcapAuthLocationDao().toRedcapAuthLocationVO(loc);
    	}
    	
    	return null;
    }

    /**
     * @see bw.ub.ehealth.dhislink.redacap.auth.service.AuthLocationService#updateAuthLocation(LocationVO)
     */
    @Override
    protected  void handleUpdateAuthLocation(RedcapAuthLocationVO authLocationVO)
        throws Exception
    {
    	RedcapAuthLocation loc = getRedcapAuthLocationDao().redcapAuthLocationVOToEntity(authLocationVO);
    	
    	if(loc != null && loc.getId() == null) {
    		getRedcapAuthLocationDao().update(loc);
    	}
    }

    /**
     * @see bw.ub.ehealth.dhislink.redacap.auth.service.AuthLocationService#deleteAuthLocation(Long)
     */
    @Override
    protected  void handleDeleteAuthLocation(Long id)
        throws Exception
    {
        // TODO implement protected  void handleDeleteAuthLocation(Long id)
        throw new UnsupportedOperationException("bw.ub.ehealth.dhislink.redacap.auth.service.AuthLocationService.handleDeleteAuthLocation(Long id) Not implemented!");
    }

    /**
     * @see bw.ub.ehealth.dhislink.redacap.auth.service.AuthLocationService#findById(Long)
     */
    @Override
    protected  RedcapAuthLocationVO handleFindById(@NotNull Long id)
        throws Exception
    {
    	return getRedcapAuthLocationDao().toRedcapAuthLocationVO(getRedcapAuthLocationDao().load(id));
    }

    /**
     * @see bw.ub.ehealth.dhislink.redacap.auth.service.AuthLocationService#searchByRedcapAuth(RedcapAuthVO)
     */
    @Override
    protected  RedcapAuthLocationVO handleSearchByRedcapAuth(RedcapAuthVO auth)
        throws Exception
    {    	
    	RedcapAuthLocation loc = getRedcapAuthLocationDao().findAuthLocation(auth.getUsername());
    	
    	if(loc != null) {
    		return getRedcapAuthLocationDao().toRedcapAuthLocationVO(loc);
    	}
    	
    	return null;
    }

    /**
     * @see bw.ub.ehealth.dhislink.redacap.auth.service.AuthLocationService#findAll()
     */
    @Override
    protected  Collection<RedcapAuthLocationVO> handleFindAll()
        throws Exception
    {
    	return getRedcapAuthLocationDao().toRedcapAuthLocationVOCollection(getRedcapAuthLocationDao().loadAll());
    }

}