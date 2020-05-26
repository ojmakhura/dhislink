// license-header java merge-point
/**
 * This is only generated once! It will never be overwritten.
 * You can (and have to!) safely modify it by hand.
 * TEMPLATE:    SpringServiceImpl.vsl in andromda-spring cartridge
 * MODEL CLASS: NewModel::bw.ub.ehealth.dhislink::redacap::auth::service::AuthLocationService
 * STEREOTYPE:  Service
 */
package bw.ub.ehealth.dhislink.redacap.auth.service;

import bw.ub.ehealth.dhislink.redacap.auth.vo.RedcapAuthLocationVO;
import bw.ub.ehealth.dhislink.redacap.auth.vo.RedcapAuthVO;
import bw.ub.ehealth.dhislink.redacap.location.vo.LocationVO;
import java.util.Collection;
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
        // TODO implement protected  RedcapAuthLocationVO handleCreateAuthLocation(RedcapAuthLocationVO authLocationVO)
        throw new UnsupportedOperationException("bw.ub.ehealth.dhislink.redacap.auth.service.AuthLocationService.handleCreateAuthLocation(RedcapAuthLocationVO authLocationVO) Not implemented!");
    }

    /**
     * @see bw.ub.ehealth.dhislink.redacap.auth.service.AuthLocationService#updateAuthLocation(LocationVO)
     */
    @Override
    protected  void handleUpdateAuthLocation(LocationVO authLocationVO)
        throws Exception
    {
        // TODO implement protected  void handleUpdateAuthLocation(LocationVO authLocationVO)
        throw new UnsupportedOperationException("bw.ub.ehealth.dhislink.redacap.auth.service.AuthLocationService.handleUpdateAuthLocation(LocationVO authLocationVO) Not implemented!");
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
    protected  RedcapAuthLocationVO handleFindById(Long id)
        throws Exception
    {
        // TODO implement protected  RedcapAuthLocationVO handleFindById(Long id)
        throw new UnsupportedOperationException("bw.ub.ehealth.dhislink.redacap.auth.service.AuthLocationService.handleFindById(Long id) Not implemented!");
    }

    /**
     * @see bw.ub.ehealth.dhislink.redacap.auth.service.AuthLocationService#searchByRedcapAuth(RedcapAuthVO)
     */
    @Override
    protected  RedcapAuthVO handleSearchByRedcapAuth(RedcapAuthVO auth)
        throws Exception
    {
        // TODO implement protected  RedcapAuthVO handleSearchByRedcapAuth(RedcapAuthVO auth)
        throw new UnsupportedOperationException("bw.ub.ehealth.dhislink.redacap.auth.service.AuthLocationService.handleSearchByRedcapAuth(RedcapAuthVO auth) Not implemented!");
    }

    /**
     * @see bw.ub.ehealth.dhislink.redacap.auth.service.AuthLocationService#findAll()
     */
    @Override
    protected  Collection<RedcapAuthLocationVO> handleFindAll()
        throws Exception
    {
        // TODO implement protected  Collection<RedcapAuthLocationVO> handleFindAll()
        throw new UnsupportedOperationException("bw.ub.ehealth.dhislink.redacap.auth.service.AuthLocationService.handleFindAll() Not implemented!");
    }

}