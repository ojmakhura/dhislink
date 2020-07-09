package bw.ub.ehealth.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.uuid.Generators;

import bw.ub.ehealth.dhislink.redacap.auth.service.RedcapAuthService;
import bw.ub.ehealth.dhislink.redacap.auth.service.SecurityService;
import bw.ub.ehealth.dhislink.redacap.auth.service.SecurityServiceImpl;
import bw.ub.ehealth.dhislink.redacap.auth.vo.RedcapAuthVO;
import bw.ub.ehealth.dhislink.security.UserDetailsImpl;
import bw.ub.ehealth.dhislink.vo.JwtAuthenticationResponse;
import bw.ub.ehealth.security.JwtTokenProvider;

@RestController
@RequestMapping("/ddpcontroller/auth")
@CrossOrigin(origins = "https://ehealth.ub.bw/redcap/", maxAge = 3600)
public class RedcapAuthController {

	private static final Logger logger = LoggerFactory.getLogger(RedcapAuthController.class);
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private JwtTokenProvider tokenProvider;
	
	@Autowired
	private SecurityService securityService;
	
	@Autowired
	private RedcapAuthService redcapAuthService;
	
	private HashMap<String, String> refreshTokens = new HashMap<String, String>();
	private HashMap<String, String> loginTokens = new HashMap<String, String>();
	
	@PostMapping("/signin")
	@ResponseBody
	public ResponseEntity<?> authenticatedUser(@Valid @RequestBody RedcapAuthVO authVO) {
		
		logger.debug(authVO.toString());
		securityService.login(authVO.getUsername(), authVO.getPassword());
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if(authentication.getPrincipal() instanceof String && 
				((String)authentication.getPrincipal()).equalsIgnoreCase("anonymousUser")) {

			return new ResponseEntity(HttpStatus.UNAUTHORIZED);
		}
		
		String jwt = tokenProvider.generateToken((@NotNull UserDetailsImpl) authentication.getPrincipal());
		JwtAuthenticationResponse response = new JwtAuthenticationResponse();
		
		if(!StringUtils.isBlank(jwt)) {
			response.setAccessToken(jwt);
			response.setStatus((long)HttpStatus.OK.value());
			response.setUsername(authVO.getUsername());
		} else {
			return new ResponseEntity(HttpStatus.UNAUTHORIZED);
		}
		
		UUID refToken = Generators.randomBasedGenerator().generate();
		
		ArrayList<String> refreshes = new ArrayList<>();
 		for(Map.Entry<String, String> entry : refreshTokens.entrySet()) {
			if(entry.getValue().equals(authVO.getUsername()))
			{
				refreshes.add(entry.getKey());
			}
		}
		
 		for(String k : refreshes) {
 			refreshTokens.remove(k);
 		}
 		
		refreshTokens.put(refToken.toString(), authVO.getUsername());
		loginTokens.put(response.getUsername(), response.getAccessToken());
		
		response.setRefreshToken(refToken.toString());
		
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/logout")
	@ResponseStatus(code = HttpStatus.OK) 
	public void logout(HttpServletRequest request, HttpServletResponse response) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if(authentication.getPrincipal() instanceof String && 
				((String)authentication.getPrincipal()).equalsIgnoreCase("anonymousUser")) {
			return;
		}
		
		String username = ((UserDetailsImpl) authentication.getPrincipal()).getUsername();
		loginTokens.remove(username);
		
		ArrayList<String> refreshes = new ArrayList<>();
 		for(Map.Entry<String, String> entry : refreshTokens.entrySet()) {
			if(entry.getValue().equals(username))
			{
				refreshes.add(entry.getKey());
			}
		}
		
 		for(String k : refreshes) {
 			refreshTokens.remove(k);
 		}
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
	}
	
	@PostMapping("/refresh")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.OK) 
	public ResponseEntity<?> refreshToken(@RequestBody JwtAuthenticationResponse request) {
						
		String username = request.getUsername();

		if( refreshTokens.get(request.getRefreshToken()) == null) {
			return new ResponseEntity(HttpStatus.UNAUTHORIZED);
		}
		
		if(refreshTokens.get(request.getRefreshToken()).equals(request.getUsername())) {
			RedcapAuthVO auth = redcapAuthService.findByUsername(username);
			if(auth != null) {
				String jwt = tokenProvider.generateToken(new UserDetailsImpl(auth.getUsername(), auth.getPassword()));
				JwtAuthenticationResponse response = new JwtAuthenticationResponse();
				response.setAccessToken(jwt);
				response.setStatus((long)HttpStatus.OK.value());
				UUID refToken = Generators.randomBasedGenerator().generate();
				
				refreshTokens.remove(request.getRefreshToken());
				refreshTokens.put(refToken.toString(), auth.getUsername());				
				response.setRefreshToken(refToken.toString());
				
				return ResponseEntity.ok(response);
			}
			
		}
		
		return new ResponseEntity(HttpStatus.UNAUTHORIZED);
		
	}
	
	@GetMapping("/me")
	@ResponseBody
	public UserDetailsImpl getLoggedInUser() {
		String username = securityService.findLoggedInUsername();
		
		if(StringUtils.isBlank(username) || !loginTokens.containsKey(username)) {
			return null;
		}
		
		UserDetailsImpl details = new UserDetailsImpl();
		details.setUsername(username);
		
		return details;
	}
}
