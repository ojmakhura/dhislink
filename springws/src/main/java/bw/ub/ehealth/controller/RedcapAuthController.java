package bw.ub.ehealth.controller;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

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
	
	@PostMapping("/signin")
	@ResponseBody
	public ResponseEntity<?> authenticatedUser(@Valid @RequestBody RedcapAuthVO authVO) {
		
		securityService.login(authVO.getUsername(), authVO.getPassword());
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		String jwt = tokenProvider.generateToken((@NotNull UserDetailsImpl) authentication.getPrincipal());
		JwtAuthenticationResponse response = new JwtAuthenticationResponse();
		
		if(!StringUtils.isBlank(jwt)) {
			response.setAccessToken(jwt);
			response.setStatus((long)HttpStatus.OK.value());
		} else {
			return new ResponseEntity(HttpStatus.UNAUTHORIZED);
		}
		
		UUID refToken = Generators.randomBasedGenerator().generate();
		refreshTokens.put(refToken.toString(), authVO.getUsername());
		
		response.setRefreshToken(refToken.toString());
		
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("/refresh")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.OK) 
	public ResponseEntity<?> refreshToken(@RequestBody JwtAuthenticationResponse request) {
				
		String username = request.getUsername();
		
		if(refreshTokens.get(request.getRefreshToken()) == null) {
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
		
		if(StringUtils.isBlank(username)) {
			return null;
		}
		
		UserDetailsImpl details = new UserDetailsImpl();
		details.setUsername(username);
		
		return details;
	}
}
