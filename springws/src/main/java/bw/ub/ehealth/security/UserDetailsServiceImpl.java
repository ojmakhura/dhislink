package bw.ub.ehealth.security;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import bw.ub.ehealth.dhislink.redacap.auth.service.RedcapAuthService;
import bw.ub.ehealth.dhislink.redacap.auth.vo.RedcapAuthVO;
import bw.ub.ehealth.dhislink.security.UserDetailsImpl;

@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {
	
	private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
	
	@Autowired
	RedcapAuthService redcapAuthService;
	
	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		// Let people login with either username or email
		RedcapAuthVO auth = redcapAuthService.findByUsername(username);		
		return new UserDetailsImpl(auth.getUsername(), auth.getPassword());
	}
	
	// This method is used by JWTAuthenticationFilter
	@Transactional
	public UserDetails loadUserById(Long id) {
		
		/*
		 * User user = userRepository.findById(id) .orElseThrow(() -> new
		 * UsernameNotFoundException("User not found with id " + id));
		 * 
		 * return UserPrincipal.create(user);
		 */
		
		return null;
	}
}
