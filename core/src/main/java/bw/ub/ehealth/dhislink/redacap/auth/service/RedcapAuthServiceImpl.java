// license-header java merge-point
/**
 * This is only generated once! It will never be overwritten.
 * You can (and have to!) safely modify it by hand.
 * TEMPLATE:    SpringServiceImpl.vsl in andromda-spring cartridge
 * MODEL CLASS: NewModel::bw.ub.ehealth.dhislink::redacap::auth::service::RedcapAuthService
 * STEREOTYPE:  Service
 */
package bw.ub.ehealth.dhislink.redacap.auth.service;

import bw.ub.ehealth.dhislink.redacap.auth.RedcapAuth;
import bw.ub.ehealth.dhislink.redacap.auth.vo.RedcapAuthVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @see bw.ub.ehealth.dhislink.redacap.auth.service.RedcapAuthService
 */
 @Transactional(propagation = Propagation.REQUIRED, readOnly=false)
@Service("redcapAuthService")
public class RedcapAuthServiceImpl
    extends RedcapAuthServiceBase
{

    /**
     * @see bw.ub.ehealth.dhislink.redacap.auth.service.RedcapAuthService#findByUsername(String)
     */
    @Override
    protected  RedcapAuthVO handleFindByUsername(String username)
        throws Exception
    {
    	RedcapAuth auth = getRedcapAuthDao().searchUniqueUsername(username);
    	
    	if(auth == null) {
    		return null;
    	}
    	
    	return getRedcapAuthDao().toRedcapAuthVO(auth);
    }

	@Override
	protected String handleGetUserProjectToken(String username, Long projectId) throws Exception {
		
		return getUserRightDao().getUserProjectToken(username, projectId);
	}

}