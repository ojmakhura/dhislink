// license-header java merge-point
/**
 * This is only generated once! It will never be overwritten.
 * You can (and have to!) safely modify it by hand.
 * TEMPLATE:    SpringServiceImpl.vsl in andromda-spring cartridge
 * MODEL CLASS: NewModel::bw.ub.ehealth.dhislink::redacap::auth::service::SeurityService
 * STEREOTYPE:  Service
 */
package bw.ub.ehealth.dhislink.redacap.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @see bw.ub.ehealth.dhislink.redacap.auth.service.SeurityService
 */
 @Transactional(propagation = Propagation.REQUIRED, readOnly=false)
@Service("seurityService")
public class SeurityServiceImpl
    extends SeurityServiceBase
{

    /**
     * @see bw.ub.ehealth.dhislink.redacap.auth.service.SeurityService#findLoggedInUsername()
     */
    @Override
    protected  String handleFindLoggedInUsername()
        throws Exception
    {
        // TODO implement protected  String handleFindLoggedInUsername()
        throw new UnsupportedOperationException("bw.ub.ehealth.dhislink.redacap.auth.service.SeurityService.handleFindLoggedInUsername() Not implemented!");
    }

    /**
     * @see bw.ub.ehealth.dhislink.redacap.auth.service.SeurityService#login(String, String)
     */
    @Override
    protected  void handleLogin(String username, String password)
        throws Exception
    {
        // TODO implement protected  void handleLogin(String username, String password)
        throw new UnsupportedOperationException("bw.ub.ehealth.dhislink.redacap.auth.service.SeurityService.handleLogin(String username, String password) Not implemented!");
    }

}