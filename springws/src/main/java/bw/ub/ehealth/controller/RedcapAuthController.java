package bw.ub.ehealth.controller;

import java.net.URI;
import java.util.Collections;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bw.ub.ehealth.dhislink.redacap.auth.vo.RedcapAuthVO;
import bw.ub.ehealth.dhislink.vo.JwtAuthenticationResponse;
import bw.ub.ehealth.security.JwtTokenProvider;

@RestController
@RequestMapping("/ddpcontroller/auth")
public class RedcapAuthController {

	@Autowired
	AuthenticationManager authenticationManager;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	JwtTokenProvider tokenProvider;
	
	@PostMapping("/signin")
	public ResponseEntity<?> authenticatedUser(@Valid @RequestBody RedcapAuthVO authVO) {
		
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(authVO.getUsername(), authVO.getPassword())
		);
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = tokenProvider.generateToken(authentication);
		
		return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
	}
	
	/*@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
		
		if(userRepository.existsByUsername(signUpRequest.getUsername())) {
			return new ResponseEntity(new ApiResponse(false, "Username is already taken"),
					HttpStatus.BAD_REQUEST);
		}
		
		if(userRepository.existsByEmail(signUpRequest.getEmail())) {
			return new ResponseEntity(new ApiResponse(false, "Email is already in use."), HttpStatus.BAD_REQUEST);
		}
		
		// Creating user account
		User user = new User(signUpRequest.getName(), signUpRequest.getUsername(),
				signUpRequest.getEmail(), signUpRequest.getPassword());
		
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		
		Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
				.orElseThrow(() -> new AppException("User Role not set"));
		
		user.setRoles(Collections.singleton(userRole));
		
		User result = userRepository.save(user);
		
		URI location = ServletUriComponentsBuilder
				.fromCurrentContextPath().path("/api/users/{username}")
				.buildAndExpand(result.getUsername()).toUri();
		
		return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully!"));
	}*/
}
