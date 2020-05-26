// license-header java merge-point
/**
 * This is only generated once! It will never be overwritten.
 * You can (and have to!) safely modify it by hand.
 * TEMPLATE:    SpringServiceImpl.vsl in andromda-spring cartridge
 * MODEL CLASS: NewModel::bw.ub.ehealth.dhislink::redacap::auth::service::SeurityService
 * STEREOTYPE:  Service
 */
package bw.ub.ehealth.dhislink.redacap.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import bw.ub.ehealth.dhislink.redacap.auth.RedcapAuth;
import bw.ub.ehealth.dhislink.security.DhislinkPasswordEncoder;
import bw.ub.ehealth.dhislink.security.UserDetailsImpl;

/**
 * @see bw.ub.ehealth.dhislink.redacap.auth.service.SeurityService
 */
 @Transactional(propagation = Propagation.REQUIRED, readOnly=false)
@Service("securityService")
public class SecurityServiceImpl
    extends SecurityServiceBase
{
	 @Autowired
	 private AuthenticationManager authenticationManager;
	 
	 @Autowired
	 private DhislinkPasswordEncoder dhislinkPasswordEncoder;
	 
	 private static final Logger logger = LoggerFactory.getLogger(SecurityServiceImpl.class);
    /**
     * @see bw.ub.ehealth.dhislink.redacap.auth.service.SeurityService#findLoggedInUsername()
     */
    @Override
    protected  String handleFindLoggedInUsername()
        throws Exception
    {
    	logger.info(SecurityContextHolder.getContext().toString());
    	Object userDetails = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	
    	if(userDetails instanceof UserDetailsImpl) {
    		return ((UserDetailsImpl)userDetails).getUsername();
    	}
    	
    	return null;
    }

    /**
     * @see bw.ub.ehealth.dhislink.redacap.auth.service.SeurityService#login(String, String)
     */
    @Override
    protected  void handleLogin(String username, String password)
        throws Exception
    {
    	RedcapAuth auth = getRedcapAuthDao().userAuthentication(username, password);
    	UserDetailsImpl userDetails = new UserDetailsImpl();
    	if(auth != null) {
    		
    		userDetails.setUsername(auth.getUsername());
    		userDetails.setPassword(auth.getPassword());
    	}
    	
    	String encodedPassword = dhislinkPasswordEncoder.encode(password + auth.getPasswordSalt());
    	UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails, password + auth.getPasswordSalt(), userDetails.getAuthorities());
    	
    	Authentication authentication = authenticationManager.authenticate(token);
    	    	
    	if(authentication.isAuthenticated()) {
    		SecurityContextHolder.getContext().setAuthentication(authentication);
    	}
    }

}